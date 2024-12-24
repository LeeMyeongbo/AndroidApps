package com.alarm.newsalarm.outputmanager;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;

import com.alarm.newsalarm.database.AlarmData;

import java.util.ArrayList;
import java.util.Locale;

public class TtsManager {

    private static final String CLASS_NAME = "TtsManager";

    private final ArrayList<Runnable> speakList = new ArrayList<>();
    private final AlarmData data;
    private AudioAttributes attr;
    private TextToSpeech tts;
    private boolean isTtsInitialized;

    public TtsManager(Context context, AlarmData data) {
        this.data = data;
        initAudioAttributes();
        initTts(context);
        setVolumeSize(context, data);
    }

    private void setVolumeSize(Context context, AlarmData data) {
        int volumeSize = data.getVolumeSize();
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (manager.getStreamVolume(AudioManager.STREAM_ALARM) != volumeSize) {
            manager.setStreamVolume(AudioManager.STREAM_ALARM, volumeSize, 0);
        }
        Log.i(CLASS_NAME, "setVolumeSize$set volume : " + volumeSize);
    }

    private void initAudioAttributes() {
        attr = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build();
    }

    private void initTts(Context context) {
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.ERROR) {
                return;
            }
            setTts();
            speakList.forEach(Runnable::run);
            isTtsInitialized = true;
            Log.i(CLASS_NAME, "initTts$tts initialized successfully");
        });
    }

    private void setTts() {
        tts.setLanguage(Locale.KOREAN);
        tts.setAudioAttributes(attr);
        tts.setSpeechRate(data.getTempo());
        setVoice();
    }

    private void setVoice() {
        if ("여성".equals(data.getGender())) {
            Log.i(CLASS_NAME, "setVoice$voice is basically set to female... using default voice");
            return;
        }
        for (Voice voice : tts.getVoices()) {
            if (voice.getName().contains("KR")
                    && voice.getFeatures().parallelStream().anyMatch(s -> s.contains("=male"))) {
                tts.setVoice(voice);
                Log.i(CLASS_NAME, "setVoice$voice set to male... using " + voice.getName());
                return;
            }
        }
    }

    public void speakArticles(ArrayList<String> titleList, ArrayList<String> bodyList) {
        speak("안녕하세요? 오늘의 뉴스를 알려드리겠습니다.", 1);
        for (int i = 0; i < titleList.size(); i++) {
            Log.i(CLASS_NAME, "speakArticles$queuing article title index : " + i);
            speak((i + 1) + "번 뉴스입니다.", 1);
            speak(titleList.get(i), 1);
            speak("기사 내용입니다.", 1);
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
