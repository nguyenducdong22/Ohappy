package com.example.noname.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class GeminiApiManager {

    private static final String GEMINI_API_KEY = "AIzaSyDg97ByBjxHFnp6bjSiT_6XW_erulCGwk0"; // THAY THẾ BẰNG API KEY CỦA BẠN
    private static final String GEMINI_API_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final String TAG = "GeminiApiManager";

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // Khởi tạo OkHttpClient một lần duy nhất để tận dụng connection pooling
    private static OkHttpClient client = new OkHttpClient();

    public interface GeminiApiResponseListener {
        void onSuccess(String responseText);
        void onFailure(String errorMessage);
    }

    // <<< THÊM TỪ KHÓA 'static' TẠI ĐÂY >>>
    public static void generateContent(final String prompt, final GeminiApiResponseListener listener) {
        try {
            // Tạo JSON body cho request
            JSONObject requestBodyJson = new JSONObject();

            // (Phần generationConfig bị bỏ comment nếu bạn muốn dùng lại maxOutputTokens)
            // JSONObject generationConfig = new JSONObject();
            // generationConfig.put("maxOutputTokens", MAX_OUTPUT_TOKENS);
            // requestBodyJson.put("generationConfig", generationConfig);

            JSONArray contentsArray = new JSONArray();
            JSONObject contentObject = new JSONObject();
            JSONArray partsArray = new JSONArray();
            JSONObject partObject = new JSONObject();

            String modifiedPrompt = "Trả lời ngắn gọn: " + prompt; // Thêm chỉ dẫn vào prompt
            partObject.put("text", modifiedPrompt);

            partsArray.put(partObject);
            contentObject.put("parts", partsArray);
            contentsArray.put(contentObject);
            requestBodyJson.put("contents", contentsArray);

            RequestBody body = RequestBody.create(requestBodyJson.toString(), JSON);

            // Xây dựng request OkHttp
            Request request = new Request.Builder()
                    .url(GEMINI_API_ENDPOINT + "?key=" + GEMINI_API_KEY)
                    .post(body)
                    .build();

            // Thực hiện cuộc gọi không đồng bộ
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "OkHttp API call failed: " + e.getMessage(), e);
                    listener.onFailure("Network or API call error: " + e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String jsonResponse = response.body().string();
                    Log.d(TAG, "OkHttp Response Code: " + response.code());
                    Log.d(TAG, "OkHttp Full API Response: " + jsonResponse);

                    if (response.isSuccessful()) { // Mã 2xx
                        try {
                            JSONObject json = new JSONObject(jsonResponse);
                            JSONArray candidates = json.optJSONArray("candidates");
                            if (candidates != null && candidates.length() > 0) {
                                JSONObject firstCandidate = candidates.getJSONObject(0);
                                JSONObject content = firstCandidate.optJSONObject("content");
                                if (content != null) {
                                    JSONArray contentParts = content.optJSONArray("parts");
                                    if (contentParts != null && contentParts.length() > 0) {
                                        String generatedText = contentParts.getJSONObject(0).optString("text", "Không có nội dung.");
                                        listener.onSuccess(generatedText);
                                    } else {
                                        listener.onFailure("No text part found in response.");
                                    }
                                } else {
                                    listener.onFailure("No content object found in candidate.");
                                }
                            } else {
                                JSONObject promptFeedback = json.optJSONObject("promptFeedback");
                                if (promptFeedback != null) {
                                    JSONArray safetyRatings = promptFeedback.optJSONArray("safetyRatings");
                                    if (safetyRatings != null) {
                                        StringBuilder safetyInfo = new StringBuilder("API blocked due to safety reasons: ");
                                        for (int i = 0; i < safetyRatings.length(); i++) {
                                            JSONObject rating = safetyRatings.getJSONObject(i);
                                            safetyInfo.append(rating.optString("category")).append(": ").append(rating.optString("probability")).append("; ");
                                        }
                                        listener.onFailure(safetyInfo.toString());
                                    } else {
                                        listener.onFailure("No candidates found and no specific safety feedback.");
                                    }
                                } else {
                                    listener.onFailure("No candidates found in response.");
                                }
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error: " + e.getMessage(), e);
                            listener.onFailure("Failed to parse AI response: " + e.getMessage());
                        }

                    } else {
                        // Xử lý lỗi từ server (mã 4xx, 5xx)
                        listener.onFailure("API call failed with code " + response.code() + ": " + jsonResponse);
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Failed to create API request: " + e.getMessage(), e);
            listener.onFailure("Failed to initiate API call: " + e.getMessage());
        }
    }
}