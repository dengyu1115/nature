package org.nature.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Future;

public class PythonUtil {

    private static final TypeReference<Map<String, String>> TYPE_HEADERS = new TypeReference<>() {
    };

    private static boolean initialized = false;

    private static PyObject builtins_module;

    private static PyObject script_module;
    private static PyObject json_module;

    public static void init() {
        if (!initialized) {
            if (!Python.isStarted()) {
                Python.start(new AndroidPlatform(CtxUtil.get()));
                Python instance = Python.getInstance();
                builtins_module = instance.getModule("builtins");
                json_module = Python.getInstance().getModule("json");
                script_module = instance.getModule("nature").get("dynamic_exec");
            }
            initialized = true;
        }
    }

    public static void refresh() {
        DbUtil.refresh();
        CtxUtil.refresh();
    }

    public static Object execScript(String script, JSONObject args) {
        return toJava(script_module.call(script, toPython(args)));
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
                // ignore
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
        // 1. 获取Python原生类型名称（核心：替代isInstance的关键）
        String type = type(po);
        // 2. 基础类型（无嵌套，直接转换）
        switch (type) {
            case "decimal.Decimal":
                return new BigDecimal(po.toString());
            case "datetime.datetime":
                return po.toJava(Date.class);
            case "NoneType":
                return null;
            // 3. 列表/元组（嵌套，递归解析每个元素）
            case "list":
            case "tuple":
                List<Object> list = new ArrayList<>();
                for (PyObject item : po.asList()) {
                    list.add(toJava(item)); // 递归转换子元素
                }
                return list;
            // 4. 集合（嵌套，递归解析）
            case "set":
                Set<Object> set = new HashSet<>();
                for (PyObject item : po.asSet()) {
                    set.add(toJava(item));
                }
                return set;
            // 5. 字典（嵌套，递归解析键值对）
            case "dict":
                Map<Object, Object> map = new HashMap<>();
                for (Map.Entry<PyObject, PyObject> entry : po.asMap().entrySet()) {
                    Object key = toJava(entry.getKey());
                    Object value = toJava(entry.getValue());
                    map.put(key, value);
                }
                return map;
            // 6. 其他类型（自定义处理，比如返回原始对象或字符串）
            default:
                return po.toJava(Object.class);
        }
    }

    private static String type(PyObject po) {
        if (po == null) {
            return "null";
        }
        PyObject pyType = po.type(); // 获取类型对象
        return pyType.toString().replace("<class '", "").replace("'>", "");
    }

    /**
     * 将Java对象转换为Python对象
     */
    public static PyObject toPython(Object obj) {
        String s = JSON.toJSONString(obj, SerializerFeature.WriteMapNullValue);
        return json_module.callAttr("loads", s);
    }
}