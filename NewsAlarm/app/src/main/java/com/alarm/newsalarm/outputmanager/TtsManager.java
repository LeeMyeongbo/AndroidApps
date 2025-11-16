package com.alarm.newsalarm.outputmanager;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;

import androidx.annotation.NonNull;

import com.alarm.newsalarm.database.AlarmData;
import com.alarm.newsalarm.utils.LogUtil;

import java.util.ArrayList;
import java.util.Locale;

public class TtsManager {

    public enum Mode {
        DEFAULT, ADD_SILENCE
    }

    public interface OnInitListener {
        void onInitialized();
    }

    public interface OnTtsAllDoneListener {
        void onSpeechAllDone();
    }

    private static final String CLASS_NAME = "TtsManager";

    private final OnInitListener initListener;
    private final ArrayList<Runnable> speakList = new ArrayList<>();
    private final ArrayList<Voice> voiceList = new ArrayList<>();
    private AudioManager manager;
    private AudioAttributes attr;
    private TextToSpeech tts;
    private OnTtsAllDoneListener doneListener;
    private boolean isTtsInitialized;
    private int originalVolume;
    private int pendingSpeechCnt;

    public TtsManager(@NonNull Context context, AlarmData data, OnInitListener initListener) {
        this.initListener = initListener;
        storeOriginalVolume(context);
        initAudioAttributes();
        if (data == null) {
            initTts(context, 0, 4, 1.0f, 1.0f);
            return;
        }
        initTts(context, data.getVoiceIdx(), data.getVolumeSize(), data.getTempo(), data.getPitch());
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

    private void initTts(Context context, int idx, int volumeSize, float tempo, float pitch) {
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.ERROR) {
                LogUtil.logE(CLASS_NAME, "OnInitListener.onInit",
                    "tts initialization failed - status " + status);
                return;
            }

            setTts(idx, volumeSize, tempo, pitch);
            if (initListener != null) {
                initListener.onInitialized();
            }
            speakList.forEach(Runnable::run);
            isTtsInitialized = true;
            LogUtil.logI(CLASS_NAME, "OnInitListener.onInit", "tts initialized successfully!");
        });
    }

    private void setTts(int idx, int volumeSize, float tempo, float pitch) {
        prepareVoices();
        setSpecificVoice(idx);
        setVolumeSize(volumeSize);
        setVoicePitch(pitch);
        setVoiceTempo(tempo);
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {

            private int speechCnt = 0;

            @Override
            public void onStart(String s) {
            }

            @Override
            public void onDone(String s) {
                speechCnt++;
                if (speechCnt == pendingSpeechCnt && doneListener != null) {
                    LogUtil.logD(CLASS_NAME, "onDone", "all speech done");
                    doneListener.onSpeechAllDone();
                }
            }

            @Override
            public void onError(String s) {
            }
        });
    }

    private void prepareVoices() {
        tts.setLanguage(Locale.KOREAN);
        tts.setAudioAttributes(attr);
        for (Voice voice : tts.getVoices()) {
            String name = voice.getName().toLowerCase(Locale.ROOT);
            if (name.contains("ko-kr") && name.contains("local")) {
                voiceList.add(voice);
                LogUtil.logD(CLASS_NAME, "prepareVoices", "found voice : " + voice.getName());
            }
        }
        if (voiceList.isEmpty()) {
            tts.setVoice(tts.getDefaultVoice());
            LogUtil.logD(CLASS_NAME, "prepareVoices", "no local voice found - add default voice "
                + tts.getDefaultVoice().getName());
        }
    }

    public void setSpecificVoice(int idx) {
        if (idx < 0 || idx >= voiceList.size()) {
            LogUtil.logE(CLASS_NAME, "setSpecificVoice", "invalid voice index : " + idx);
            tts.setVoice(tts.getDefaultVoice());
            return;
        }

        Voice voice = voiceList.get(idx);
        tts.setVoice(voice);
        LogUtil.logD(CLASS_NAME, "setSpecificVoice", "voice index : " + idx);
        LogUtil.logD(CLASS_NAME, "setSpecificVoice", "voice info : " + voice);
    }

    public void setVolumeSize(int volumeSize) {
        manager.setStreamVolume(AudioManager.STREAM_ALARM, volumeSize, 0);
        LogUtil.logD(CLASS_NAME, "setVolumeSize", "set volume : " + volumeSize);
    }

    public void setVoicePitch(float pitch) {
        tts.setPitch(pitch);
        LogUtil.logD(CLASS_NAME, "setVoicePitch", "set pitch : " + pitch);
    }

    public void setVoiceTempo(float tempo) {
        tts.setSpeechRate(tempo);
        LogUtil.logD(CLASS_NAME, "setVoiceTempo", "set tempo : " + tempo);
    }

    public int getAvailableVoiceNum() {
        return voiceList.size();
    }

    public void setTtsDoneListener(OnTtsAllDoneListener listener) {
        doneListener = listener;
    }

    public void speakArticles(ArrayList<String> titleList, ArrayList<String> bodyList) {
        speak("안녕하세요? 오늘의 뉴스를 알려드리겠습니다.", Mode.ADD_SILENCE);
        for (int i = 0; i < titleList.size(); i++) {
            speak((i + 1) + "번 뉴스입니다.", Mode.ADD_SILENCE);
            speak(titleList.get(i), Mode.ADD_SILENCE);
            speak("기사 내용입니다.", Mode.ADD_SILENCE);
            speakArticleBody(bodyList, i);
        }
    }

    private void speakArticleBody(ArrayList<String> bodyList, int cur) {
        String body = bodyList.get(cur);
        int maxLenPerSpeech = TextToSpeech.getMaxSpeechInputLength() / 4;
        int num = body.length() / maxLenPerSpeech;
        for (int i = 0; i <= num; i++) {
            int l = Integer.min(maxLenPerSpeech * i + maxLenPerSpeech, body.length());
            speak(body.substring(i * maxLenPerSpeech, l), Mode.DEFAULT);
        }
    }

    public void speak(String txt, Mode mode) {
        speak(txt, mode, TextToSpeech.QUEUE_ADD);
    }

    public void speak(String txt, Mode mode, int queueMode) {
        if (isTtsInitialized) {
            speakWithTts(txt, mode, queueMode);
        } else {
            speakList.add(() -> speakWithTts(txt, mode, queueMode));
        }
    }

    private void speakWithTts(String txt, Mode mode, int queueMode) {
        if (mode.equals(Mode.ADD_SILENCE)) {
            tts.playSilentUtterance(800L, queueMode, "silence");
            pendingSpeechCnt++;
        }
        tts.speak(txt, queueMode, null, "speak");
        pendingSpeechCnt++;
    }

    public void release() {
        manager.setStreamVolume(AudioManager.STREAM_ALARM, originalVolume, 0);
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        LogUtil.logI(CLASS_NAME, "release", "tts released completely!");
    }
}
