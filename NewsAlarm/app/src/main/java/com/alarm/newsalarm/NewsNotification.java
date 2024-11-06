package com.alarm.newsalarm;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.tts.TextToSpeech;

import androidx.annotation.NonNull;

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

public class NewsNotification {

    private static final NewsNotification news = new NewsNotification();

    private TextToSpeech tts;
    private RequestQueue queue;
    private MediaPlayer mp;
    private Bundle bundle;
    private Handler handler;
    private int news_num;

    public static NewsNotification getInstance() {
        return news;
    }

    public void notifyNews(Context context) {
        bundle = new Bundle();
        queue = Volley.newRequestQueue(context.getApplicationContext());
        handler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(@NonNull Message msg) {
                news_num++;
                String title = msg.getData().getString("title");
                String[] contents = msg.getData().getStringArray("content");

                speakTTS(news_num + "번 뉴스입니다.", 1);
                speakTTS(title, 1);
                speakTTS("기사 내용입니다.", 1);
                for (String content : contents)
                    speakTTS(content, 0);
            }
        };
        getNews(context);
    }

    public void getNews(Context context) {
        createTTS(context);

        String keyword = "코로나";
        String url = "https://openapi.naver.com/v1/search/news?query=" + keyword + "&display=20";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            playBackgroundMusic(context);
            speakTTS("안녕하세요? 오늘의 뉴스를 알려드리겠습니다.", 1);

            Set<JSONObject> jsonObjects = convert2JSONSet(response);
            new Thread(() -> {
                for (JSONObject j : jsonObjects) {
                    String title = getArticleTitle(j);
                    String[] contents = getArticleBody(j);

                    if (title != null && contents != null) {
                        bundle.putString("title", title);
                        bundle.putStringArray("content", contents);
                        Message msg = handler.obtainMessage();
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }
                }
            }).start();
        }, error -> {
            speakTTS("서버 통신 오류가 발생했습니다.", 0);
        }) {
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

    public void playBackgroundMusic(Context context) {
        mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_ALARM);
        try {
            mp.setDataSource(context, Uri.parse("android.resource://" + context.getPackageName()
                + "/" + R.raw.morningkiss));
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mp.setVolume(0.1f, 0.1f);
        mp.setLooping(true);
        mp.start();
    }

    public Set<JSONObject> convert2JSONSet(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray arrayArticles = jsonObject.getJSONArray("items");
            int length = arrayArticles.length();
            Set<JSONObject> jsonObjects = new HashSet<>();

            for (int i = 0; i < length; i++) {
                JSONObject articleObject = arrayArticles.getJSONObject(i);
                if (articleObject.getString("link").contains("news.naver.com"))
                    jsonObjects.add(articleObject);
                if (jsonObjects.size() == 5) break;
            }

            return jsonObjects;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new HashSet<>();
    }

    public String getArticleTitle(JSONObject j) {
        try {
            return j.getString("title").replaceAll("([\\[(<&](.*?)[;>)\\]])", "");
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        }

        return null;
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
            e.printStackTrace();
        }

        return null;
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
                    || e.child(i).className().equals("end_photo_org"))
                    e.child(i).remove();
            }

            return e;
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String modifyContent(Element e) {
        if (e == null) return null;
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
        if (mode == 1)
            tts.playSilentUtterance(800L, TextToSpeech.QUEUE_ADD, "silence");
        tts.speak(txt, TextToSpeech.QUEUE_ADD, myHashAlarm);
    }

    public void destroyTTS() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;

            mp.stop();
            mp.release();
            news_num = 0;
        }
    }
}
