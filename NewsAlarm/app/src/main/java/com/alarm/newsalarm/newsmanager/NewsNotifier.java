package com.alarm.newsalarm.newsmanager;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alarm.newsalarm.database.AlarmData;
import com.alarm.newsalarm.outputmanager.SoundPlayer;
import com.alarm.newsalarm.outputmanager.TtsManager;
import com.alarm.newsalarm.outputmanager.Vibrator;

import java.util.ArrayList;
import java.util.Objects;

public class NewsNotifier {

    private static final String CLASS_NAME = "NewsNotifier";

    static final int MSG_LOAD_NEWS_START = 0x00;
    static final int MSG_LOAD_NEWS_SUCCESS = 0x01;
    static final int MSG_LOAD_NEWS_FAILURE = 0x02;
    static final int MSG_CRAWL_NEWS_SUCCESS = 0x03;
    static final int MSG_CRAWL_NEWS_FAILURE = 0x04;
    static final int MSG_NEWS_ALARM_END = 0x05;

    private final NewsLoader loader;
    private final NewsArticleCrawler crawler;
    private final SoundPlayer soundPlayer;
    private final Vibrator vibrator;
    private final TtsManager ttsManager;
    private final AlarmData data;
    private final Handler handler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_LOAD_NEWS_START -> {
                    Log.i(CLASS_NAME, "handleMessage$loading news data started!");
                    loader.load(data.getAlarmTopic());
                }
                case MSG_LOAD_NEWS_SUCCESS -> {
                    Log.i(CLASS_NAME, "handleMessage$loading news data completed!");
                    ArrayList<String> loadedData = msg.getData().getStringArrayList("load_return");
                    crawler.crawl(Objects.requireNonNull(loadedData));
                }
                case MSG_LOAD_NEWS_FAILURE -> {
                    Log.i(CLASS_NAME, "handleMessage$loading news data failed!");
                    ttsManager.speak("뉴스를 검색하지 못했습니다. 연결을 확인해 보시거나, 다른 키워드로 부탁드립니다.", 1);
                }
                case MSG_CRAWL_NEWS_SUCCESS -> {
                    Log.i(CLASS_NAME, "handleMessage$crawl news article completed!");
                    Bundle bundle = msg.getData();
                    ttsManager.speakArticles(
                        Objects.requireNonNull(bundle.getStringArrayList("crawl_return_title")),
                        Objects.requireNonNull(bundle.getStringArrayList("crawl_return_body"))
                    );
                }
                case MSG_CRAWL_NEWS_FAILURE -> {
                    Log.i(CLASS_NAME, "handleMessage$crawl news article failed!");
                    ttsManager.speak("서버 오류 혹은 다른 문제로 인해 뉴스에 연결하지 못했습니다. 연결이 원활한 환경에서 시도바랍니다.", 1);
                }
                case MSG_NEWS_ALARM_END -> {
                    handler.removeMessages(MSG_LOAD_NEWS_START);
                    handler.removeMessages(MSG_LOAD_NEWS_SUCCESS);
                    handler.removeMessages(MSG_LOAD_NEWS_FAILURE);
                    handler.removeMessages(MSG_CRAWL_NEWS_SUCCESS);
                    handler.removeMessages(MSG_CRAWL_NEWS_FAILURE);
                }
            }
        }
    };

    public NewsNotifier(Context context, AlarmData data) {
        this.data = data;
        loader = new NewsLoader(context, handler);
        crawler = new NewsArticleCrawler(handler);
        soundPlayer = new SoundPlayer(context);
        vibrator = new Vibrator(context);
        ttsManager = new TtsManager(context, data);
    }

    public void start() {
        soundPlayer.playBgm(data.getVolumeSize());
        vibrator.vibrateRepeatedly(data.getVibIntensity());
        handler.sendEmptyMessage(MSG_LOAD_NEWS_START);
    }

    public void finish() {
        handler.sendEmptyMessage(MSG_NEWS_ALARM_END);
        loader.release();
        crawler.release();
        soundPlayer.release();
        vibrator.release();
        ttsManager.release();
    }
}
