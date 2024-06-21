package org.nature.common.ioc.holder;

import org.nature.common.db.annotation.TableModel;
import org.nature.common.db.proxy.MapperProxy;
import org.nature.common.exception.Warn;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.JobExec;
import org.nature.common.ioc.annotation.PageView;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 实例持有
 * @author nature
 * @version 1.0.0
 * @since 2019/11/21 16:33
 */
public class InstanceHolder {

    /**
     * 类-实例 上下文
     */
    private static final Map<Class<?>, Map<String, Object>> CTX = new HashMap<>();

    /**
     * 获取实例
     * @param cls 实例class
     * @return 实例
     */
    @SuppressWarnings("all")
    public static <T> T get(Class<T> cls) {
        Map<String, Object> map = CTX.get(cls);
        if (map == null) {
            throw new Warn("instance does't exist in the context:" + cls);
        }
        Collection<Object> values = map.values();
        if (values.isEmpty()) {
            throw new Warn("instance collection is empty:" + cls);
        }
        if (values.size() > 1) {
            throw new Warn("instance is not singleton:" + cls);
        }
        return (T) values.toArray()[0];
    }

    @SuppressWarnings("all")
    public static <T> List<T> list(Class<T> cls) {
        Map<String, Object> map = CTX.getOrDefault(cls, new HashMap<>());
        return Arrays.stream(map.values().toArray()).map(i -> (T) i).collect(Collectors.toList());
    }

    @SuppressWarnings("all")
    public static <T> Map<String, T> map(Class<T> cls) {
        Map<String, Object> map = CTX.getOrDefault(cls, new HashMap<>());
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, i -> (T) i.getValue()));
    }

    @SuppressWarnings("all")
    public static <T> void add(Class<T> cls) {
        Object o;
        if (cls.isAnnotationPresent(Component.class)
                || cls.isAnnotationPresent(PageView.class)
                || cls.isAnnotationPresent(JobExec.class)) {
            try {
                o = cls.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (cls.isAnnotationPresent(TableModel.class)) {
            o = MapperProxy.instant(cls);
        } else {
            throw new Warn("class is not marked as component:" + cls);
        }
        // 添加至上下文
        add(cls, cls.getName(), o);
    }

    /**
     * 添加至上下文
     * @param cls  类
     * @param name 名称
     * @param o    实例
     * @param <T>  类型
     */
    private static <T> void add(Class<T> cls, String name, Object o) {
        if (cls == null) {
            return;
        }
        // 获取类对应的map，往map中按名称添加实例对象
        Map<String, Object> map = CTX.computeIfAbsent(cls, k -> new HashMap<>());
        map.put(name, o);
        Class<?>[] interfaces = cls.getInterfaces();
        for (Class<?> i : interfaces) {
            // 接口添加对象
            add(i, name, o);
        }
        Class<? super T> sc = cls.getSuperclass();
        // 顶级Object类不处理
        if (Object.class.equals(sc)) {
            return;
        }
        // 父类添加对象
        add(sc, name, o);
    }

}
