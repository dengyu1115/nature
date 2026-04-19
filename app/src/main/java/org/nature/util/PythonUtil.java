package org.nature.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import org.nature.exception.Warn;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class PythonUtil {

    private static final TypeReference<Map<String, String>> TYPE_HEADERS = new TypeReference<>() {
    };

    private static boolean initialized = false;

    private static PyObject builtins_module;

    private static PyObject script_module;
    private static PyObject func_module;
    private static PyObject json_obj_module;
    private static PyObject json_str_module;

    public static void init() {
        if (!initialized) {
            if (!Python.isStarted()) {
                Python.start(new AndroidPlatform(CtxUtil.get()));
                Python instance = Python.getInstance();
                builtins_module = instance.getModule("builtins");
                script_module = instance.getModule("nature").get("dynamic_exec");
                func_module = instance.getModule("nature").get("module_func_exec");
                json_str_module = instance.getModule("nature").get("to_json");
                json_obj_module = Python.getInstance().getModule("json").get("loads");
                instance.getModule("module_script");
            }
            initialized = true;
        }
    }

    public static void refresh() {
        DbUtil.refresh();
        CtxUtil.refresh();
    }

    public static Object execScript(String script, JSONObject args) {
        try {
            return toJava(script_module.call(script, toPython(args)));
        } catch (PyException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Warn) {
                throw (Warn) cause;
            }
            throw e;
        }
    }

    public static Object execModule(String module, String func, JSONObject args) {
        try {
            return toJava(func_module.call(module, func, toPython(args)));
        } catch (PyException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Warn) {
                throw (Warn) cause;
            }
            throw e;
        }
    }

    public static PyObject multiThread(PyObject items, PyObject run) {
        // 任务结果获取集合
        List<Future<PyObject>> cl = new LinkedList<>();
        // 提交任务
        items.asList().forEach(i -> cl.add(ExecUtil.submit(() -> run.call(i))));
        PyObject list = builtins_module.callAttr("list");
        cl.forEach(i -> {
            try {
                list.callAttr("append", i.get());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return list;
    }

    public static PyObject find(String path, String sql) {
        return toPython(DbUtil.find(path, sql));
    }

    public static PyObject list(String path, String sql) {
        List<Map<String, Object>> list = DbUtil.list(path, sql);
        return toPython(list);
    }

    public static PyObject update(String path, String sql) {
        return toPython(DbUtil.update(path, sql));
    }

    public static void ddl(String path, String sql) {
        DbUtil.ddl(path, sql);
    }

    public static PyObject post(String url, PyObject headers, PyObject data) {
        Map<String, String> hs = JSON.parseObject(JSON.toJSONString(toJava(headers)), TYPE_HEADERS);
        return toPython(HttpUtil.post(url, hs, JSON.parseObject(JSON.toJSONString(toJava(data)))));
    }

    public static PyObject get(String url, PyObject headers, PyObject data) {
        Map<String, String> hs = JSON.parseObject(JSON.toJSONString(toJava(headers)), TYPE_HEADERS);
        Map<String, String> d = JSON.parseObject(JSON.toJSONString(toJava(data)), TYPE_HEADERS);
        return toPython(HttpUtil.get(url, hs, d));
    }

    public static Object toJava(PyObject po) {
        if (po == null) {
            return null;
        }
        PyObject json = json_str_module.call(po);
        return JSON.parse(json.toString());
    }

    /**
     * 将Java对象转换为Python对象
     */
    public static PyObject toPython(Object obj) {
        String s = JSON.toJSONString(obj, SerializerFeature.WriteMapNullValue);
        return json_obj_module.call(s);
    }
}