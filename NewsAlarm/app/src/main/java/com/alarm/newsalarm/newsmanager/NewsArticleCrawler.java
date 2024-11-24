package com.alarm.newsalarm.newsmanager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class NewsArticleCrawler {

    private static final String CLASS_NAME = "NewsArticleCrawler";

    private final ExecutorService service = Executors.newFixedThreadPool(5);
    private final Bundle bundle = new Bundle();
    private final Handler handler;
    private List<Future<Pair<String, String>>> futureList;

    NewsArticleCrawler(Handler handler) {
        this.handler = handler;
    }

    void crawl(List<String> jsonArticleStringList) {
        List<Callable<Pair<String, String>>> crawlingList = new ArrayList<>();
        for (String jsonArticleString : jsonArticleStringList) {
            crawlingList.add(new CrawlingExecutor(parseToJson(jsonArticleString)));
        }
        crawlAsync(crawlingList);
    }

    private JSONObject parseToJson(String json) {
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            Log.e(CLASS_NAME, "parseToJson$parse String to JSONObject failed!");
        }
        return null;
    }

    private void crawlAsync(List<Callable<Pair<String, String>>> crawlingList) {
        try {
            futureList = service.invokeAll(crawlingList, 2, TimeUnit.SECONDS);
            ArrayList<Pair<String, String>> completedArticleList = getCompletedArticleList();
            if (!completedArticleList.isEmpty()) {
                sendMessageSuccess(completedArticleList);
                return;
            }
        } catch (InterruptedException | RejectedExecutionException | NullPointerException e) {
            Log.e(CLASS_NAME, "crawlAsync$" + e.getMessage());
        }
        sendMessageFailure();
    }

    private ArrayList<Pair<String, String>> getCompletedArticleList() {
        ArrayList<Pair<String, String>> completedArticleList = new ArrayList<>();
        for (int i = 0; i < futureList.size(); i++) {
            try {
                Pair<String, String> article = futureList.get(i).get();
                if ("".equals(article.first) || "".equals(article.second)) {
                    continue;
                }
                completedArticleList.add(article);
            } catch (ExecutionException | InterruptedException | CancellationException e) {
                Log.e(CLASS_NAME, "getCompletedArticleList$" + e.getMessage());
            }
        }
        return completedArticleList;
    }

    private void sendMessageSuccess(ArrayList<Pair<String, String>> articleList) {
        putArticleDataToBundle(articleList);
        Message msg = handler.obtainMessage();
        msg.setData(bundle);
        msg.what = NewsNotifier.MSG_CRAWL_NEWS_SUCCESS;
        handler.sendMessage(msg);
    }

    private void putArticleDataToBundle(ArrayList<Pair<String, String>> articleList) {
        ArrayList<String> titleList = new ArrayList<>();
        ArrayList<String> bodyList = new ArrayList<>();
        for (Pair<String, String> article : articleList) {
            titleList.add(article.first);
            bodyList.add(article.second);
        }
        bundle.putStringArrayList("crawl_return_title", titleList);
        bundle.putStringArrayList("crawl_return_body", bodyList);
    }

    private void sendMessageFailure() {
        Message msg = handler.obtainMessage();
        msg.what = NewsNotifier.MSG_CRAWL_NEWS_FAILURE;
        handler.sendMessage(msg);
    }

    void release() {
        service.shutdown();
    }

    static class CrawlingExecutor implements Callable<Pair<String, String>> {

        private final JSONObject jsonArticle;

        private CrawlingExecutor(JSONObject jsonArticle) {
            this.jsonArticle = jsonArticle;
        }

        @Override
        public Pair<String, String> call() {
            return new Pair<>(getArticleTitle(), getArticleBody());
        }

        private String getArticleTitle() {
            try {
                return jsonArticle.getString("title").replaceAll("([\\[(<&](.*?)[;>)\\]])", "");
            } catch (JSONException | NullPointerException e) {
                Log.e(CLASS_NAME, "getArticleTitle$" + e.getMessage());
            }

            return "";
        }

        private String getArticleBody() {
            try {
                return modifyContent(crawlPassage(jsonArticle.getString("link")));
            } catch (JSONException | NullPointerException e) {
                Log.e(CLASS_NAME, "getArticleBody$" + e.getMessage());
            }

            return "";
        }

        private String modifyContent(Element e) {
            if (e == null) {
                return "";
            }
            String content = e.text().replaceAll("([\\[(<&](.*?)[;>)\\]])", "");
            final char[] exceptChars = {'#', '※', '▶', 'ⓒ'};
            for (char exceptChar : exceptChars) {
                int index = content.indexOf(exceptChar);
                if (index != -1) {
                    content = content.substring(0, index);
                    break;
                }
            }
            return content;
        }

        private Element crawlPassage(String link) {
            try {
                Document doc = Jsoup.connect(link).get();
                Element e = doc.selectFirst("#dic_area");
                if (e == null) {
                    e = doc.selectFirst("#articeBody");
                }
                if (e == null) {
                    e = doc.selectFirst("#newsEndContents");
                }

                int num = e.childrenSize();
                for (int i = num - 1; i >= 0; i--) {
                    String tag = e.child(i).tagName();
                    if ((!tag.equals("font") && !tag.equals("span") && !tag.equals("b"))
                        || e.child(i).className().equals("end_photo_org")) {
                        e.child(i).remove();
                    }
                }
                Log.i(CLASS_NAME, "crawlPassage$crawling completed!");

                return e;
            } catch (IOException | NullPointerException e) {
                Log.e(CLASS_NAME, "crawlPassage$" + e.getMessage());
            }

            return null;
        }
    }
}
