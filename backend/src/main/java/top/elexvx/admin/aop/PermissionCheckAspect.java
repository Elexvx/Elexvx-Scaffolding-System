package top.elexvx.admin.aop;

import top.elexvx.admin.annotation.PagePerm;
import top.elexvx.admin.annotation.RequirePerm;
import top.elexvx.admin.security.AccessControlService;
import top.elexvx.admin.service.PermissionCodeService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * 控制器权限切面。
 *
 * <p>统一处理 @PagePerm / @RequirePerm，避免权限校验散落在每个接口内手写。
 */
@Aspect
@Component
public class PermissionCheckAspect {
  private final AccessControlService accessControlService;
  private final PermissionCodeService permissionCodeService;

  public PermissionCheckAspect(AccessControlService accessControlService, PermissionCodeService permissionCodeService) {
    this.accessControlService = accessControlService;
    this.permissionCodeService = permissionCodeService;
  }

  @Around("execution(* top.elexvx.admin.controller..*(..))")
  public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
    String permissionCode = resolvePermissionCode(joinPoint);
    if (permissionCode != null && !accessControlService.hasPermission(permissionCode)) {
      throw new AccessDeniedException(permissionCode);
    }
    return joinPoint.proceed();
  }

  private String resolvePermissionCode(ProceedingJoinPoint joinPoint) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Class<?> targetClass = joinPoint.getTarget() == null ? signature.getDeclaringType() : joinPoint.getTarget().getClass();
    java.lang.reflect.Method method = AopUtils.getMostSpecificMethod(signature.getMethod(), targetClass);

    RequirePerm requirePerm = AnnotatedElementUtils.findMergedAnnotation(method, RequirePerm.class);
    if (requirePerm == null) {
      requirePerm = AnnotatedElementUtils.findMergedAnnotation(targetClass, RequirePerm.class);
    }
    if (requirePerm != null) {
      return normalize(requirePerm.value());
    }

    PagePerm pagePerm = AnnotatedElementUtils.findMergedAnnotation(method, PagePerm.class);
    if (pagePerm == null) {
      pagePerm = AnnotatedElementUtils.findMergedAnnotation(targetClass, PagePerm.class);
    }
    if (pagePerm == null) return null;
    return permissionCodeService.buildPermissionCode(pagePerm.routeName(), pagePerm.action());
  }

  private String normalize(String text) {
    if (text == null) return null;
    String value = text.trim();
    return value.isEmpty() ? null : value;
  }
}
