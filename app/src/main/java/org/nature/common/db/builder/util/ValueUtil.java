package org.nature.common.db.builder.util;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 值处理工具类
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
public class ValueUtil {

    /**
     * 获取JSONObject中的值
     * @param json     json
     * @param property 属性
     * @return Object
     */
    public static Object get(JSONObject json, String property) {
        String[] split = property.split("\\.");
        for (int i = 0; i < split.length - 1; i++) {
            String key = split[i];
            JSONObject temp = json.getJSONObject(key);
            if (temp == null) {
                return null;
            }
            json = temp;
        }
        return json.get(split[split.length - 1]);
    }

    /**
     * 设置JSONObject中的值
     * @param json     json
     * @param property 属性
     * @param value    值
     */
    public static void set(JSONObject json, String property, Object value) {
        String[] split = property.split("\\.");
        for (int i = 0; i < split.length - 1; i++) {
            String key = split[i];
            JSONObject temp = json.getJSONObject(key);
            if (temp == null) {
                json.put(key, temp = new JSONObject());
            }
            json = temp;
        }
        json.put(split[split.length - 1], value);
    }

    /**
     * 属性集合
     * @param where where条件子句
     * @return where条件中所包含的字段集合
     */
    public static List<String> properties(String where) {
        if (where == null) {
            return new ArrayList<>();
        }
        List<String> properties = new ArrayList<>();
        int s = 0, e = 0;
        while (true) {
            s = where.indexOf("#{", s);
            if (s == -1) {
                break;
            }
            s = s + 2;
            e = where.indexOf("}", e);
            if (e == -1) {
                break;
            }
            properties.add(where.substring(s, e));
            s = e;
            e = e + 1;
        }
        return properties;
    }

}
