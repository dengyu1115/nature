package org.nature.html.manager;

import android.annotation.SuppressLint;
import android.webkit.JavascriptInterface;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.nature.exception.Warn;
import org.nature.html.model.Res;
import org.nature.util.DbUtil;
import org.nature.util.HttpUtil;
import org.nature.util.Md5Util;
import org.nature.util.PythonUtil;

import java.util.List;
import java.util.Map;

/**
 * 页面配置
 * @author Nature
 * @version 1.0.0
 * @since 2025/11/06
 */
@SuppressLint("DefaultLocale")
public class NativeManager {

    private static final TypeReference<Map<String, String>> TYPE_HEADERS = new TypeReference<>() {
    };

    @JavascriptInterface
    public String invoke(String name, String param) {
        try {
            Object data = this.doInvoke(name, param);
            return this.buildRes("success", "", data);
        } catch (Warn e) {
            return this.buildRes("warn", e.getMessage(), "");
        } catch (Exception e) {
            return this.buildRes("error", "系统异常：" + e.getMessage(), "");
        }
    }

    private String buildRes(String code, String message, Object data) {
        return JSON.toJSONString(new Res(code, message, data), SerializerFeature.WriteMapNullValue);
    }

    private Object doInvoke(String name, String param) {
        switch (name) {
            case "page":
                return this.page(param);
            case "md5":
                return this.md5(param);
            case "http":
                return this.http(param);
            case "sql":
                return this.sql(param);
            case "python":
                return this.python(param);
            default:
                throw new Warn("未定义的接口：" + name);
        }
    }

    private Object page(String param) {
        String id = JSON.parseObject(param, String.class);
        String sql = "select config from page where id=" + id;
        Map<String, Object> config = DbUtil.find("nature_test/html.db", sql);
        return config == null ? null : JSON.toJSON(config.get("config"));
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
        return HttpUtil.request(url, method, headers, data);
    }

    private Object sql(String param) {
        JSONObject json = JSON.parseObject(param);
        String path = json.getString("path");
        String type = json.getString("type");
        String sql = json.getString("sql");
        switch (type) {
            case "find":
                return DbUtil.find(path, sql);
            case "list":
                return DbUtil.list(path, sql);
            default:
                return DbUtil.update(path, sql);
        }
    }

    private Object python(String param) {
        JSONObject json = JSON.parseObject(param);
        String script = json.getString("script");
        JSONObject args = json.getJSONObject("args");
        // 判断类型，分类型返回
        return PythonUtil.execScript(script, args);
    }

}
