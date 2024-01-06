package org.nature.common.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * http util
 * @author nature
 * @version 1.0.0
 * @since 2019/8/6 8:50
 */
public class HttpUtil {

    private static final String UTF_8 = "utf8";

    private static final Map<String, String> HEADER = null;

    private static final int POOL_SIZE_CORE = 32, POOL_SIZE_MAX = 64, ALIVE_TIME = 1;

    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(POOL_SIZE_CORE, POOL_SIZE_MAX, ALIVE_TIME,
            TimeUnit.SECONDS, new LinkedBlockingDeque<>());

    /**
     * get处理
     * @param uri      uri
     * @param function function
     * @param <T>      t
     * @return T
     */
    public static <T> T doGet(String uri, Function<Stream<String>, T> function) {
        return doGet(uri, UTF_8, HEADER, function);
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
        return doGet(uri, UTF_8, header, function);
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
        return doGet(uri, charset, HEADER, function);
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
    public static <T> T doGet(String uri, String charset, Map<String, String> header
            , Function<Stream<String>, T> function) {
        Future<T> future = EXECUTOR.submit(() -> {
            InputStream inputStream = null;
            try {
                URL url = new URL(uri);
                URLConnection connection = url.openConnection();
                if (header != null) {
                    for (Map.Entry<String, String> entry : header.entrySet()) {
                        connection.setRequestProperty(entry.getKey(), entry.getValue());
                    }
                }
                connection.setConnectTimeout(30000);    // 三十秒超时
                inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset));
                Stream<String> lines = bufferedReader.lines();
                return function.apply(lines);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
