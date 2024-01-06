package org.nature.common.db.builder.util;

import org.apache.commons.lang3.StringUtils;
import org.nature.common.db.annotation.Param;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class MethodUtil {


    public static String[] listName(Method method) {
        Annotation[][] annotations = method.getParameterAnnotations();
        int length = annotations.length;
        if (length == 1) {
            // 如果只有一个参数可以不用@Param标记
            return new String[]{getParamName(annotations[0])};

        }
        String[] array = new String[length];
        for (int i = 0; i < length; i++) {
            Annotation[] ans = annotations[i];
            if (ans == null || ans.length == 0) {
                throw new RuntimeException("param must be marked as param by @Param");
            }

            String name = getParamName(ans);
            if (StringUtils.isBlank(name)) {
                throw new RuntimeException("param name is blank");
            }
            array[i] = name;
        }
        return array;
    }

    private static String getParamName(Annotation[] ans) {
        if (ans == null || ans.length == 0) {
            return null;
        }
        for (Annotation an : ans) {
            Class<? extends Annotation> cls = an.annotationType();
            if (cls == Param.class) {
                return ((Param) an).value();
            }
        }
        throw new RuntimeException("param must be marked as param by @Param");
    }
}
