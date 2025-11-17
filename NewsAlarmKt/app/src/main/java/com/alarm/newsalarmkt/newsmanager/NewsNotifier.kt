package com.alarm.newsalarmkt.newsmanager

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Pair
import com.alarm.newsalarmkt.BaseActivity
import com.alarm.newsalarmkt.database.AlarmData
import com.alarm.newsalarmkt.outputmanager.SoundPlayer
import com.alarm.newsalarmkt.outputmanager.TtsManager
import com.alarm.newsalarmkt.outputmanager.Vibrator
import com.alarm.newsalarmkt.utils.LogUtil
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class NewsNotifier(private val context: Context, private val data: AlarmData) {

    private val parser: JsonNewsParser = JsonNewsParser()
    private val crawler: NewsArticleCrawler = NewsArticleCrawler()
    private val soundPlayer: SoundPlayer = SoundPlayer(context)
    private val vibrator: Vibrator = Vibrator(context)
    private val queue: RequestQueue = Volley.newRequestQueue(context.applicationContext)
    private lateinit var ttsManager: TtsManager

    fun start() {
        ttsManager = TtsManager(context, data) {
            LogUtil.logI(CLASS_NAME, "start", "load news data started!")
            soundPlayer.playBgm()
            vibrator.vibrateRepeatedly(data.vibIntensity)

            val newsApiRequest = getRequest(data.alarmTopic)
            newsApiRequest.setShouldCache(false)
            queue.add(newsApiRequest)
            ttsManager.setTtsDoneListener {
                Handler(Looper.getMainLooper()).postDelayed({
                    (context as BaseActivity).finish()
                }, 5000)
            }
        }
    }

    private fun getRequest(keyword: String): StringRequest {
        val apiUrl = "https://openapi.naver.com/v1/search/news?query=$keyword&display=20"
        return object : StringRequest(
            Method.GET,
            apiUrl,
            Response.Listener { response -> this.processNewsData(response) },
            Response.ErrorListener { error -> this.notifyVolleyError(error) }) {

            override fun getHeaders(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["X-Naver-Client-Id"] = "77ZHNWgvaXOrqCOeo3s5"
                params["X-Naver-Client-Secret"] = "x_SI3JImaI"

                return params
            }
        }
    }

    private fun processNewsData(response: String) {
        val jsonArticleSet = parser.parseNaverNewsToJSONSet(response)
        if (isParsingFailed(jsonArticleSet)) {
            return
        }
        val articles = crawler.crawl(jsonArticleSet)
        if (isCrawlingFailed(articles)) {
            return
        }
        notifyNewsContents(articles.first, articles.second)
    }

    private fun isParsingFailed(jsonArticleSet: MutableSet<JSONObject>): Boolean {
        if (jsonArticleSet.isEmpty()) {
            notifyParseNewsDataFail()
            return true
        }
        return false
    }

    private fun notifyParseNewsDataFail() {
        LogUtil.logE(CLASS_NAME, "notifyParseNewsDataFail", "parse news data failed!")
        ttsManager.speak(
            "조건에 맞는 뉴스를 찾을 수 없습니다. 다른 키워드로 부탁드립니다.",
            TtsManager.Mode.ADD_SILENCE
        )
    }

    private fun isCrawlingFailed(articles: Pair<ArrayList<String>, ArrayList<String>>): Boolean {
        if (articles.first.isEmpty() || articles.second.isEmpty()) {
            notifyCrawlNewsDataFail()
            return true
        }
        return false
    }

    private fun notifyCrawlNewsDataFail() {
        LogUtil.logE(CLASS_NAME, "notifyCrawlNewsDataFail", "crawl news article failed!")
        ttsManager.speak(
            "서버 오류 혹은 다른 문제로 인해 뉴스 연결하지 못했습니다. 연결이 원활한 환경에서 시도바랍니다.",
            TtsManager.Mode.ADD_SILENCE
        )
    }

    private fun notifyNewsContents(titleList: ArrayList<String>, bodyList: ArrayList<String>) {
        LogUtil.logI(CLASS_NAME, "notifyNewsContents", "speak news articles started!")
        ttsManager.speakArticles(titleList, bodyList)
    }

    private fun notifyVolleyError(error: VolleyError) {
        LogUtil.logE(CLASS_NAME, "notifyVolleyError", "loading news data failed! : $error")
        ttsManager.speak(
            "뉴스를 검색하지 못했습니다. 연결을 확인해 보시거나, 다른 키워드로 부탁드립니다.",
            TtsManager.Mode.ADD_SILENCE
        )
    }

    fun finish() {
        crawler.release()
        soundPlayer.release()
        vibrator.release()
        ttsManager.release()
    }

    companion object {
        private const val CLASS_NAME = "NewsNotifier"
    }
}
