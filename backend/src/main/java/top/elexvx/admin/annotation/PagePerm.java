package top.elexvx.admin.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 页面动作权限注解。
 *
 * <p>会在切面中转换为 system:{routeName}:{action} 并执行权限校验。
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface PagePerm {
  String routeName();

  String action();
}
