package org.nature.util;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.nature.config.Config.DB_PATH_JOB;
import static org.nature.config.Config.SQL_JOB;

public class JobUtil {

    private static Map<String, PyObject> jobMap = new HashMap<>();

    public static void init() {
        Python instance = Python.getInstance();
        PyObject module = instance.getModule("nature").get("get_job_func");
        List<Map<String, Object>> list = DbUtil.list(DB_PATH_JOB, SQL_JOB);
        for (Map<String, Object> i : list) {
            String name = (String) i.get("name");
            String script = (String) i.get("script");
            PyObject func = module.call(script);
            jobMap.put(name, func);
        }
    }

    public static void destroy() {
        jobMap.clear();
    }

    public static Map<String, PyObject> jobs() {
        return jobMap;
    }
}
