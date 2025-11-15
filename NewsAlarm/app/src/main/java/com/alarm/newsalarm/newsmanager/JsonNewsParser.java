package com.alarm.newsalarm.newsmanager;

import androidx.annotation.NonNull;

import com.alarm.newsalarm.utils.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

class JsonNewsParser {

    private static final String CLASS_NAME = "JsonNewsParser";

    Set<JSONObject> parseNaverNewsToJSONSet(String response) {
        try {
            JSONObject jsonArticles = new JSONObject(response);
            JSONArray jsonArticleArray = jsonArticles.getJSONArray("items");
            LogUtil.logD(CLASS_NAME, "parseNaverNewsToJSONSet", "json articles ready");

            return selectOnlyNaverNews(jsonArticleArray);
        } catch (JSONException e) {
            LogUtil.logE(CLASS_NAME, "parseNaverNewsToJSONSet", e);
        }
        return new HashSet<>();
    }

    @NonNull
    private Set<JSONObject> selectOnlyNaverNews(JSONArray jsonArticleArray) throws JSONException {
        Set<JSONObject> jsonArticleSet = new HashSet<>();
        for (int i = 0; i < jsonArticleArray.length(); i++) {
            JSONObject jsonArticle = jsonArticleArray.getJSONObject(i);
            if (jsonArticle.getString("link").contains("news.naver.com")) {
                jsonArticleSet.add(jsonArticle);
            }
            if (jsonArticleSet.size() == 5) {
                break;
            }
        }
        LogUtil.logI(CLASS_NAME, "selectOnlyNaverNews",
            "count of naver news article : " + jsonArticleSet.size());

        return jsonArticleSet;
    }
}
