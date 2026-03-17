package com.tencent.tdesign.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 权限点编码工具。
 *
 * <p>统一处理 actions 解析与 permission code 生成，避免各处重复拼接逻辑。
 */
@Service
public class PermissionCodeService {
  private static final Logger log = LoggerFactory.getLogger(PermissionCodeService.class);
  private final ObjectMapper objectMapper;

  public PermissionCodeService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public String buildPermissionCode(String routeName, String action) {
    String normalizedRouteName = normalize(routeName);
    String normalizedAction = normalize(action);
    if (normalizedRouteName == null || normalizedAction == null) return null;
    return "system:" + normalizedRouteName + ":" + normalizedAction;
  }

  /**
   * 解析菜单 actions 字段，兼容：
   * - 逗号分隔字符串：query,create,update
   * - JSON 数组字符串：["query","create"]
   */
  public List<String> parseActions(String rawActions) {
    if (rawActions == null || rawActions.isBlank()) return List.of();
    String text = rawActions.trim();
    if (text.isEmpty()) return List.of();

    if (text.startsWith("[") && text.endsWith("]")) {
      try {
        List<String> values = objectMapper.readValue(text, new TypeReference<List<String>>() {});
        return normalizeActions(values);
      } catch (Exception parseException) {
        log.debug("解析 actions JSON 失败，回退为逗号分隔，raw={}", text, parseException);
      }
    }

    String[] parts = text.split(",");
    List<String> values = new ArrayList<>();
    for (String part : parts) {
      values.add(part);
    }
    return normalizeActions(values);
  }

  public List<String> normalizeActions(List<String> actions) {
    if (actions == null || actions.isEmpty()) return List.of();
    Set<String> normalized = new LinkedHashSet<>();
    for (String action : actions) {
      String value = normalize(action);
      if (value != null) {
        normalized.add(value);
      }
    }
    return List.copyOf(normalized);
  }

  private String normalize(String value) {
    if (value == null) return null;
    String text = value.trim();
    return text.isEmpty() ? null : text;
  }
}
