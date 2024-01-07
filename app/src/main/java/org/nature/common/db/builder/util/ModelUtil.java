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

/**
 * model类工具
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
public class ModelUtil {

    /**
     * model与db关系数据map
     */
    private static final Map<Class<?>, String> MODEL_DB = new HashMap<>();
    /**
     * model与table关系数据map
     */
    private static final Map<Class<?>, String> MODEL_TABLE = new HashMap<>();
    /**
     * model与column关系数据map
     */
    private static final Map<Class<?>, List<Mapping>> MODEL_MAPPINGS = new HashMap<>();
    /**
     * model与ID字段关系数据map
     */
    private static final Map<Class<?>, List<Mapping>> MODEL_ID_MAPPINGS = new HashMap<>();
    /**
     * model与非ID字段关系数据map
     */
    private static final Map<Class<?>, List<Mapping>> MODEL_NONE_ID_MAPPINGS = new HashMap<>();
    /**
     * model与结果转换关系数据map
     */
    private static final Map<Class<?>, Function<Cursor, ?>> MODEL_RESULT_MAP = new HashMap<>();

    /**
     * 获取Model标记
     * @param cls 类
     * @return Model
     */
    public static Model getModel(Class<?> cls) {
        Model model = cls.getAnnotation(Model.class);
        if (model == null) {
            throw new RuntimeException(String.format("model class %s should be marked with Model", cls));
        }
        return model;
    }

    /**
     * 获取db
     * @param cls 类
     * @return String
     */
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

    /**
     * 获取model对应的表
     * @param cls 类
     * @return String
     */
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

    /**
     * 获取mapping集合
     * @param cls 类
     * @return list
     */
    public static List<Mapping> listMapping(Class<?> cls) {
        return getFromCache(cls, MODEL_MAPPINGS, i -> {
            Set<Mapping> fields = new HashSet<>();
            Model model = getModel(i);
            Set<String> excludeFields = Arrays.stream(model.excludeFields()).collect(Collectors.toSet());
            doAddFields(i, fields, excludeFields, "", "");
            return new ArrayList<>(fields);
        });
    }

    /**
     * 获取id映射数据集合
     * @param cls 类
     * @return list
     */
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

    /**
     * 获取非id字段映射数据集合
     * @param cls 类
     * @return list
     */
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

    /**
     * 结果映射获取
     * @param cls 类
     * @return 结果映射
     */
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
                        // 根据java字段类型设置值
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
                    // json转换为java对象
                    return JSON.toJavaObject(json, cls);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        });

    }

    /**
     * 从缓存中获取
     * @param cls  类
     * @param map  缓存map
     * @param func 设置缓存的处理逻辑
     * @return T
     */
    private static <T> T getFromCache(Class<?> cls, Map<Class<?>, T> map, Function<Class<?>, T> func) {
        T t = map.get(cls);
        // 缓存存在直接返回
        if (t != null) {
            return t;
        }
        //  缓存中没有，则设置缓存
        t = func.apply(cls);
        map.put(cls, t);
        // 设置好缓存后返回
        return t;
    }

    /**
     * 添加字段处理
     * @param cls           类
     * @param mappings      映射数据集合
     * @param excludeFields 排除的字段集合
     * @param scope         范围
     * @param prefix        前缀
     */
    private static void doAddFields(Class<?> cls, Set<Mapping> mappings, Set<String> excludeFields, String scope, String prefix) {
        // Object类不需要处理
        if (cls == Object.class) {
            return;
        }
        // 处理父类
        doAddFields(cls.getSuperclass(), mappings, excludeFields, scope, prefix);
        // 获取类涉及的字段
        Field[] declaredFields = cls.getDeclaredFields();
        // java字段与映射数据形成map
        Map<String, Mapping> map = mappings.stream().collect(Collectors.toMap(Mapping::getProperty, i -> i));
        // 遍历字段
        for (Field field : declaredFields) {
            String name = field.getName();
            // 字段标记为排除了则不处理
            if (excludeFields.contains(name)) {
                continue;
            }
            // 生成字段完整名称
            if (StringUtils.isNotBlank(scope)) {
                name = scope + "." + name;
            }
            // 处理对象类型字段
            Hold hold = field.getAnnotation(Hold.class);
            if (hold == null) {
                Mapping mapping = map.get(name);
                // 移除已存在的映射数据，子类覆盖父类
                if (mapping != null) {
                    mappings.remove(mapping);
                }
                // 获取字段
                String column = getColumn(field);
                // 添加前缀
                if (StringUtils.isNotBlank(prefix)) {
                    column = prefix + column;
                }
                // 获取id字段
                Id id = field.getAnnotation(Id.class);
                // 添加值mapping集合
                mappings.add(new Mapping(name, column, field.getType(), id == null ? null : id.order()));
            } else {
                // 递归处理关联对象
                excludeFields = Arrays.stream(hold.excludeFields()).collect(Collectors.toSet());
                doAddFields(field.getType(), mappings, excludeFields, name, prefix + hold.prefix());
            }
        }
    }

    /**
     * 获取字段列名
     * @param field 字段
     * @return String
     */
    private static String getColumn(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column != null) {
            // 有@Column注解，则使用其value值
            return column.value();
        }
        //  没有@Column注解，则使用字段名生成
        String name = field.getName();
        return name.replaceAll("[A-Z]", "_$0").toLowerCase();
    }


}
