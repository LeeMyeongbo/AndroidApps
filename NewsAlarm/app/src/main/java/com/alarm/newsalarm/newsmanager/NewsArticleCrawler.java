package com.alarm.newsalarm.newsmanager;

import android.util.Pair;

import com.alarm.newsalarm.utils.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

class NewsArticleCrawler {

    private static final String CLASS_NAME = "NewsArticleCrawler";

    private final ExecutorService service = Executors.newFixedThreadPool(5);
    private final List<Callable<Pair<String, String>>> excutorList = new ArrayList<>();
    private List<Future<Pair<String, String>>> futureList;

    Pair<ArrayList<String>, ArrayList<String>> crawl(Set<JSONObject> jsonArticleSet) {
        for (JSONObject article : jsonArticleSet) {
            excutorList.add(new CrawlingExecutor(article));
        }
        return crawlAsync();
    }

    private Pair<ArrayList<String>, ArrayList<String>> crawlAsync() {
        try {
            futureList = service.invokeAll(excutorList, 2, TimeUnit.SECONDS);
            return getCompletedArticleList();
        } catch (InterruptedException | RejectedExecutionException | NullPointerException e) {
            LogUtil.logE(CLASS_NAME, "crawlAsync", e);
        }
        return new Pair<>(new ArrayList<>(), new ArrayList<>());
    }

    private Pair<ArrayList<String>, ArrayList<String>> getCompletedArticleList() {
        ArrayList<String> titleList = new ArrayList<>();
        ArrayList<String> bodyList = new ArrayList<>();
        for (int i = 0; i < futureList.size(); i++) {
            try {
                Pair<String, String> article = futureList.get(i).get();
                if ("".equals(article.first) || "".equals(article.second)) {
                    continue;
                }
                titleList.add(article.first);
                bodyList.add(article.second);
            } catch (ExecutionException | InterruptedException | CancellationException e) {
                LogUtil.logE(CLASS_NAME, "getCompletedArticleList", e);
            }
        }
        return new Pair<>(titleList, bodyList);
    }

    void release() {
        service.shutdown();
    }

    record CrawlingExecutor(JSONObject jsonArticle) implements Callable<Pair<String, String>> {

        @Override
        public Pair<String, String> call() {
            return new Pair<>(getArticleTitle(), getArticleBody());
        }

        private String getArticleTitle() {
            try {
                return jsonArticle.getString("title").replaceAll("([\\[(<&](.*?)[;>)\\]])", "");
            } catch (JSONException | NullPointerException e) {
                LogUtil.logE(CLASS_NAME, "getArticleTitle", e);
            }

            return "";
        }

        private String getArticleBody() {
            try {
                return modifyContent(crawlPassage(jsonArticle.getString("link")));
            } catch (JSONException | NullPointerException e) {
                LogUtil.logE(CLASS_NAME, "getArticleBody", e);
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
                LogUtil.logI(CLASS_NAME, "crawlPassage", "crawling completed!");

                return e;
            } catch (IOException | NullPointerException e) {
                LogUtil.logE(CLASS_NAME, "crawlPassage", e);
            }

            return null;
        }
    }
}
