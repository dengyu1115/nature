package org.nature.util;


import android.annotation.SuppressLint;
import com.alibaba.fastjson.JSON;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * http util
 * @author nature
 * @version 1.0.0
 * @since 2019/8/6 8:50
 */
@SuppressLint("NewApi")
public class HttpUtil {


    /**
     * GET请求
     * @param url     请求地址
     * @param headers 请求头
     * @param params  请求参数
     * @return 响应内容
     */
    public static String get(String url, Map<String, String> headers, Map<String, String> params) {
        HttpURLConnection conn = null;
        try {
            // 构建带参数的URL
            if (params != null && !params.isEmpty()) {
                String queryString = buildQueryString(params);
                url = url.contains("?") ? url + "&" + queryString : url + "?" + queryString;
            }

            // 建立连接
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            // 设置请求头
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            // 获取响应码
            int code = conn.getResponseCode();
            // 读取响应内容
            InputStream inputStream = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
            String responseBody = HttpUtil.read(inputStream);
            if (code == 200) {
                return responseBody;
            } else {
                throw new RuntimeException("调用异常");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * POST请求
     * @param url     请求地址
     * @param headers 请求头
     * @param data    请求数据
     * @return 响应内容
     */
    public static String post(String url, Map<String, String> headers, Map<String, Object> data) {
        HttpURLConnection conn = null;
        try {
            // 建立连接
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            // 设置请求头
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            // 设置POST请求参数
            if (data != null && !data.isEmpty()) {
                conn.setDoOutput(true);
                try (DataOutputStream stream = new DataOutputStream(conn.getOutputStream())) {
                    stream.writeBytes(JSON.toJSONString(data));
                    stream.flush();
                }
            }

            // 获取响应码
            int code = conn.getResponseCode();
            // 读取响应内容
            InputStream inputStream = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
            String responseBody = HttpUtil.read(inputStream);
            if (code == 200) {
                return responseBody;
            } else {
                throw new RuntimeException("调用异常");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * 读取输入流
     * @param inputStream 输入流
     * @return 字符串内容
     * @throws IOException IOException
     */
    private static String read(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
        }
        return result.toString();
    }

    /**
     * 构建查询字符串
     * @param params 参数map
     * @return 查询字符串
     */
    private static String buildQueryString(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getKey() == null) {
                continue; // 跳过键为null的条目
            }
            if (!first) {
                sb.append("&");
            }
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(URLEncoder.encode(key, StandardCharsets.UTF_8));
            sb.append("=");
            sb.append(URLEncoder.encode(value != null ? value : "", StandardCharsets.UTF_8));
            first = false;
        }
        return sb.toString();
    }
}
