package com.bme.jnsbbk.oauthserver.components.methods

import com.gargoylesoftware.htmlunit.ScriptException
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener
import java.lang.Exception
import java.net.MalformedURLException
import java.net.URL

fun setupWebClient(client: WebClient) {
    client.options.isThrowExceptionOnScriptError = false
    client.options.isThrowExceptionOnFailingStatusCode = false
    client.javaScriptErrorListener = object : JavaScriptErrorListener {
        override fun scriptException(page: HtmlPage, ex: ScriptException) {}
        override fun timeoutError(page: HtmlPage, allowedTime: Long, executionTime: Long) {}
        override fun malformedScriptURL(page: HtmlPage, url: String, ex: MalformedURLException) {}
        override fun loadScriptError(page: HtmlPage, scriptUrl: URL, ex: Exception) {}
        override fun warn(message: String, sourceName: String, line: Int, lineSource: String, lineOffset: Int) {}
    }
}
