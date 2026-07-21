package org.nature.html;

import android.webkit.JavascriptInterface;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.nature.util.CtxUtil;
import org.nature.util.DbUtil;
import org.nature.util.ExecUtil;
import org.nature.util.PythonUtil;

import java.util.Map;

import static org.nature.config.Config.DB_PATH_HTML;
import static org.nature.config.Config.SQL_HTML;

/**
 * 页面配置
 * @author Nature
 * @version 1.0.0
 * @since 2025/11/06
 */
public class NativeManager {

    @JavascriptInterface
    public void asyncInvoke(String id, String param) {
        ExecUtil.submit(() -> {
            String res = PythonUtil.exec(param);
            CtxUtil.callback(id, res);
        });
    }

    @JavascriptInterface
    public void closePage() {
        CtxUtil.closePage();
    }

    @JavascriptInterface
    public void page(String id, String pageId) {
        ExecUtil.submit(() -> {
            String res = this.getPageConfig(pageId);
            CtxUtil.callback(id, res);
        });
    }

    private String getPageConfig(String pageId) {
        JSONObject res = new JSONObject();
        try {
            String sql = SQL_HTML + "'" + pageId + "'";
            Map<String, Object> datum = DbUtil.find(DB_PATH_HTML, sql);
            if (datum == null) {
                res.put("code", "warning");
                res.put("message", "页面配置不存在");
            } else {
                res.put("code", "success");
                res.put("data", datum.get("config"));
            }
        } catch (Exception e) {
            res.put("code", "error");
            res.put("message", "系统异常：" + e.getMessage());
        }
        return res.toString(JSONWriter.Feature.WriteMapNullValue);
    }

}
