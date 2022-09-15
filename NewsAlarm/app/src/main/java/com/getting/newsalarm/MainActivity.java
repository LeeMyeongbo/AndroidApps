package com.getting.newsalarm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Button;

import android.speech.tts.TextToSpeech;
import android.widget.EditText;

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
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private TextToSpeech tts;
    private RequestQueue queue;
    private EditText keywordText;
    private MediaPlayer mediaPlayer;
    private Bundle bundle;
    private Handler handler;
    private int news_num;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button readButton = findViewById(R.id.readButton);
        readButton.setOnClickListener((e) -> getNews());
        Button stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener((e) -> destroyTTS());

        bundle = new Bundle();
        keywordText = findViewById(R.id.keywordText);
        queue = Volley.newRequestQueue(this);      // requestQueue 생성

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                news_num++;
                String title = msg.getData().getString("title");
                String[] contents = msg.getData().getStringArray("content");

                speakTTS(news_num + "번 뉴스입니다.", "announce", 1);
                speakTTS(title, "title", 1);
                speakTTS("기사 내용입니다.", "contentReady", 1);
                for (String content : contents)
                    speakTTS(content, "content", 0);
            }
        };
    }

    public void getNews() {
        createTTS();

        String keyword = keywordText.getText().toString();
        String url = "https://openapi.naver.com/v1/search/news?query=" + keyword + "&display=20";

        // 해당 url 로부터 뉴스기사(Json 형태) 응답 요청
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
            response -> {       // 성공적으로 응답 받아왔을 경우
                // 배경음악 재생!
                mediaPlayer = MediaPlayer.create(this, R.raw.morningkiss);
                mediaPlayer.setVolume(0.1f, 0.1f);
                mediaPlayer.setLooping(true);
                mediaPlayer.start();

                speakTTS("안녕하세요? 오늘의 뉴스를 알려드리겠습니다.", "start", 1);
                // Json 형식이 아닌 데이터를 처리할 수도 있으므로 예외 처리 필요
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray arrayArticles = jsonObject.getJSONArray("items");
                    int length = arrayArticles.length();
                    Set<JSONObject> jsonObjects = new HashSet<>();

                    // 같은 기사가 중복되는 경우도 있어서 HashSet 으로 중복 제거 + 네이버 뉴스 기사만 들고 옴(한 5개 까지)
                    for (int i = 0; i < length; i++) {
                        JSONObject articleObject = arrayArticles.getJSONObject(i);
                        if (articleObject.getString("link").contains("news.naver.com"))
                            jsonObjects.add(articleObject);
                        if (jsonObjects.size() == 5) break;
                    }

                    new Thread() {
                        @Override
                        public void run() {
                            for (JSONObject j : jsonObjects) {
                                try {
                                    // json 형식의 기사에서 "title"에 해당하는 부분 가지고 오면서 [], (), <>, &;로 싸여져 있는 부분 제거
                                    String title = j.getString("title");
                                    title = title.replaceAll("([\\[(<&](.*?)[;>)\\]])", "");

                                    // "link"에 해당하는 부분도 가져와서 html 파싱, 기사 부분만 크롤링해 옴
                                    String link = j.getString("link");
                                    Document doc = Jsoup.connect(link).get();
                                    Element e = doc.selectFirst("#dic_area");
                                    if (e == null)
                                        e = doc.selectFirst("#articeBody");
                                    if (e == null)
                                        e = doc.selectFirst("#newsEndContents");

                                    int num = Objects.requireNonNull(e).childrenSize();

                                    // 자식 요소 중 태그가 <font>, <span>, <b>가 아니거나 class="end_photo_org"인 경우 모두 제거
                                    for (int i = num - 1; i >= 0; i--) {
                                        String tag = e.child(i).tagName();
                                        if ((!tag.equals("font") && !tag.equals("span") && !tag.equals("b"))
                                            || e.child(i).className().equals("end_photo_org"))
                                            e.child(i).remove();
                                    }

                                    // 기사 본문에서 [], (), <>, &;로 싸여져 있는 부분 제거
                                    String content = e.text().replaceAll("([\\[(<&](.*?)[;>)\\]])", "");

                                    // #, ※, ▶, ⓒ 등의 특수문자가 오면 그 뒤의 내용은 필요없어서 잘라냄
                                    int idx = content.indexOf('#');
                                    if (idx == -1)
                                        idx = content.indexOf('※');
                                    if (idx == -1)
                                        idx = content.indexOf('▶');
                                    if (idx == -1)
                                        idx = content.indexOf('ⓒ');
                                    if (idx != -1)
                                        content = content.substring(0, idx);

                                    // handler 로 제목 전송
                                    bundle.putString("title", title);

                                    // 본문도 전송하는데 4000자 넘어가면 음성 출력 안되니까 1000자 단위로 끊어서 전송
                                    int send = content.length() / 1000;
                                    String[] contents = new String[send + 1];
                                    for (int i = 0; i <= send; i++) {
                                        int l = Integer.min(1000 * i + 1000, content.length());
                                        contents[i] = content.substring(i * 1000, l);
                                    }
                                    bundle.putStringArray("content", contents);
                                    Message msg = handler.obtainMessage();
                                    msg.setData(bundle);
                                    handler.sendMessage(msg);
                                } catch (IOException | JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }.start();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> {       // 에러났을 경우
                speakTTS("서버 통신 오류가 발생했습니다.", "error", 0);
            }
        ) {
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

    public void createTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.KOREAN);
            }
        });
        setVolumeControlStream(AudioManager.STREAM_ALARM);
        tts.setPitch(1.0f);
        tts.setSpeechRate(0.85f);
    }

    public void speakTTS(String txt, String utterance, int mode) {
        if (mode == 1)
            tts.playSilentUtterance(800L, TextToSpeech.QUEUE_ADD, "silence");
        tts.speak(txt, TextToSpeech.QUEUE_ADD, null, utterance);
    }

    public void destroyTTS() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;

            mediaPlayer.stop();
            mediaPlayer.release();
            news_num = 0;
        }
    }

    @Override
    protected void onDestroy() {
        destroyTTS();
        super.onDestroy();
    }
}
