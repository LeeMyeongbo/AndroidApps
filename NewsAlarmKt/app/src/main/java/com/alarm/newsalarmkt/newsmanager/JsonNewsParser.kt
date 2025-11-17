package com.alarm.newsalarmkt.newsmanager

import com.alarm.newsalarmkt.utils.LogUtil
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class JsonNewsParser {

    fun parseNaverNewsToJSONSet(response: String): MutableSet<JSONObject> {
        try {
            val jsonArticles = JSONObject(response)
            val jsonArticleArray = jsonArticles.getJSONArray("items")
            LogUtil.logD(CLASS_NAME, "parseNaverNewsToJSONSet", "json articles ready")

            return selectOnlyNaverNews(jsonArticleArray)
        } catch (e: JSONException) {
            LogUtil.logE(CLASS_NAME, "parseNaverNewsToJSONSet", e)
        }
        return HashSet()
    }

    @Throws(JSONException::class)
    private fun selectOnlyNaverNews(jsonArticleArray: JSONArray): MutableSet<JSONObject> {
        val jsonArticleSet: MutableSet<JSONObject> = HashSet()
        for (i in 0..<jsonArticleArray.length()) {
            val jsonArticle = jsonArticleArray.getJSONObject(i)
            if (jsonArticle.getString("link").contains("news.naver.com")) {
                jsonArticleSet.add(jsonArticle)
            }
            if (jsonArticleSet.size == 5) {
                break
            }
        }
        LogUtil.logI(
            CLASS_NAME, "selectOnlyNaverNews",
            "count of naver news article : " + jsonArticleSet.size
        )

        return jsonArticleSet
    }

    companion object {
        private const val CLASS_NAME = "JsonNewsParser"
    }
}
