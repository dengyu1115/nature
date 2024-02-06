package org.nature.common.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.nature.common.constant.Const.UTF_8;

/**
 * http util
 * @author nature
 * @version 1.0.0
 * @since 2019/8/6 8:50
 */
public class HttpUtil {


    private static final Map<String, String> HEADER = null;

    /**
     * get处理
     * @param uri      uri
     * @param function function
     * @param <T>      t
     * @return T
     */
    public static <T> T doGet(String uri, Function<Stream<String>, T> function) {
        return HttpUtil.doGet(uri, UTF_8, HEADER, function);
    }

    /**
     * get处理
     * @param uri      uri
     * @param header   header
     * @param function function
     * @param <T>      t
     * @return T
     */
    public static <T> T doGet(String uri, Map<String, String> header, Function<Stream<String>, T> function) {
        return HttpUtil.doGet(uri, UTF_8, header, function);
    }

    /**
     * get处理
     * @param uri      uri
     * @param charset  charset
     * @param function function
     * @param <T>      t
     * @return T
     */
    public static <T> T doGet(String uri, String charset, Function<Stream<String>, T> function) {
        return HttpUtil.doGet(uri, charset, HEADER, function);
    }

    /**
     * get处理
     * @param uri      uri
     * @param charset  charset
     * @param header   header
     * @param function function
     * @param <T>      t
     * @return T
     */
    public static <T> T doGet(String uri, String charset, Map<String, String> header,
                              Function<Stream<String>, T> function) {
        return ExecUtil.single(() -> HttpUtil.exec(uri, header, connection -> {
            connection.setConnectTimeout(30000);
            try (InputStream is = connection.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset))) {
                Stream<String> lines = reader.lines();
                return function.apply(lines);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
    }

    /**
     * 执行http请求
     * @param uri    uri
     * @param header header
     * @param func   处理函数
     * @return T
     */
    private static <T> T exec(String uri, Map<String, String> header, Function<HttpURLConnection, T> func) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(uri);
            connection = (HttpURLConnection) url.openConnection();
            if (header != null) {
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            // 三十秒超时
            connection.setConnectTimeout(30000);
            return func.apply(connection);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

}
