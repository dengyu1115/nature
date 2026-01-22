package org.nature.common.util;

import android.content.Context;
import com.alibaba.fastjson.JSONObject;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.util.*;

public class PythonUtil {

    private static boolean initialized = false;


    public static void init(Context context) {
        if (!initialized) {
            if (!Python.isStarted()) {
                Python.start(new AndroidPlatform(context));
            }
            initialized = true;
        }
    }

    public static Object execScript(String script, JSONObject args) {
        Python py = Python.getInstance();
        PyObject module = py.getModule("nature");
        PyObject method = module.get("dynamic_exec");
        return convert(method.call(script, args));
    }

    private static Object convert(PyObject po) {
        if (po == null) {
            return null;
        }

        // 1. 获取Python原生类型名称（核心：替代isInstance的关键）
        String type = type(po);

        // 2. 基础类型（无嵌套，直接转换）
        switch (type) {
            case "int":
                return po.toJava(Integer.class);
            case "float":
                return po.toJava(Double.class);
            case "str":
                return po.toJava(String.class);
            case "bool":
                return po.toJava(Boolean.class);
            case "NoneType": // Python的None对应类型名
                return null;

            // 3. 列表/元组（嵌套，递归解析每个元素）
            case "list":
            case "tuple":
                List<Object> javaList = new ArrayList<>();
                for (PyObject item : po.asList()) {
                    javaList.add(convert(item)); // 递归转换子元素
                }
                return javaList;

            // 4. 集合（嵌套，递归解析）
            case "set":
                Set<Object> javaSet = new HashSet<>();
                for (PyObject item : po.asSet()) {
                    javaSet.add(convert(item));
                }
                return javaSet;

            // 5. 字典（嵌套，递归解析键值对）
            case "dict":
                Map<Object, Object> javaMap = new HashMap<>();
                for (Map.Entry<PyObject, PyObject> entry : po.asMap().entrySet()) {
                    Object key = convert(entry.getKey());
                    Object value = convert(entry.getValue());
                    javaMap.put(key, value);
                }
                return javaMap;

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
}