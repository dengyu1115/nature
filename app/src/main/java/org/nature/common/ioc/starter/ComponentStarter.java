package org.nature.common.ioc.starter;

import android.content.Context;
import dalvik.system.DexFile;
import org.nature.common.db.annotation.TableModel;
import org.nature.common.exception.Warn;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.JobExec;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.ioc.holder.InstanceHolder;
import org.nature.common.ioc.holder.JobHolder;
import org.nature.common.ioc.holder.PageHolder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
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
    @SuppressWarnings("all")
    public synchronized void start(Context ctx) {
        // 控制只执行一次
        if (ran) {
            return;
        }
        String path = ctx.getPackageResourcePath();
        DexFile home;
        try {
            home = new DexFile(path);
        } catch (IOException e) {
            throw new Warn("dex file init error:" + e.getMessage());
        }
        Enumeration<String> entries = home.entries();
        List<Class<?>> classes = this.collect(entries);
        for (Class<?> i : classes) {
            InstanceHolder.add(i);
        }
        for (Class<?> i : classes) {
            this.inject(i);
        }
        ran = true;

    }

    private List<Class<?>> collect(Enumeration<String> entries) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            throw new RuntimeException("no context class loader");
        }
        List<Class<?>> classes = new ArrayList<>();
        while (entries.hasMoreElements()) {
            String element = entries.nextElement();
            if (!element.startsWith("org.nature") || element.contains("$$ExternalSyntheticLambda")) {
                continue;
            }
            Class<?> cls;
            try {
                cls = loader.loadClass(element);
            } catch (ClassNotFoundException e) {
                throw new Warn("class not found:" + e.getMessage());
            }
            PageView pageView = cls.getAnnotation(PageView.class);
            if (pageView != null) {
                PageHolder.register(cls, pageView);
            }
            if (!this.isNeedType(cls)) {
                continue;
            }
            classes.add(cls);
        }
        return classes;
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
     * 判断是否满足类型
     * @param cls 类
     * @return boolean
     */
    private boolean isNeedType(Class<?> cls) {
        int modifiers = cls.getModifiers();
        return !cls.isInterface() && !Modifier.isAbstract(modifiers) && Modifier.isPublic(modifiers)
                && (cls.getAnnotation(Component.class) != null
                || cls.getAnnotation(JobExec.class) != null)
                || cls.isInterface() && cls.getAnnotation(TableModel.class) != null;
    }

}
