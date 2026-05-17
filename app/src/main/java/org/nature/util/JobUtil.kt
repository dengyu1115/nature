package org.nature.util

import com.chaquo.python.PyObject
import com.chaquo.python.Python
import org.nature.config.Config.DB_PATH_JOB
import org.nature.config.Config.SQL_JOB

object JobUtil {

    private val JOB_MAP = HashMap<String, PyObject>()

    @JvmStatic
    fun init() {
        JOB_MAP.clear()
        val instance = Python.getInstance()
        val module = instance.getModule("nature").get("get_job_func")
        val list = DbUtil.list(DB_PATH_JOB, SQL_JOB)
        for (i in list) {
            val name = i["name"] as String
            val script = i["script"] as String
            val func = module!!.call(script)
            JOB_MAP[name] = func
        }
    }

    @JvmStatic
    fun destroy() {
        JOB_MAP.clear()
    }

    @JvmStatic
    fun jobs(): Map<String, PyObject> {
        return JOB_MAP
    }
}
