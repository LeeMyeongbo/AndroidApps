package com.alarm.newsalarm.newsmanager;

import static com.android.volley.Request.Method.GET;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.alarm.newsalarm.database.AlarmData;
import com.alarm.newsalarm.outputmanager.SoundPlayer;
import com.alarm.newsalarm.outputmanager.TtsManager;
import com.alarm.newsalarm.outputmanager.Vibrator;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NewsNotifier {

    private static final String CLASS_NAME = "NewsNotifier";

    private final JsonNewsParser parser;
    private final NewsArticleCrawler crawler;
    private final SoundPlayer soundPlayer;
    private final Vibrator vibrator;
    private final TtsManager ttsManager;
    private final AlarmData data;
    private final RequestQueue queue;

    public NewsNotifier(Context context, AlarmData data) {
        this.data = data;
        parser = new JsonNewsParser();
        crawler = new NewsArticleCrawler();
        soundPlayer = new SoundPlayer(context);
        vibrator = new Vibrator(context);
        ttsManager = new TtsManager(context, data);
        queue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public void start() {
        Log.i(CLASS_NAME, "start$loading news data started!");
        soundPlayer.playBgm(data.getVolumeSize());
        vibrator.vibrateRepeatedly(data.getVibIntensity());

        StringRequest newsApiRequest = getRequest(data.getAlarmTopic());
        newsApiRequest.setShouldCache(false);
        queue.add(newsApiRequest);
    }

    @NonNull
    private StringRequest getRequest(String keyword) {
        String apiUrl = "https://openapi.naver.com/v1/search/news?query=" + keyword + "&display=20";
        return new StringRequest(GET, apiUrl, this::processNewsData, this::notifyVolleyError) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("X-Naver-Client-Id", "77ZHNWgvaXOrqCOeo3s5");
                params.put("X-Naver-Client-Secret", "x_SI3JImaI");

                return params;
            }
        };
    }

    private void processNewsData(String response) {
        Set<JSONObject> jsonArticleSet = parser.parseNaverNewsToJSONSet(response);
        if (isParsingFailed(jsonArticleSet)) {
            return;
        }
        Pair<ArrayList<String>, ArrayList<String>> articles = crawler.crawl(jsonArticleSet);
        if (isCrawlingFailed(articles)) {
            return;
        }
        notifyNewsContents(articles.first, articles.second);
    }

    private boolean isParsingFailed(Set<JSONObject> jsonArticleSet) {
        if (jsonArticleSet.isEmpty()) {
            notifyParseNewsDataFail();
            return true;
        }
        Log.i(CLASS_NAME, "isParsingFailed$succeeded -> loading news data completed!");
        return false;
    }

    private void notifyParseNewsDataFail() {
        Log.e(CLASS_NAME, "notifyParseNewsDataFail$parsing news data failed!");
        ttsManager.speak("조건에 맞는 뉴스를 찾을 수 없습니다. 다른 키워드로 부탁드립니다.", 1);
    }

    private boolean isCrawlingFailed(Pair<ArrayList<String>, ArrayList<String>> articles) {
        if (articles.first.isEmpty() || articles.second.isEmpty()) {
            notifyCrawlNewsDataFail();
            return true;
        }
        Log.i(CLASS_NAME, "isCrawlingFailed$succeeded -> crawling news article completed!");
        return false;
    }

    private void notifyCrawlNewsDataFail() {
        Log.e(CLASS_NAME, "notifyCrawlNewsDataFail$crawl news article failed!");
        ttsManager.speak("서버 오류 혹은 다른 문제로 인해 뉴스 본문에 연결하지 못했습니다. 연결이 원활한 환경에서 시도바랍니다.", 1);
    }

    private void notifyNewsContents(ArrayList<String> titleList, ArrayList<String> bodyList) {
        Log.i(CLASS_NAME, "notifyNewsContents$speaking news articles just started!");
        ttsManager.speakArticles(titleList, bodyList);
    }

    private void notifyVolleyError(VolleyError error) {
        Log.e(CLASS_NAME, "notifyVolleyError$loading news data failed! : " + error);
        ttsManager.speak("뉴스를 검색하지 못했습니다. 연결을 확인해 보시거나, 다른 키워드로 부탁드립니다.", 1);
    }

    public void finish() {
        crawler.release();
        soundPlayer.release();
        vibrator.release();
        ttsManager.release();
    }
}
