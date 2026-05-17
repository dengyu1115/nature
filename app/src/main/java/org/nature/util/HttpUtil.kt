package org.nature.util

import com.alibaba.fastjson2.JSON
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * http util
 * @author nature
 * @version 1.0.0
 * @since 2019/8/6 8:50
 */
object HttpUtil {

    /**
     * GET请求
     * @param url     请求地址
     * @param headers 请求头
     * @param params  请求参数
     * @return 响应内容
     */
    @JvmStatic
    fun get(url: String, headers: Map<String, String>?, params: Map<String?, String?>?): String {
        var conn: HttpURLConnection? = null
        return try {
            // 构建带参数的URL
            var finalUrl = url
            if (params != null && params.isNotEmpty()) {
                val queryString = buildQueryString(params)
                finalUrl = if (finalUrl.contains("?")) "$finalUrl&$queryString" else "$finalUrl?$queryString"
            }

            // 建立连接
            conn = URL(finalUrl).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000

            // 设置请求头
            if (headers != null) {
                for ((key, value) in headers) {
                    conn.setRequestProperty(key, value)
                }
            }

            // 获取响应码
            val code = conn.responseCode
            // 读取响应内容
            val inputStream = if (code >= 400) conn.errorStream else conn.inputStream
            val responseBody = read(inputStream)
            if (code == 200) {
                responseBody
            } else {
                throw RuntimeException("调用异常")
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        } finally {
            conn?.disconnect()
        }
    }

    /**
     * POST请求
     * @param url     请求地址
     * @param headers 请求头
     * @param data    请求数据
     * @return 响应内容
     */
    @JvmStatic
    fun post(url: String, headers: Map<String, String>?, data: Map<String, Any>?): String {
        var conn: HttpURLConnection? = null
        return try {
            // 建立连接
            conn = URL(url).openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000

            // 设置请求头
            if (headers != null) {
                for ((key, value) in headers) {
                    conn.setRequestProperty(key, value)
                }
            }

            // 设置POST请求参数
            if (data != null && data.isNotEmpty()) {
                conn.doOutput = true
                DataOutputStream(conn.outputStream).use { stream ->
                    stream.writeBytes(JSON.toJSONString(data))
                    stream.flush()
                }
            }

            // 获取响应码
            val code = conn.responseCode
            // 读取响应内容
            val inputStream = if (code >= 400) conn.errorStream else conn.inputStream
            val responseBody = read(inputStream)
            if (code == 200) {
                responseBody
            } else {
                throw RuntimeException("调用异常")
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        } finally {
            conn?.disconnect()
        }
    }

    /**
     * 读取输入流
     * @param inputStream 输入流
     * @return 字符串内容
     * @throws IOException IOException
     */
    @Throws(IOException::class)
    private fun read(inputStream: InputStream?): String {
        if (inputStream == null) {
            return ""
        }
        val result = StringBuilder()
        BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                result.append(line).append("\n")
            }
        }
        return result.toString()
    }

    /**
     * 构建查询字符串
     * @param params 参数map
     * @return 查询字符串
     */
    private fun buildQueryString(params: Map<String?, String?>): String {
        if (params.isEmpty()) {
            return ""
        }

        val sb = StringBuilder()
        var first = true

        for ((key, value) in params) {
            if (key == null) {
                continue // 跳过键为null的条目
            }
            if (!first) {
                sb.append("&")
            }
            sb.append(URLEncoder.encode(key, StandardCharsets.UTF_8))
            sb.append("=")
            sb.append(URLEncoder.encode(value ?: "", StandardCharsets.UTF_8))
            first = false
        }
        return sb.toString()
    }
}
