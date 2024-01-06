package org.nature.common.db.builder.source.annotated;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.nature.common.db.builder.source.definition.AnnotatedSource;
import org.nature.common.db.builder.util.MethodUtil;
import org.nature.common.db.builder.util.SqlBuilder;
import org.nature.common.db.builder.util.ValueUtil;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseAnnotatedSource implements AnnotatedSource {


    protected SqlBuilder genSql(Class<?> cls, String where, Method method, Object... args) {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("no args");
        }
        String[] names = MethodUtil.listName(method);
        JSONObject o = new JSONObject();
        if (args.length == 1) {
            Object arg = JSON.toJSON(args[0]);
            // 不是对象，那么必须使用@Param命名变量
            String name = names[0];
            if (name == null && !(arg instanceof JSONObject)) {
                throw new IllegalArgumentException("primitive param must be named by @Param");
            }
            if (name != null) {
                o.put(name, arg);
            } else {
                o = (JSONObject) arg;
            }
        } else {
            for (int i = 0; i < args.length; i++) {
                String name = names[i];
                o.put(name, JSON.toJSON(args[i]));
            }
        }
        SqlBuilder builder = this.initSqlBuilder(cls);
        List<String> properties = ValueUtil.properties(where);
        for (String property : properties) {
            Object obj = ValueUtil.get(o, property);
            if (obj instanceof List) {
                List<?> list = (List<?>) obj;
                where = where.replace("#{" + property + "}",
                        list.stream().map(i -> "?").collect(Collectors.joining(",")));
                for (Object i : list) {
                    builder.appendArg(i);
                }
            } else {
                where = where.replace("#{" + property + "}", "?");
                builder.appendArg(obj);
            }
        }
        builder.append(where);
        return builder;
    }

    protected abstract SqlBuilder initSqlBuilder(Class<?> cls);

}
