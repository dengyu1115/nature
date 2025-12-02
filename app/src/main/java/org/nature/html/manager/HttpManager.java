package org.nature.html.manager;

import org.nature.common.exception.Warn;
import org.nature.common.ioc.annotation.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class HttpManager {

    /**
     * 同步HTTP请求
     * @param url     请求地址
     * @param method  请求方法
     * @param headers 请求头
     * @param data    请求数据
     * @return HttpResponse
     */
    public String request(String url, String method, Map<String, String> headers, String data) {
        HttpURLConnection conn = null;
        try {
            // 建立连接
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            // 设置请求头
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            // 设置POST请求参数
            if ("POST".equalsIgnoreCase(method) && data != null) {
                conn.setDoOutput(true);
                try (DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream())) {
                    outputStream.writeBytes(data);
                    outputStream.flush();
                }
            }
            // 获取响应码
            int code = conn.getResponseCode();
            // 读取响应内容
            InputStream inputStream = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
            String responseBody = this.read(inputStream);
            if (code == 200) {
                return responseBody;
            } else if (code >= 400) {
                throw new Warn(responseBody);
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
    private String read(InputStream inputStream) throws IOException {
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

}