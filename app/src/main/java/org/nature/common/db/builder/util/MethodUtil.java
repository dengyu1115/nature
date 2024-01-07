package org.nature.common.db.builder.util;

import org.apache.commons.lang3.StringUtils;
import org.nature.common.db.annotation.Param;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 方法处理工具类
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/6
 */
public class MethodUtil {

    /**
     * 获取方法参数的@Param注解的值
     * @param method 方法
     * @return String[]
     */
    public static String[] listName(Method method) {
        Annotation[][] annotations = method.getParameterAnnotations();
        int length = annotations.length;
        if (length == 1) {
            // 如果只有一个参数可以不用@Param标记
            return new String[]{getParamName(annotations[0])};

        }
        //  多个参数必须用@Param标记
        String[] array = new String[length];
        for (int i = 0; i < length; i++) {
            Annotation[] ans = annotations[i];
            if (ans == null || ans.length == 0) {
                throw new RuntimeException("param must be marked by @Param");
            }
            // 获取@Param注解的值
            String name = getParamName(ans);
            if (StringUtils.isBlank(name)) {
                throw new RuntimeException("param name is blank");
            }
            array[i] = name;
        }
        return array;
    }

    /**
     * 获取参数名
     * @param ans 注解
     * @return String
     */
    private static String getParamName(Annotation[] ans) {
        if (ans == null || ans.length == 0) {
            return null;
        }
        for (Annotation an : ans) {
            Class<? extends Annotation> cls = an.annotationType();
            if (cls == Param.class) {
                // 如果是Param注解，返回注解标记的名称
                return ((Param) an).value();
            }
        }
        throw new RuntimeException("param must be marked by @Param");
    }
}
