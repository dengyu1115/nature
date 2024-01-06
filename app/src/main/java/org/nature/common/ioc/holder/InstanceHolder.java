package org.nature.common.ioc.holder;

import org.nature.common.db.annotation.TableModel;
import org.nature.common.db.proxy.MapperProxy;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.JobExec;
import org.nature.common.ioc.annotation.PageView;

import java.util.HashMap;
import java.util.Map;

/**
 * 实例持有
 * @author nature
 * @version 1.0.0
 * @since 2019/11/21 16:33
 */
public class InstanceHolder {

    /**
     * 存放实例的map
     */
    private static final Map<Class<?>, Object> map = new HashMap<>();

    public static <T> void put(Class<T> cls, T o) {
        map.put(cls, o);
    }

    /**
     * 获取实例
     * @param cls 实例class
     * @param <T> 类型
     * @return 实例
     */
    @SuppressWarnings("all")
    public static <T> T get(Class<T> cls) {
        Object o = map.get(cls);
        if (o != null) {
            return (T) o;
        }
        synchronized (cls) { // 单例存放
            if ((o = map.get(cls)) != null) {
                return (T) o;
            }
            if (cls.isAnnotationPresent(Component.class)
                    || cls.isAnnotationPresent(PageView.class)
                    || cls.isAnnotationPresent(JobExec.class)) {
                try {
                    map.put(cls, o = cls.newInstance());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (cls.isAnnotationPresent(TableModel.class)) {
                map.put(cls, o = MapperProxy.instant(cls));
            } else {
                throw new RuntimeException("class is not marked as component:" + cls);
            }
        }
        return (T) o;
    }

}
