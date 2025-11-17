package com.alarm.newsalarmkt.newsmanager

import android.util.Pair
import com.alarm.newsalarmkt.utils.LogUtil
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

internal class NewsArticleCrawler {

    private val service = Executors.newFixedThreadPool(5)
    private val executorList = ArrayList<Callable<Pair<String, String>>>()
    private lateinit var futureList: MutableList<Future<Pair<String, String>>>

    fun crawl(jsonArticleSet: MutableSet<JSONObject>): Pair<ArrayList<String>, ArrayList<String>> {
        for (article in jsonArticleSet) {
            executorList.add(CrawlingExecutor(article))
        }

        return crawlAsync()
    }

    private fun crawlAsync(): Pair<ArrayList<String>, ArrayList<String>> {
        try {
            futureList = service.invokeAll(executorList, 2, TimeUnit.SECONDS)
            return completedArticleList
        } catch (e: Exception) {
            LogUtil.logE(CLASS_NAME, "crawlAsync", e)
        }

        return Pair(ArrayList(), ArrayList())
    }

    private val completedArticleList: Pair<ArrayList<String>, ArrayList<String>>
        get() {
            val titleList = ArrayList<String>()
            val bodyList = ArrayList<String>()
            for (i in futureList.indices) {
                try {
                    val article = futureList[i].get()
                    if ("" == article.first || "" == article.second) {
                        continue
                    }
                    titleList.add(article.first)
                    bodyList.add(article.second)
                } catch (e: Exception) {
                    LogUtil.logE(CLASS_NAME, "getCompletedArticleList", e)
                }
            }
            return Pair(titleList, bodyList)
        }

    fun release() {
        service.shutdown()
    }

    private data class CrawlingExecutor(val jsonArticle: JSONObject)
        : Callable<Pair<String, String>> {

        override fun call(): Pair<String, String> {
            return Pair(articleTitle, articleBody)
        }

        private val articleTitle: String
            get() {
                try {
                    return jsonArticle
                        .getString("title")
                        .replace("([\\[(<&](.*?)[;>)\\]])".toRegex(), "")
                } catch (e: Exception) {
                    LogUtil.logE(CLASS_NAME, "getArticleTitle", e)
                }

                return ""
            }

        private val articleBody: String
            get() {
                try {
                    return modifyContent(crawlPassage(jsonArticle.getString("link")))
                } catch (e: Exception) {
                    LogUtil.logE(CLASS_NAME, "getArticleBody", e)
                }

                return ""
            }

        private fun modifyContent(e: Element?): String {
            if (e == null) {
                return ""
            }

            var content = e.text().replace("([\\[(<&](.*?)[;>)\\]])".toRegex(), "")
            val exceptChars = charArrayOf('#', '※', '▶', 'ⓒ')
            for (exceptChar in exceptChars) {
                val index = content.indexOf(exceptChar)
                if (index != -1) {
                    content = content.take(index)
                    break
                }
            }
            return content
        }

        private fun crawlPassage(link: String): Element? {
            try {
                val doc = Jsoup.connect(link).get()
                val e = doc.selectFirst("#dic_area")
                    ?: doc.selectFirst("#articeBody")
                    ?: doc.selectFirst("#newsEndContents")
                    ?: return null

                val num = e.childrenSize()
                for (i in num - 1 downTo 0) {
                    val tag = e.child(i).tagName()
                    if (((tag != "font") && (tag != "span") && (tag != "b"))
                        || e.child(i).className() == "end_photo_org"
                    ) {
                        e.child(i).remove()
                    }
                }
                LogUtil.logI(CLASS_NAME, "crawlPassage", "crawling completed!")

                return e
            } catch (e: Exception) {
                LogUtil.logE(CLASS_NAME, "crawlPassage", e)
            }

            return null
        }
    }

    companion object {
        private const val CLASS_NAME = "NewsArticleCrawler"
    }
}
