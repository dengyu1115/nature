package org.nature.common.util;

import com.alibaba.fastjson.JSONObject;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import android.content.Context;

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

    public static PyObject execScript(String script, JSONObject args) {
        Python py = Python.getInstance();
        PyObject module = py.getModule("nature");
        PyObject method = module.get("dynamic_exec");
        return method.callAttr(script, args);
    }
}