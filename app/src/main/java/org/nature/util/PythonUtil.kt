package org.nature.util

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import com.alibaba.fastjson2.JSONWriter
import com.alibaba.fastjson2.TypeReference
import com.chaquo.python.PyException
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import org.nature.exception.Warn
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Future

object PythonUtil {

    private val TYPE_HEADERS = object : TypeReference<Map<String, String>>() {}

    private var initialized = false

    private var builtins_module: PyObject? = null

    private var script_module: PyObject? = null
    private var func_module: PyObject? = null
    private var json_obj_module: PyObject? = null
    private var json_str_module: PyObject? = null

    @JvmStatic
    fun init() {
        if (!initialized) {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(CtxUtil.get()))
                val instance = Python.getInstance()
                builtins_module = instance.getModule("builtins")
                script_module = instance.getModule("nature").get("dynamic_exec")
                func_module = instance.getModule("nature").get("module_func_exec")
                json_str_module = instance.getModule("nature").get("to_json")
                json_obj_module = Python.getInstance().getModule("json").get("loads")
                instance.getModule("module_script")
            }
            initialized = true
        }
    }

    @JvmStatic
    fun refresh() {
        DbUtil.refresh()
        CtxUtil.refresh()
    }

    @JvmStatic
    fun execScript(script: String, args: JSONObject): Any? {
        return try {
            toJava(script_module!!.call(script, toPython(args)))
        } catch (e: PyException) {
            val cause = e.cause
            if (cause is Warn) {
                throw cause
            }
            throw e
        }
    }

    @JvmStatic
    fun execModule(module: String, func: String, args: JSONObject): Any? {
        return try {
            toJava(func_module!!.call(module, func, toPython(args)))
        } catch (e: PyException) {
            val cause = e.cause
            if (cause is Warn) {
                throw cause
            }
            throw e
        }
    }

    @JvmStatic
    fun multiThread(items: PyObject, run: PyObject): PyObject {
        // 任务结果获取集合
        val cl: MutableList<Future<PyObject>> = LinkedList()
        // 提交任务
        items.asList().forEach { i ->
            cl.add(ExecUtil.submit(Callable { run.call(i) }))
        }
        val list = builtins_module!!.callAttr("list")
        cl.forEach { i ->
            try {
                list.callAttr("append", i.get())
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
        return list
    }

    @JvmStatic
    fun find(path: String, sql: String): PyObject {
        return toPython(DbUtil.find(path, sql))
    }

    @JvmStatic
    fun list(path: String, sql: String): PyObject {
        val list = DbUtil.list(path, sql)
        return toPython(list)
    }

    @JvmStatic
    fun update(path: String, sql: String): PyObject {
        return toPython(DbUtil.update(path, sql))
    }

    @JvmStatic
    fun ddl(path: String, sql: String) {
        DbUtil.ddl(path, sql)
    }

    @JvmStatic
    fun post(url: String, headers: PyObject, data: PyObject): PyObject {
        val hs = JSON.parseObject<Map<String, String>>(JSON.toJSONString(toJava(headers)), TYPE_HEADERS.type)
        return toPython(HttpUtil.post(url, hs, JSON.parseObject(JSON.toJSONString(toJava(data)))))
    }

    @JvmStatic
    fun get(url: String, headers: PyObject, data: PyObject): PyObject {
        val hs = JSON.parseObject<Map<String, String>>(JSON.toJSONString(toJava(headers)), TYPE_HEADERS.type)
        val d = JSON.parseObject<Map<String?, String?>>(JSON.toJSONString(toJava(data)), TYPE_HEADERS.type)
        return toPython(HttpUtil.get(url, hs, d))
    }

    @JvmStatic
    fun toJava(po: PyObject?): Any? {
        if (po == null) {
            return null
        }
        val json = json_str_module!!.call(po)
        return JSON.parse(json.toString())
    }

    /**
     * 将Java对象转换为Python对象
     */
    @JvmStatic
    fun toPython(obj: Any?): PyObject {
        val s = JSON.toJSONString(obj, JSONWriter.Feature.WriteMapNullValue)
        return json_obj_module!!.call(s)
    }
}
