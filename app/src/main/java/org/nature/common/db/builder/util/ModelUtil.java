package org.nature.common.db.builder.util;

import android.database.Cursor;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.nature.common.db.DB;
import org.nature.common.db.annotation.Column;
import org.nature.common.db.annotation.Hold;
import org.nature.common.db.annotation.Id;
import org.nature.common.db.annotation.Model;
import org.nature.common.db.builder.model.Mapping;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ModelUtil {

    private static final Map<Class<?>, String> MODEL_DB = new HashMap<>();

    private static final Map<Class<?>, String> MODEL_TABLE = new HashMap<>();

    private static final Map<Class<?>, List<Mapping>> MODEL_MAPPINGS = new HashMap<>();

    private static final Map<Class<?>, List<Mapping>> MODEL_ID_MAPPINGS = new HashMap<>();

    private static final Map<Class<?>, List<Mapping>> MODEL_NONE_ID_MAPPINGS = new HashMap<>();

    private static final Map<Class<?>, Function<Cursor, ?>> MODEL_RESULT_MAP = new HashMap<>();

    public static Model getModel(Class<?> cls) {
        Model model = cls.getAnnotation(Model.class);
        if (model == null) {
            throw new RuntimeException(String.format("model class %s should be marked with Model", cls));
        }
        return model;
    }

    public static String db(Class<?> cls) {
        return getFromCache(cls, MODEL_DB, i -> {
            Model model = getModel(i);
            String db = model.db();
            if (StringUtils.isBlank(db)) {
                throw new RuntimeException("db should not be blank");
            }
            return db;
        });
    }

    public static String table(Class<?> cls) {
        return getFromCache(cls, MODEL_TABLE, i -> {
            Model model = getModel(i);
            String table = model.table();
            if (StringUtils.isBlank(table)) {
                throw new RuntimeException("table should not be blank");
            }
            return table;
        });
    }

    public static List<Mapping> listMapping(Class<?> cls) {
        return getFromCache(cls, MODEL_MAPPINGS, i -> {
            Set<Mapping> fields = new HashSet<>();
            Model model = getModel(i);
            Set<String> excludeFields = Arrays.stream(model.excludeFields()).collect(Collectors.toSet());
            doAddFields(i, fields, excludeFields, "", "");
            return new ArrayList<>(fields);
        });
    }

    public static List<Mapping> listIdMapping(Class<?> cls) {
        return getFromCache(cls, MODEL_ID_MAPPINGS, i -> {
            List<Mapping> list = listMapping(i);
            list = list.stream().filter(o -> o.getIdOrder() != null)
                    .sorted(Comparator.comparing(Mapping::getIdOrder)).collect(Collectors.toList());
            if (list.isEmpty()) {
                throw new RuntimeException("no field marked as Id");
            }
            return list;
        });
    }

    public static List<Mapping> listNoneIdMapping(Class<?> cls) {
        return getFromCache(cls, MODEL_NONE_ID_MAPPINGS, i -> {
            List<Mapping> list = listMapping(i);
            list = list.stream().filter(o -> o.getIdOrder() == null).collect(Collectors.toList());
            if (list.isEmpty()) {
                throw new RuntimeException("no field not marked as Id");
            }
            return list;
        });
    }

    public static Function<Cursor, ?> resultMap(Class<?> cls) {
        return getFromCache(cls, MODEL_RESULT_MAP, i -> {
            List<Mapping> mappings = listMapping(cls);
            return cursor -> {
                try {
                    JSONObject json = new JSONObject();
                    for (Mapping mapping : mappings) {
                        String property = mapping.getProperty();
                        String column = mapping.getColumn().replace("`", "");
                        Class<?> type = mapping.getType();
                        if (type == Integer.class || type == int.class) {
                            ValueUtil.set(json, property, DB.getInt(cursor, column));
                        } else if (type == Double.class || type == double.class) {
                            ValueUtil.set(json, property, DB.getDouble(cursor, column));
                        } else if (type == BigDecimal.class) {
                            ValueUtil.set(json, property, DB.getDecimal(cursor, column));
                        } else if (type == String.class) {
                            ValueUtil.set(json, property, DB.getString(cursor, column));
                        } else {
                            throw new IllegalArgumentException("type not support:" + cls);
                        }
                    }
                    return JSON.toJavaObject(json, cls);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        });

    }

    private static <T> T getFromCache(Class<?> cls, Map<Class<?>, T> map, Function<Class<?>, T> func) {
        T t = map.get(cls);
        if (t != null) {
            return t;
        }
        t = func.apply(cls);
        map.put(cls, t);
        return t;
    }

    private static void doAddFields(Class<?> cls, Set<Mapping> mappings, Set<String> excludeFields, String scope, String prefix) {
        if (cls == Object.class) {
            return;
        }
        doAddFields(cls.getSuperclass(), mappings, excludeFields, scope, prefix);
        Field[] declaredFields = cls.getDeclaredFields();
        Map<String, Mapping> map = mappings.stream().collect(Collectors.toMap(Mapping::getProperty, i -> i));
        for (Field field : declaredFields) {
            String name = field.getName();
            if (excludeFields.contains(name)) {
                continue;
            }
            Hold hold = field.getAnnotation(Hold.class);
            if (StringUtils.isNotBlank(scope)) {
                name = scope + "." + name;
            }
            String column = getColumn(field);
            if (StringUtils.isNotBlank(prefix)) {
                column = prefix + column;
            }
            if (hold == null) {
                Mapping mapping = map.get(name);
                if (mapping != null) {
                    mappings.remove(mapping);
                }
                Id id = field.getAnnotation(Id.class);
                mappings.add(new Mapping(name, column, field.getType(), id == null ? null : id.order()));
            } else {
                excludeFields = Arrays.stream(hold.excludeFields()).collect(Collectors.toSet());
                doAddFields(field.getType(), mappings, excludeFields, name, prefix + hold.prefix());
            }
        }
    }

    private static String getColumn(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column != null) {
            return column.value();
        }
        String name = field.getName();
        return name.replaceAll("[A-Z]", "_$0").toLowerCase();
    }


}
