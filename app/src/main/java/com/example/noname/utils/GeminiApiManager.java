package com.example.noname.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeminiApiManager {

    private static final String TAG = "GeminiApiManager";
    private static final String GEMINI_API_KEY = "AIzaSyDg97ByBjxHFnp6bjSiT_6XW_erulCGwk0";
    private static final String GEMINI_API_ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static GeminiApiManager instance;
    private final OkHttpClient client;

    // Giao diện phản hồi
    public interface GeminiApiResponseListener {
        void onSuccess(String responseText);
        void onFailure(String errorMessage);
    }

    // Singleton
    private GeminiApiManager() {
        client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .build();
    }

    public static GeminiApiManager getInstance() {
        if (instance == null) {
            instance = new GeminiApiManager();
        }
        return instance;
    }

    public void generateContent(String prompt, GeminiApiResponseListener listener) {
        try {
            // Tạo JSON body
            JSONObject requestBody = new JSONObject();

            JSONArray contentsArray = new JSONArray();
            JSONObject contentObject = new JSONObject();
            JSONArray partsArray = new JSONArray();
            JSONObject partObject = new JSONObject();

            String modifiedPrompt = "Trả lời ngắn gọn: " + prompt;
            partObject.put("text", modifiedPrompt);

            partsArray.put(partObject);
            contentObject.put("parts", partsArray);
            contentsArray.put(contentObject);
            requestBody.put("contents", contentsArray);

            // Request
            Request request = new Request.Builder()
                    .url(GEMINI_API_ENDPOINT + "?key=" + GEMINI_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody.toString(), JSON))
                    .build();

            long startTime = System.currentTimeMillis();

            // Gửi request async
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Request failed: " + e.getMessage(), e);
                    listener.onFailure("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    long endTime = System.currentTimeMillis();
                    Log.d(TAG, "Total request time: " + (endTime - startTime) + "ms");

                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "No error body";
                        Log.e(TAG, "Unsuccessful response: " + errorBody);
                        listener.onFailure("HTTP error " + response.code() + ": " + errorBody);
                        return;
                    }

                    String jsonResponse = response.body().string();
                    Log.d(TAG, "API response: " + jsonResponse);

                    try {
                        JSONObject json = new JSONObject(jsonResponse);
                        JSONArray candidates = json.optJSONArray("candidates");

                        if (candidates != null && candidates.length() > 0) {
                            JSONObject content = candidates.getJSONObject(0).optJSONObject("content");
                            if (content != null) {
                                JSONArray contentParts = content.optJSONArray("parts");
                                if (contentParts != null && contentParts.length() > 0) {
                                    String generatedText = contentParts.getJSONObject(0)
                                            .optString("text", "Không có nội dung.");
                                    listener.onSuccess(generatedText);
                                } else {
                                    listener.onFailure("No text part found.");
                                }
                            } else {
                                listener.onFailure("No content found.");
                            }
                        } else {
                            listener.onFailure("No candidates found.");
                        }

                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parse error: " + e.getMessage(), e);
                        listener.onFailure("Parsing error: " + e.getMessage());
                    }
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "JSON building error: " + e.getMessage(), e);
            listener.onFailure("JSON creation failed: " + e.getMessage());
        }
    }
}
