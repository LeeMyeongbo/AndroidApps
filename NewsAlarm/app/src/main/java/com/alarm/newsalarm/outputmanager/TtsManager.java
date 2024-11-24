package com.alarm.newsalarm.outputmanager;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.alarm.newsalarm.database.AlarmData;

import java.util.ArrayList;
import java.util.Locale;

public class TtsManager {

    private static final String CLASS_NAME = "TtsManager";

    private final AlarmData data;
    private AudioAttributes attr;
    private TextToSpeech tts;

    public TtsManager(Context context, AlarmData data) {
        initAudioAttributes();
        initTts(context);
        this.data = data;
    }

    private void initAudioAttributes() {
        attr = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setLegacyStreamType(AudioManager.STREAM_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
            .build();
    }

    private void initTts(Context context) {
        tts = new TextToSpeech(context, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.KOREAN);
            }
        });
        tts.setAudioAttributes(attr);
        tts.setPitch(1.0f);
        tts.setSpeechRate(0.85f);
    }

    public void speakArticles(ArrayList<String> titleList, ArrayList<String> bodyList) {
        speak("안녕하세요? 오늘의 뉴스를 알려드리겠습니다.", 1);
        int curSeq = 1;
        for (String title : titleList) {
            speak(curSeq + "번 뉴스입니다.", 1);
            speak(title, 1);
            speak("기사 내용입니다.", 1);
            speakArticleBody(bodyList);
        }
    }

    private void speakArticleBody(ArrayList<String> bodyList) {
        for (String body : bodyList) {
            int maxLenPerSpeech = TextToSpeech.getMaxSpeechInputLength() / 4;
            int num = body.length() / maxLenPerSpeech;
            for (int i = 0; i <= num; i++) {
                int l = Integer.min(maxLenPerSpeech * i + maxLenPerSpeech, body.length());
                speak(body.substring(i * maxLenPerSpeech, l), 0);
            }
        }
    }

    public void speak(String txt, int mode) {
        if (mode == 1) {
            tts.playSilentUtterance(800L, TextToSpeech.QUEUE_ADD, "silence");
        }
        tts.speak(txt, TextToSpeech.QUEUE_ADD, null, "speak");
    }

    public void release() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        Log.i(CLASS_NAME, "release$TtsManager released completely!");
    }
}
