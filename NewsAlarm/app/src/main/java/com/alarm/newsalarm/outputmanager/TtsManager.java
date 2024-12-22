package com.alarm.newsalarm.outputmanager;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.alarm.newsalarm.database.AlarmData;

import java.util.ArrayList;
import java.util.Locale;

public class TtsManager {

    private static final String CLASS_NAME = "TtsManager";

    private final ArrayList<Runnable> speakList = new ArrayList<>();
    private final Bundle bundle = new Bundle();
    private final AlarmData data;
    private AudioAttributes attr;
    private TextToSpeech tts;
    private boolean isTtsInitialized;

    public TtsManager(Context context, AlarmData data) {
        this.data = data;
        bundle.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1f);
        initAudioAttributes();
        initTts(context);
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
            if (status == TextToSpeech.ERROR) {
                return;
            }
            tts.setLanguage(Locale.KOREAN);
            tts.setAudioAttributes(attr);
            tts.setPitch(1.0f);
            tts.setSpeechRate(data.getTempo());
            speakList.forEach(Runnable::run);
            isTtsInitialized = true;
            Log.i(CLASS_NAME, "initTts$tts initialized successfully");
        });
    }

    public void speakArticles(ArrayList<String> titleList, ArrayList<String> bodyList) {
        speak("안녕하세요? 오늘의 뉴스를 알려드리겠습니다.", 1);
        for (int i = 0; i < titleList.size(); i++) {
            speak((i + 1) + "번 뉴스입니다.", 1);
            speak(titleList.get(i), 1);
            speak("기사 내용입니다.", 1);
            Log.i(CLASS_NAME, "speakArticles$queuing article title index : " + i);
            speakArticleBody(bodyList, i);
        }
    }

    private void speakArticleBody(ArrayList<String> bodyList, int cur) {
        String body = bodyList.get(cur);
        int maxLenPerSpeech = TextToSpeech.getMaxSpeechInputLength() / 4;
        int num = body.length() / maxLenPerSpeech;
        for (int i = 0; i <= num; i++) {
            int l = Integer.min(maxLenPerSpeech * i + maxLenPerSpeech, body.length());
            speak(body.substring(i * maxLenPerSpeech, l), 0);
        }
        Log.i(CLASS_NAME, "speakArticleBody$queuing article body index : " + cur);
    }

    public void speak(String txt, int mode) {
        if (isTtsInitialized) {
            speakWithTts(txt, mode);
        } else {
            speakList.add(() -> speakWithTts(txt, mode));
        }
    }

    private void speakWithTts(String txt, int mode) {
        if (mode == 1) {
            tts.playSilentUtterance(800L, TextToSpeech.QUEUE_ADD, "silence");
        }
        tts.speak(txt, TextToSpeech.QUEUE_ADD, bundle, "speak");
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
