package com.screenassistant;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AIHelper {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = "YOUR_API_KEY_HERE";
    private OkHttpClient client;
    private Gson gson;

    public interface AICallback {
        void onSuccess(String answer);
        void onFailure(String error);
    }

    public AIHelper() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        gson = new Gson();
    }

    public void getAnswer(String question, AICallback callback) {
        try {
            JsonObject messageObj = new JsonObject();
            messageObj.addProperty("role", "user");
            messageObj.addProperty("content", "请回答以下问题,只给出答案和简要解释:\n" + question);

            JsonObject requestJson = new JsonObject();
            requestJson.addProperty("model", "gpt-3.5-turbo");
            requestJson.add("messages", gson.toJsonTree(new JsonObject[]{messageObj}));
            requestJson.addProperty("max_tokens", 500);
            requestJson.addProperty("temperature", 0.7);

            RequestBody body = RequestBody.create(
                    requestJson.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnMainThread(() -> callback.onFailure(e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        try {
                            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                            String answer = jsonResponse.getAsJsonArray("choices")
                                    .get(0).getAsJsonObject()
                                    .getAsJsonObject("message")
                                    .get("content").getAsString();
                            runOnMainThread(() -> callback.onSuccess(answer));
                        } catch (Exception e) {
                            runOnMainThread(() -> callback.onFailure("解析响应失败"));
                        }
                    } else {
                        runOnMainThread(() -> callback.onFailure("API请求失败: " + response.code()));
                    }
                }
            });

        } catch (Exception e) {
            callback.onFailure(e.getMessage());
        }
    }

    private void runOnMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
