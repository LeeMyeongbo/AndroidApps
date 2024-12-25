package com.alarm.newsalarm.outputmanager;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;

import androidx.annotation.Nullable;

import com.alarm.newsalarm.database.AlarmData;

import java.util.ArrayList;
import java.util.Locale;

public class TtsManager {

    private static final String CLASS_NAME = "TtsManager";

    private final ArrayList<Runnable> speakList = new ArrayList<>();
    private AudioManager manager;
    private AudioAttributes attr;
    private TextToSpeech tts;
    private Voice maleVoice, femaleVoice;
    private boolean isTtsInitialized;
    private int originalVolume;

    public TtsManager(Context context, @Nullable AlarmData data) {
        storeOriginalVolume(context);
        initAudioAttributes();
        if (data == null) {
            initTts(context, 1.0f, "male");
            setVolumeSize(4);
            return;
        }
        initTts(context, data.getTempo(), "남성".equals(data.getGender()) ? "male" : "female");
        setVolumeSize(data.getVolumeSize());
    }

    private void storeOriginalVolume(Context context) {
        manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        originalVolume = manager.getStreamVolume(AudioManager.STREAM_ALARM);
    }

    private void initAudioAttributes() {
        attr = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build();
    }

    private void initTts(Context context, float tempo, String gender) {
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.ERROR) {
                return;
            }
            setTts(tempo, gender);
            speakList.forEach(Runnable::run);
            isTtsInitialized = true;
            Log.i(CLASS_NAME, "initTts$tts initialized successfully");
        });
    }

    private void setTts(float tempo, String gender) {
        tts.setLanguage(Locale.KOREAN);
        tts.setAudioAttributes(attr);
        prepareVoices();
        setVoiceTempo(tempo);
        setVoiceGender(gender);
    }

    private void prepareVoices() {
        femaleVoice = tts.getVoice();
        Log.i(CLASS_NAME, "prepareVoices$female voice(default voice) : " + femaleVoice.getName());

        for (Voice voice : tts.getVoices()) {
            if (voice.getName().contains("KR")
                    && voice.getFeatures().parallelStream().anyMatch(s -> s.contains("=male"))) {
                maleVoice = voice;
                Log.i(CLASS_NAME, "prepareVoices$male voice : " + maleVoice.getName());
                return;
            }
        }
    }

    public void setVoiceTempo(float tempo) {
        tts.setSpeechRate(tempo);
        Log.i(CLASS_NAME, "setVoiceTempo$voice tempo : " + tempo);
    }

    public void setVoiceGender(String gender) {
        switch (gender) {
            case "female" -> {
                tts.setVoice(femaleVoice);
                Log.i(CLASS_NAME, "setVoiceGender$voice set to female");
            }
            case "male" -> {
                tts.setVoice(maleVoice);
                Log.i(CLASS_NAME, "setVoiceGender$voice set to male");
            }
            default -> Log.i(CLASS_NAME, "setVoiceGender$wrong gender parameter!!");
        }
    }

    public void setVolumeSize(int volumeSize) {
        manager.setStreamVolume(AudioManager.STREAM_ALARM, volumeSize, 0);
        Log.i(CLASS_NAME, "setVolumeSize$set volume : " + volumeSize);
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

    public void speak(String txt, int mode, int queueMode) {
        if (isTtsInitialized) {
            speakWithTts(txt, mode, queueMode);
        } else {
            speakList.add(() -> speakWithTts(txt, mode, queueMode));
        }
    }

    private void speakWithTts(String txt, int mode) {
        if (mode == 1) {
            tts.playSilentUtterance(800L, TextToSpeech.QUEUE_ADD, "silence");
        }
        tts.speak(txt, TextToSpeech.QUEUE_ADD, null, "speak");
    }

    private void speakWithTts(String txt, int mode, int queueMode) {
        if (mode == 1) {
            tts.playSilentUtterance(800L, queueMode, "silence");
        }
        tts.speak(txt, queueMode, null, "speak");
    }

    public void release() {
        manager.setStreamVolume(AudioManager.STREAM_ALARM, originalVolume, 0);
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        Log.i(CLASS_NAME, "release$TtsManager released completely!");
    }
}
