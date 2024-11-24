package com.alarm.newsalarm.newsmanager;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NewsLoader {

    private static final String CLASS_NAME = "NewsLoader";

    private final Handler handler;
    private final RequestQueue queue;
    private final Bundle bundle = new Bundle();

    NewsLoader(Context context, Handler handler) {
        this.handler = handler;
        queue = Volley.newRequestQueue(context.getApplicationContext());
    }

    void load(String keyword) {
        StringRequest request = new StringRequest(
            Request.Method.GET,
            "https://openapi.naver.com/v1/search/news?query=" + keyword + "&display=20",
            this::convertToJSONFormat,
            error -> sendMessageFailure()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("X-Naver-Client-Id", "77ZHNWgvaXOrqCOeo3s5");
                params.put("X-Naver-Client-Secret", "x_SI3JImaI");

                return params;
            }
        };
        request.setShouldCache(false);
        queue.add(request);
    }

    private void convertToJSONFormat(String response) {
        try {
            JSONObject jsonArticles = new JSONObject(response);
            JSONArray jsonArticleArray = jsonArticles.getJSONArray("items");
            Log.d(CLASS_NAME, "convertToJSONFormat$json article array ready");

            Set<String> articleSet = selectOnlyNaverNews(jsonArticleArray);
            if (!articleSet.isEmpty()) {
                sendMessageSuccess(new ArrayList<>(articleSet));
                return;
            }
        } catch (JSONException e) {
            Log.e(CLASS_NAME, "convertToJSONFormat$" + e.getMessage());
        }
        sendMessageFailure();
    }

    @NonNull
    private Set<String> selectOnlyNaverNews(JSONArray jsonArticleArray) throws JSONException {
        Set<String> jsonArticleSet = new HashSet<>();

        for (int i = 0; i < jsonArticleArray.length(); i++) {
            JSONObject jsonArticle = jsonArticleArray.getJSONObject(i);
            if (jsonArticle.getString("link").contains("news.naver.com")) {
                jsonArticleSet.add(jsonArticle.toString());
            }
            if (jsonArticleSet.size() == 5) {
                Log.i(CLASS_NAME, "selectOnlyNaverNews$5 naver news contents ready");
                break;
            }
        }
        return jsonArticleSet;
    }

    private void sendMessageSuccess(ArrayList<String> jsonArticleList) {
        Message msg = handler.obtainMessage();
        bundle.putStringArrayList("load_return", jsonArticleList);
        msg.setData(bundle);
        msg.what = NewsNotifier.MSG_LOAD_NEWS_SUCCESS;
        handler.sendMessage(msg);
    }

    private void sendMessageFailure() {
        Message msg = handler.obtainMessage();
        msg.what = NewsNotifier.MSG_LOAD_NEWS_FAILURE;
        handler.sendMessage(msg);
    }

    void release() {
        queue.stop();
    }
}
