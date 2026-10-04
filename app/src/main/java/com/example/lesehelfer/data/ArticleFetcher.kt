package com.example.lesehelfer.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

object ArticleFetcher {
    suspend fun fetchTextFromUrl(urlStr: String): String = withContext(Dispatchers.IO) {
        val doc = Jsoup.connect(urlStr)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .timeout(10000)
            .get()

        // Remove non-article tags
        doc.select("script, style, nav, footer, header, aside").remove()

        val paragraphs = doc.select("p")
        if (paragraphs.isNotEmpty()) {
            paragraphs.joinToString("\n\n") { it.text() }
        } else {
            doc.body().text()
        }
    }
}
