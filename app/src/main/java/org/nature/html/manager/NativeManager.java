package org.nature.html.manager;

import android.annotation.SuppressLint;
import android.webkit.JavascriptInterface;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.nature.common.exception.Warn;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.util.Md5Util;
import org.nature.html.mapper.PageConfigMapper;
import org.nature.html.model.PageConfig;
import org.nature.html.model.Res;

import java.util.List;
import java.util.Map;

/**
 * 页面配置
 * @author Nature
 * @version 1.0.0
 * @since 2025/11/06
 */
@SuppressLint("DefaultLocale")
@Component
public class NativeManager {

    private static final TypeReference<Map<String, String>> TYPE_HEADERS = new TypeReference<>() {
    };
    @Injection
    private PageConfigMapper pageConfigMapper;
    @Injection
    private DbManager dbManager;
    @Injection
    private HttpManager httpManager;
    @Injection
    private CommManager commManager;

    @JavascriptInterface
    public String invoke(String name, String param) {
        System.out.println("invoke:" + name + ":" + param);
        try {
            return this.buildRes("success", "", this.doInvoke(name, param));
        } catch (Warn e) {
            return this.buildRes("warn", e.getMessage(), "");
        } catch (Exception e) {
            return this.buildRes("error", "系统异常" + e.getMessage(), "");
        }
    }

    private String buildRes(String code, String message, Object data) {
        return JSON.toJSONString(new Res(code, message, data));
    }

    private Object doInvoke(String name, String param) {
        switch (name) {
            case "page":
                return this.page(param);
            case "md5":
                return this.md5(param);
            case "http":
                return this.http(param);
            case "list":
                return this.list(param);
            case "find":
                return this.find(param);
            case "update":
                return this.update(param);
            default:
                return commManager.handle(name, param);
        }
    }

    private Object page(String param) {
        String id = JSON.parseObject(param, String.class);
        PageConfig config = pageConfigMapper.findById(id);
        return config == null ? null : JSON.parseObject(config.getConfig());
    }

    private Object md5(String param) {
        List<String> list = JSON.parseArray(param, String.class);
        return Md5Util.md5(list.toArray(new String[0]));
    }

    private Object http(String param) {
        JSONObject json = JSON.parseObject(param);
        String url = json.getString("url");
        String method = json.getString("method");
        String data = json.getString("data");
        Map<String, String> headers = json.getObject("headers", TYPE_HEADERS);
        return httpManager.request(url, method, headers, data);
    }

    private Object list(String param) {
        JSONObject json = JSON.parseObject(param);
        String path = json.getString("path");
        String sql = json.getString("sql");
        return dbManager.list(path, sql);
    }

    private Object find(String param) {
        JSONObject json = JSON.parseObject(param);
        String path = json.getString("path");
        String sql = json.getString("sql");
        return dbManager.find(path, sql);
    }

    private Object update(String param) {
        JSONObject json = JSON.parseObject(param);
        String path = json.getString("path");
        String sql = json.getString("sql");
        return dbManager.update(path, sql);
    }


}
