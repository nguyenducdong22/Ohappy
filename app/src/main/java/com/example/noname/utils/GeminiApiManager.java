package com.example.noname.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GeminiApiManager {

    private static final String GEMINI_API_KEY = "AIzaSyAoLZ7F_18w5vLnRBZ-oLS4t-fkwoApwRQ"; // THAY THẾ BẰNG API KEY CỦA BẠN
    private static final String GEMINI_API_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final String TAG = "GeminiApiManager";

    // BỎ HOẶC BỎ COMMENT DÒNG NÀY (KHÔNG CẦN MAX_OUTPUT_TOKENS NỮA)
    // private static final int MAX_OUTPUT_TOKENS = 100;

    public interface GeminiApiResponseListener {
        void onSuccess(String responseText);
        void onFailure(String errorMessage);
    }

    public static void generateContent(final String prompt, final GeminiApiResponseListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL(GEMINI_API_ENDPOINT + "?key=" + GEMINI_API_KEY);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);

                    // Tạo JSON body cho request
                    JSONObject requestBody = new JSONObject();

                    // BỎ HOẶC BỎ COMMENT PHẦN generationConfig VÀ maxOutputTokens NÀY
                    // JSONObject generationConfig = new JSONObject();
                    // generationConfig.put("maxOutputTokens", MAX_OUTPUT_TOKENS);
                    // requestBody.put("generationConfig", generationConfig);

                    JSONArray contentsArray = new JSONArray();
                    JSONObject contentObject = new JSONObject();
                    JSONArray partsArray = new JSONArray();
                    JSONObject partObject = new JSONObject();

                    // <<< THAY ĐỔI DÒNG NÀY ĐỂ THÊM YÊU CẦU VÀO PROMPT >>>
                    // Ví dụ: "Trả lời ngắn gọn: " + prompt
                    // Hoặc "Hãy trả lời một cách ngắn gọn và súc tích: " + prompt
                    String modifiedPrompt = "Trả lời ngắn gọn: " + prompt; // Thêm chỉ dẫn vào prompt
                    partObject.put("text", modifiedPrompt); // Sử dụng prompt đã sửa đổi

                    partsArray.put(partObject);
                    contentObject.put("parts", partsArray);
                    contentsArray.put(contentObject);
                    requestBody.put("contents", contentsArray);

                    // Ghi JSON body vào OutputStream của connection
                    OutputStream os = connection.getOutputStream();
                    os.write(requestBody.toString().getBytes("UTF-8"));
                    os.close();

                    int responseCode = connection.getResponseCode();
                    Log.d(TAG, "Response Code: " + responseCode);

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        String jsonResponse = response.toString();
                        Log.d(TAG, "Full API Response: " + jsonResponse);

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
                        String errorResponse = "";
                        if (connection.getErrorStream() != null) {
                            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                            StringBuilder error = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                error.append(line);
                            }
                            errorResponse = error.toString();
                            Log.e(TAG, "API Error Response: " + errorResponse);
                        }
                        listener.onFailure("API call failed with code " + responseCode + ": " + errorResponse);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Network or API call error: " + e.getMessage(), e);
                    listener.onFailure("Network or API call error: " + e.getMessage());
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (Exception e) {
                            Log.e(TAG, "Error closing reader: " + e.getMessage());
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
}