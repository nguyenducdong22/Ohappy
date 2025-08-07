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

    // Your API Key
    private static final String GEMINI_API_KEY = "AIzaSyDg97ByBjxHFnp6bjSiT_6XW_erulCGwk0";
    private static final String GEMINI_API_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final String TAG = "GeminiApiManager";

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // Initialize OkHttpClient once to leverage connection pooling
    private static OkHttpClient client = new OkHttpClient();

    // <<< DEFINE NEW CHATBOT CONTEXT HERE >>>
    private static final String SYSTEM_CONTEXT =
            "You are a virtual assistant in a personal finance management application. " +
                    "Your main task is to provide advice, information, and support on money management, budgeting, saving, investing, and personal spending. " +
                    "You can also answer other questions in a normal and friendly way. " +
                    "At the end of each answer, please add a section 'Suggested questions:' followed by 1-2 questions on the same topic as the user's question but related to finance, separated by a comma. " +
                    "Example: 'Suggested questions: How to create a budget, Money-saving tips'. " + // Added specific instructions on the suggestion format
                    "Keep your answers concise (under 150 words if possible) and helpful.";
    // <<< END NEW CONTEXT DEFINITION >>>


    public interface GeminiApiResponseListener {
        void onSuccess(String responseText);
        void onFailure(String errorMessage);
    }

    public static void generateContent(final String prompt, final GeminiApiResponseListener listener) {
        try {
            JSONObject requestBodyJson = new JSONObject();

            String combinedPrompt = SYSTEM_CONTEXT + "\n\nQuestion: " + prompt;

            JSONArray contentsArray = new JSONArray();
            JSONObject contentObject = new JSONObject();
            JSONArray partsArray = new JSONArray();
            JSONObject partObject = new JSONObject();

            partObject.put("text", combinedPrompt);
            partsArray.put(partObject);
            contentObject.put("parts", partsArray);
            contentsArray.put(contentObject);
            requestBodyJson.put("contents", contentsArray);

            RequestBody body = RequestBody.create(requestBodyJson.toString(), JSON);

            Request request = new Request.Builder()
                    .url(GEMINI_API_ENDPOINT + "?key=" + GEMINI_API_KEY)
                    .post(body)
                    .build();

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

                    if (response.isSuccessful()) {
                        try {
                            JSONObject json = new JSONObject(jsonResponse);
                            JSONArray candidates = json.optJSONArray("candidates");
                            if (candidates != null && candidates.length() > 0) {
                                JSONObject firstCandidate = candidates.getJSONObject(0);
                                JSONObject content = firstCandidate.optJSONObject("content");
                                if (content != null) {
                                    JSONArray contentParts = content.optJSONArray("parts");
                                    if (contentParts != null && contentParts.length() > 0) {
                                        String generatedText = contentParts.getJSONObject(0).optString("text", "No content available.");
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
                        listener.onFailure("API call failed with code " + response.code() + ": " + jsonResponse);
                    }
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "Error building JSON request: " + e.getMessage(), e);
            listener.onFailure("Failed to build API request: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Failed to create API request: " + e.getMessage(), e);
            listener.onFailure("Failed to initiate API call: " + e.getMessage());
        }
    }
}