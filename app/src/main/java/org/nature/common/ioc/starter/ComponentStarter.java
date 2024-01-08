package org.nature.common.ioc.starter;

import android.content.Context;
import dalvik.system.DexFile;
import org.nature.common.db.annotation.TableModel;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.JobExec;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.ioc.holder.InstanceHolder;
import org.nature.common.ioc.holder.JobHolder;
import org.nature.common.ioc.holder.PageHolder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * 组件初始化处理器
 * @author nature
 * @version 1.0.0
 * @since 2019/8/6 12:41
 */
public class ComponentStarter {

    /**
     * 单例
     */
    private static ComponentStarter instance;
    /**
     * 启动标识
     */
    private boolean ran;

    private ComponentStarter() {
    }

    /**
     * 获取实例
     * @return 实例
     */
    public static ComponentStarter getInstance() {
        if (instance != null) {
            return instance;
        }
        synchronized (ComponentStarter.class) {
            if (instance == null) {
                instance = new ComponentStarter();
            }
        }
        return instance;
    }

    /**
     * 开始注入/实例化处理
     * @param ctx projection context
     */
    public synchronized void start(Context ctx) {
        // ran,no secondary running
        if (ran) {
            return;
        }
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            if (loader == null) {
                throw new RuntimeException("no context class loader");
            }
            String path = ctx.getPackageResourcePath();
            DexFile home = new DexFile(path);
            Enumeration<String> entries = home.entries();
            while (entries.hasMoreElements()) {
                String element = entries.nextElement();
                if (!element.startsWith("org.nature") || element.contains("$$ExternalSyntheticLambda")
                        || !this.isNeededPath(element)) {
                    continue;
                }
                Class<?> cls = loader.loadClass(element);
                if (!this.isNeedType(cls)) {
                    continue;
                }
                this.inject(cls);
            }
            ran = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 注入
     * @param cls 类
     */
    private void inject(Class<?> cls) {
        Component component = cls.getAnnotation(Component.class);
        if (component != null) {
            this.inject(cls, InstanceHolder.get(cls));
        }
        TableModel tableModel = cls.getAnnotation(TableModel.class);
        if (tableModel != null) {
            this.inject(cls, InstanceHolder.get(cls));
        }
        PageView pageView = cls.getAnnotation(PageView.class);
        if (pageView != null) {
            this.inject(cls, InstanceHolder.get(cls));
            PageHolder.register(cls, pageView);
        }
        JobExec jobExec = cls.getAnnotation(JobExec.class);
        if (jobExec != null) {
            Object o = InstanceHolder.get(cls);
            this.inject(cls, o);
            JobHolder.register(jobExec, o);
        }
    }

    /**
     * 注入
     * @param cls 类
     * @param o   实例
     */
    private void inject(Class<?> cls, Object o) {
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Injection.class)) {
                Class<?> type = field.getType();
                field.setAccessible(true);
                try {
                    field.set(o, InstanceHolder.get(type));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        Class<?> sc = cls.getSuperclass();
        if (sc != null && !sc.equals(Object.class)) {
            this.inject(sc, o);
        }
    }

    /**
     * 判断是否需要扫描
     * @param element 元素
     * @return boolean
     */
    private boolean isNeededPath(String element) {
        List<String> paths = Arrays.asList(".page", ".job", ".manager", ".service", ".mapper");
        for (String path : paths) {
            if (element.contains(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否满足类型
     * @param cls 类
     * @return boolean
     */
    private boolean isNeedType(Class<?> cls) {
        int modifiers = cls.getModifiers();
        return !Modifier.isAbstract(modifiers) && Modifier.isPublic(modifiers);
    }

}
