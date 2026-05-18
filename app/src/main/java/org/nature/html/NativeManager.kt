package org.nature.html

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import com.alibaba.fastjson2.JSONWriter
import com.alibaba.fastjson2.TypeReference
import org.nature.config.Config.DB_PATH_HTML
import org.nature.config.Config.SQL_HTML
import org.nature.exception.Warn
import org.nature.util.DbUtil
import org.nature.util.HttpUtil
import org.nature.util.Md5Util
import org.nature.util.PythonUtil

/**
 * 页面配置
 * @author Nature
 * @version 1.0.0
 * @since 2025/11/06
 */
@SuppressLint("DefaultLocale")
class NativeManager {

    private val TYPE_HEADERS = object : TypeReference<Map<String?, String?>>() {}

    @JavascriptInterface
    fun invoke(name: String, param: String): String {
        val res = JSONObject()
        try {
            res["code"] = "success"
            res["data"] = doInvoke(name, param)
        } catch (e: Warn) {
            res["code"] = "warn"
            res["message"] = e.message
        } catch (e: Exception) {
            res["code"] = "error"
            res["message"] = "系统异常：" + e.message
        }
        return res.toString(JSONWriter.Feature.WriteMapNullValue)
    }

    private fun doInvoke(name: String, param: String): Any? {
        return when (name) {
            "page" -> page(param)
            "md5" -> md5(param)
            "http" -> http(param)
            "sql" -> sql(param)
            "python" -> python(param)
            else -> throw Warn("未定义的接口：$name")
        }
    }

    private fun page(param: String): Any? {
        val id = JSON.parseObject(param, String::class.java)
        val sql = "$SQL_HTML'$id'"
        val config = DbUtil.find(DB_PATH_HTML, sql)
        return if (config == null) null else JSON.parseObject(config["config"] as String)
    }

    private fun md5(param: String): String {
        val list = JSON.parseArray(param, String::class.java)
        return Md5Util.md5(*list.toTypedArray())
    }

    private fun http(param: String): Any {
        val json = JSON.parseObject(param)
        val url = json.getString("url")
        val method = json.getString("method")
        val headers = json.getObject<Map<String?, String?>>("headers", TYPE_HEADERS.type)
        return if ("POST" == method) {
            val data = json.getJSONObject("data")
            HttpUtil.post(url, headers, data)
        } else {
            val data = json.getObject<Map<String?, String?>>("data", TYPE_HEADERS.type)
            HttpUtil.get(url, headers, data)
        }
    }

    private fun sql(param: String): Any? {
        val json = JSON.parseObject(param)
        val path = json.getString("path")
        val type = json.getString("type")
        val sql = json.getString("sql")
        return when (type) {
            "find" -> DbUtil.find(path, sql)
            "list" -> DbUtil.list(path, sql)
            "update" -> DbUtil.update(path, sql)
            else -> DbUtil.ddl(path, sql)
        }
    }

    private fun python(param: String): Any? {
        val json = JSON.parseObject(param)
        val module = json.getString("module")
        val func = json.getString("func")
        val args = json.getJSONObject("args")
        return if (module != null && func != null) {
            PythonUtil.execModule(module, func, args)
        } else {
            val script = json.getString("script")
            // 判断类型，分类型返回
            PythonUtil.execScript(script, args)
        }
    }
}
