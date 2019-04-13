package com.asjngroup.ncash.common.hibernate.annotation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * User: mike.quilleash
 * Date: 12-Oct-2006
 * Time: 17:08:51
 */
@Target({ANNOTATION_TYPE})
@Retention(RUNTIME)
public @interface Index
{
    String name();

    String[] columnNames();

    boolean unique() default true;

    boolean businessConstraint() default false;

    boolean displayName() default false;
}
