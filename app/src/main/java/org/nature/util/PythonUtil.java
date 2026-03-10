package org.nature.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
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


    public static void init() {
        if (!initialized) {
            if (!Python.isStarted()) {
                Python.start(new AndroidPlatform(CtxUtil.get()));
            }
            initialized = true;
        }
    }

    public static void refresh() {
        DbUtil.refresh();
        CtxUtil.refresh();
    }

    public static Object execScript(String script, JSONObject args) {
        Python py = Python.getInstance();
        PyObject module = py.getModule("nature");
        PyObject method = module.get("dynamic_exec");
        return toJava(method.call(script, toPython(args)));
    }

    public static PyObject multiThread(PyObject items, PyObject run) {
        // 任务结果获取集合
        List<Future<PyObject>> cl = new LinkedList<>();
        // 提交任务
        items.asList().forEach(i -> cl.add(ExecUtil.submit(() -> run.call(i))));
        PyObject builtins = Python.getInstance().getModule("builtins");
        PyObject list = builtins.callAttr("list");
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
        return toPython(DbUtil.list(path, sql));
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
            case "int":
                return po.toJava(Integer.class);
            case "float":
                return po.toJava(Double.class);
            case "str":
                return po.toJava(String.class);
            case "bool":
                return po.toJava(Boolean.class);
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
        if (obj == null) {
            return null;
        }
        if (obj instanceof PyObject) {
            return (PyObject) obj;
        }
        // 基础类型直接转换
        if (obj instanceof Double) {
            Python python = Python.getInstance();
            PyObject module = python.getModule("builtins");
            return module.callAttr("float", obj);
        }
        if (obj instanceof Date) {
            Python python = Python.getInstance();
            PyObject module = python.getModule("datetime");
            PyObject datetime = module.get("datetime");
            return datetime.callAttr("fromtimestamp", ((Date) obj).getTime() / 1000d);
        }
        // BigDecimal转为python的Decimal
        if (obj instanceof BigDecimal) {
            Python python = Python.getInstance();
            // 1. 获取Python的decimal模块
            PyObject decimalModule = python.getModule("decimal");
            // 2. 获取decimal模块中的Decimal类
            PyObject decimalClass = decimalModule.get("Decimal");
            return decimalClass.call(((BigDecimal) obj).toPlainString());
        }
        // 处理列表和数组
        if (obj instanceof Collection) {
            Collection<?> collection = (Collection<?>) obj;
            PyObject builtins = Python.getInstance().getModule("builtins");
            PyObject pyList = builtins.callAttr("list");
            for (Object item : collection) {
                pyList.callAttr("append", toPython(item));
            }
            return pyList;
        }
        // 处理Map（字典）
        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            PyObject builtins = Python.getInstance().getModule("builtins");
            PyObject pyDict = builtins.callAttr("dict");
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                PyObject key = toPython(entry.getKey());
                PyObject value = toPython(entry.getValue());
                pyDict.callAttr("__setitem__", key, value);
            }
            return pyDict;
        }
        // 其他类型尝试直接转换
        return PyObject.fromJava(obj);
    }
}