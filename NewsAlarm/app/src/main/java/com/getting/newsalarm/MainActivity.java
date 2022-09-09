package com.getting.newsalarm;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private TextToSpeech tts;
    private RequestQueue queue;
    private EditText keywordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button readButton = findViewById(R.id.readButton);
        readButton.setOnClickListener((e) -> getNews());
        Button stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener((e) -> destroyTTS());

        keywordText = findViewById(R.id.keywordText);
        queue = Volley.newRequestQueue(this);      // requestQueue 생성
    }

    public void getNews() {
        createTTS();

        String keyword = keywordText.getText().toString();
        String url = "https://openapi.naver.com/v1/search/news?query=" + keyword + "&display=15";

        // 해당 url 로부터 뉴스기사(Json 형태) 응답 요청
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
            response -> {       // 성공적으로 응답 받아왔을 경우
                StringBuilder printOut = new StringBuilder();

                // Json 형식이 아닌 데이터를 처리할 수도 있으므로 예외 처리 필요
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray arrayArticles = jsonObject.getJSONArray("items");
                    int length = arrayArticles.length();

                    for (int i = 0; i < length; i++) {
                        JSONObject articleObject = arrayArticles.getJSONObject(i);

                        // json 형식의 기사에서 "title"에 해당하는 부분 가지고 옴
                        String title = articleObject.getString("title").replaceAll("([(<&](.*?)[;>)])", "");
                        printOut.append(i + 1).append("번 뉴스기사입니다. ").append(title).append(".");
                        Log.d("News", title + '\n');

                        // "description"에 해당하는 부분도 가지고 옴
                        String des = articleObject.getString("description").replaceAll("([(<&](.*?)[;>)])", "");
                        printOut.append("기사 내용입니다. ").append(des).append("...");
                        Log.d("News", des + '\n');
                    }
                    tts.speak(printOut, TextToSpeech.QUEUE_FLUSH, null, "articles");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> {       // 에러났을 경우
                Log.e("error", error.getMessage(), error);
                tts.speak("서버 통신 오류가 발생했습니다.", TextToSpeech.QUEUE_FLUSH, null, "error");
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
        tts.setPitch(0.95f);
        tts.setSpeechRate(0.85f);
    }

    public void destroyTTS() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

    @Override
    protected void onDestroy() {
        destroyTTS();
        super.onDestroy();
    }
}
