package org.nature.common.db.builder.util;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ValueUtil {

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
