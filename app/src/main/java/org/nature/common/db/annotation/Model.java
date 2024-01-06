package org.nature.common.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Model {

    String db() default "nature/system.db";

    String table();

    String[] excludeFields() default {};

    boolean recreate() default false;
}
