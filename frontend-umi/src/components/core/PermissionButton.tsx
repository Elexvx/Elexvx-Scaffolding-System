/**
 * Elexvx Admin - Permission Button
 * 首次编写时间：2026-03-24
 * 说明：核心权限交互组件，供页面级与按钮级权限控制复用
 * Copyright (c) Elexvx. All rights reserved.
 */
import { useAccess } from 'umi';
import { Button } from 'antd';
import type { ButtonProps } from 'antd';

interface PermissionButtonProps extends ButtonProps {
  permission?: string | string[];
}

export function PermissionButton({ permission, ...props }: PermissionButtonProps) {
  const access = useAccess();
  if (permission && !access.hasPermission(permission)) {
    return null;
  }
  return <Button {...props} />;
}
