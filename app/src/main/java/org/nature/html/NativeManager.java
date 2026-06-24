package org.nature.html;

import android.annotation.SuppressLint;
import android.webkit.JavascriptInterface;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;
import org.nature.exception.Warn;
import org.nature.util.*;

import java.util.List;
import java.util.Map;

import static org.nature.config.Config.DB_PATH_HTML;
import static org.nature.config.Config.SQL_HTML;

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
        JSONObject res = new JSONObject();
        try {
            res.put("code", "success");
            res.put("data", this.doInvoke(name, param));
        } catch (Warn e) {
            res.put("code", "warn");
            res.put("message", e.getMessage());
        } catch (Exception e) {
            res.put("code", "error");
            res.put("message", "系统异常：" + e.getMessage());
        }
        return res.toString(JSONWriter.Feature.WriteMapNullValue);
    }

    @JavascriptInterface
    public void asyncInvoke(String name, String id, String param) {
        ExecUtil.submit(() -> {
            String res = this.invoke(name, param);
            CtxUtil.callback(id, res);
        });
    }

    @JavascriptInterface
    public void closePage() {
        CtxUtil.closePage();
    }

    private Object doInvoke(String name, String param) {
        return switch (name) {
            case "page" -> this.page(param);
            case "md5" -> this.md5(param);
            case "http" -> this.http(param);
            case "sql" -> this.sql(param);
            case "python" -> this.python(param);
            default -> throw new Warn("未定义的接口：" + name);
        };
    }

    private Object page(String param) {
        String id = JSON.parseObject(param, String.class);
        String sql = SQL_HTML + "'" + id + "'";
        Map<String, Object> config = DbUtil.find(DB_PATH_HTML, sql);
        return config == null ? null : JSON.parseObject((String) config.get("config"));
    }

    private Object md5(String param) {
        List<String> list = JSON.parseArray(param, String.class);
        return Md5Util.md5(list.toArray(new String[0]));
    }

    private Object http(String param) {
        JSONObject json = JSON.parseObject(param);
        String url = json.getString("url");
        String method = json.getString("method");
        Map<String, String> headers = json.getObject("headers", TYPE_HEADERS);
        if ("POST".equals(method)) {
            JSONObject data = json.getJSONObject("data");
            return HttpUtil.post(url, headers, data);
        }
        Map<String, String> data = json.getObject("data", TYPE_HEADERS);
        return HttpUtil.get(url, headers, data);
    }

    private Object sql(String param) {
        JSONObject json = JSON.parseObject(param);
        String path = json.getString("path");
        String type = json.getString("type");
        String sql = json.getString("sql");
        return switch (type) {
            case "find" -> DbUtil.find(path, sql);
            case "list" -> DbUtil.list(path, sql);
            case "update" -> DbUtil.update(path, sql);
            default -> DbUtil.ddl(path, sql);
        };
    }

    private Object python(String param) {
        JSONObject json = JSON.parseObject(param);
        String module = json.getString("module");
        String func = json.getString("func");
        JSONObject args = json.getJSONObject("args");
        if (module != null && func != null) {
            return PythonUtil.execModule(module, func, args);
        }
        String script = json.getString("script");
        // 判断类型，分类型返回
        return PythonUtil.execScript(script, args);
    }

}
