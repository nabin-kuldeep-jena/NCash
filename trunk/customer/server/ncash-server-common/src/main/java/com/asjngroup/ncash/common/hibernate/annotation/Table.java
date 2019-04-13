package com.asjngroup.ncash.common.hibernate.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * User: nabin.jena
 * Date: 29-Mar-2017
 * Time: 23:08:21
 */
@Target({TYPE})
@Retention(RUNTIME)
public @interface Table
{
    Index[] indexes() default {};

    String prefix();
}
