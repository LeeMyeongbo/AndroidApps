package com.alarm.newsalarm.newsmanager;

import static com.android.volley.Request.Method.GET;
import static com.alarm.newsalarm.outputmanager.TtsManager.Mode.*;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.alarm.newsalarm.BaseActivity;
import com.alarm.newsalarm.database.AlarmData;
import com.alarm.newsalarm.outputmanager.SoundPlayer;
import com.alarm.newsalarm.outputmanager.TtsManager;
import com.alarm.newsalarm.outputmanager.Vibrator;
import com.alarm.newsalarm.utils.LogUtil;
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

    private final Context context;
    private final JsonNewsParser parser;
    private final NewsArticleCrawler crawler;
    private final SoundPlayer soundPlayer;
    private final Vibrator vibrator;
    private final AlarmData data;
    private final RequestQueue queue;
    private TtsManager ttsManager;

    public NewsNotifier(Context context, AlarmData data) {
        this.context = context;
        this.data = data;
        parser = new JsonNewsParser();
        crawler = new NewsArticleCrawler();
        soundPlayer = new SoundPlayer(context);
        vibrator = new Vibrator(context);
        queue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public void start() {
        ttsManager = new TtsManager(context, data, () -> {
            LogUtil.logI(CLASS_NAME, "start", "load news data started!");
            soundPlayer.playBgm();
            vibrator.vibrateRepeatedly(data.getVibIntensity());

            StringRequest newsApiRequest = getRequest(data.getAlarmTopic());
            newsApiRequest.setShouldCache(false);
            queue.add(newsApiRequest);
            ttsManager.setTtsDoneListener(() -> new Handler(Looper.getMainLooper())
                .postDelayed(() -> ((BaseActivity) context).finish(), 5000));
        });
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
        return false;
    }

    private void notifyParseNewsDataFail() {
        LogUtil.logE(CLASS_NAME, "notifyParseNewsDataFail", "parse news data failed!");
        ttsManager.speak("조건에 맞는 뉴스를 찾을 수 없습니다. 다른 키워드로 부탁드립니다.", ADD_SILENCE);
    }

    private boolean isCrawlingFailed(Pair<ArrayList<String>, ArrayList<String>> articles) {
        if (articles.first.isEmpty() || articles.second.isEmpty()) {
            notifyCrawlNewsDataFail();
            return true;
        }
        return false;
    }

    private void notifyCrawlNewsDataFail() {
        LogUtil.logE(CLASS_NAME, "notifyCrawlNewsDataFail", "crawl news article failed!");
        ttsManager.speak("서버 오류 혹은 다른 문제로 인해 뉴스 연결하지 못했습니다. 연결이 원활한 환경에서 시도바랍니다.", ADD_SILENCE);
    }

    private void notifyNewsContents(ArrayList<String> titleList, ArrayList<String> bodyList) {
        LogUtil.logI(CLASS_NAME, "notifyNewsContents", "speak news articles started!");
        ttsManager.speakArticles(titleList, bodyList);
    }

    private void notifyVolleyError(VolleyError error) {
        LogUtil.logE(CLASS_NAME, "notifyVolleyError", "loading news data failed! : " + error);
        ttsManager.speak("뉴스를 검색하지 못했습니다. 연결을 확인해 보시거나, 다른 키워드로 부탁드립니다.", ADD_SILENCE);
    }

    public void finish() {
        crawler.release();
        soundPlayer.release();
        vibrator.release();
        ttsManager.release();
    }
}
