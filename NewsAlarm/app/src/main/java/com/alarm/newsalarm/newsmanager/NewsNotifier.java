package com.alarm.newsalarm.newsmanager;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alarm.newsalarm.database.AlarmData;
import com.alarm.newsalarm.outputmanager.SoundPlayer;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class NewsNotifier {

    private static final String CLASS_NAME = "NewsNotifier";
    private static final NewsNotifier INSTANCE = new NewsNotifier();

    private SoundPlayer soundPlayer;
    private AlarmData data;
    private TextToSpeech tts;
    private RequestQueue queue;
    private Bundle bundle;
    private final Handler handler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(@NonNull Message msg) {
            news_num++;
            String title = msg.getData().getString("title");
            String[] contents = msg.getData().getStringArray("content");

            speakTTS(news_num + "번 뉴스입니다.", 1);
            speakTTS(title, 1);
            speakTTS("기사 내용입니다.", 1);
            assert contents != null;
            for (String content : contents) {
                speakTTS(content, 0);
            }
        }
    };
    private int news_num;

    public static NewsNotifier getInstance() {
        return INSTANCE;
    }

    public void notifyNews(Context context, AlarmData data) {
        this.data = data;
        soundPlayer = new SoundPlayer(context);
        bundle = new Bundle();
        queue = Volley.newRequestQueue(context.getApplicationContext());
        getNews(context);
    }

    public void getNews(Context context) {
        createTTS(context);

        String keyword = "코로나";
        String url = "https://openapi.naver.com/v1/search/news?query=" + keyword + "&display=20";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            soundPlayer.playBgm(data.getVolumeSize());
            speakTTS("안녕하세요? 오늘의 뉴스를 알려드리겠습니다.", 1);

            Set<JSONObject> jsonObjects = convertToJSONSet(response);
            new Thread(() -> {
                for (JSONObject j : jsonObjects) {
                    bundle.putString("title", getArticleTitle(j));
                    bundle.putStringArray("content", getArticleBody(j));
                    Message msg = handler.obtainMessage();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            }).start();
        }, error -> speakTTS("서버 통신 오류가 발생했습니다.", 0)) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("X-Naver-Client-Id", "77ZHNWgvaXOrqCOeo3s5");
                params.put("X-Naver-Client-Secret", "x_SI3JImaI");

                return params;
            }
        };
        stringRequest.setShouldCache(false);
        queue.add(stringRequest);
    }

    public Set<JSONObject> convertToJSONSet(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray arrayArticles = jsonObject.getJSONArray("items");
            int length = arrayArticles.length();
            Set<JSONObject> jsonObjects = new HashSet<>();

            for (int i = 0; i < length; i++) {
                JSONObject articleObject = arrayArticles.getJSONObject(i);
                if (articleObject.getString("link").contains("news.naver.com")) {
                    jsonObjects.add(articleObject);
                }
                if (jsonObjects.size() == 5) {
                    Log.i(CLASS_NAME, "convertToJSONSet$JSON objects with news contents ready");
                    break;
                }
            }

            return jsonObjects;
        } catch (JSONException e) {
            Log.e(CLASS_NAME, "convertToJSONSet$" + e.getMessage());
        }

        return new HashSet<>();
    }

    public String getArticleTitle(JSONObject j) {
        try {
            return j.getString("title").replaceAll("([\\[(<&](.*?)[;>)\\]])", "");
        } catch (JSONException | NullPointerException e) {
            Log.e(CLASS_NAME, "getArticleTitle$" + e.getMessage());
        }

        return "";
    }

    public String[] getArticleBody(JSONObject j) {
        try {
            String content = modifyContent(crawlPassage(j.getString("link")));
            int line = 1000;
            int send = content.length() / line;
            String[] contents = new String[send + 1];
            for (int i = 0; i <= send; i++) {
                int l = Integer.min(line * i + line, content.length());
                contents[i] = content.substring(i * line, l);
            }

            return contents;
        } catch (JSONException | NullPointerException e) {
            Log.e(CLASS_NAME, "getArticleBody$" + e.getMessage());
        }

        return new String[0];
    }

    public Element crawlPassage(String link) {
        try {
            Document doc = Jsoup.connect(link).get();
            Element e = doc.selectFirst("#dic_area");
            if (e == null)
                e = doc.selectFirst("#articeBody");
            if (e == null)
                e = doc.selectFirst("#newsEndContents");

            int num = e.childrenSize();
            for (int i = num - 1; i >= 0; i--) {
                String tag = e.child(i).tagName();
                if ((!tag.equals("font") && !tag.equals("span") && !tag.equals("b"))
                        || e.child(i).className().equals("end_photo_org")) {
                    e.child(i).remove();
                }
            }

            return e;
        } catch (IOException | NullPointerException e) {
            Log.e(CLASS_NAME, "crawlPassage$" + e.getMessage());
        }

        return null;
    }

    public String modifyContent(Element e) {
        if (e == null) {
            return "";
        }

        String content = e.text().replaceAll("([\\[(<&](.*?)[;>)\\]])", "");
        int idx = content.indexOf('#');
        if (idx == -1)
            idx = content.indexOf('※');
        if (idx == -1)
            idx = content.indexOf('▶');
        if (idx == -1)
            idx = content.indexOf('ⓒ');
        if (idx != -1)
            content = content.substring(0, idx);

        return content;
    }

    public void createTTS(Context context) {
        tts = new TextToSpeech(context, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.KOREAN);
            }
        });
        tts.setPitch(1.0f);
        tts.setSpeechRate(0.85f);
    }

    public void speakTTS(String txt, int mode) {
        HashMap<String, String> myHashAlarm = new HashMap<>();
        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));
        if (mode == 1) {
            tts.playSilentUtterance(800L, TextToSpeech.QUEUE_ADD, "silence");
        }
        tts.speak(txt, TextToSpeech.QUEUE_ADD, myHashAlarm);
    }

    public void destroyTTS() {
        Log.i(CLASS_NAME, "destroyTTS$alarm off");
        if (tts != null) {
            soundPlayer.release();
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }
}
