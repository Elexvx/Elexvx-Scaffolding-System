package top.elexvx.admin.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * AI 可调用功能注解
 * 标记在 Service 方法上，表示该方法可以被 AI 智能助手调用
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AiFunction {
    /**
     * 功能名称，默认为方法名
     */
    String name() default "";

    /**
     * 功能描述，用于告诉 AI 这个功能是做什么的
     */
    String description();

    /**
     * 允许调用该功能所需的权限标识
     */
    String[] requiredPermissions() default {};

    /**
     * 是否为危险操作（删除、重置类），AI 默认禁止调用
     */
    boolean dangerous() default false;
}
