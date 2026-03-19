package top.elexvx.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import top.elexvx.admin.annotation.AiFunction;
import top.elexvx.admin.exception.BusinessException;
import top.elexvx.admin.exception.ErrorCodes;
import top.elexvx.admin.util.PermissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * AI 服务核心类
 * 负责扫描 @AiFunction 注解，管理工具，并模拟/处理 AI 调用
 */
@Service
public class AiService {
    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private static BusinessException badRequest(String message) {
        return new BusinessException(ErrorCodes.BAD_REQUEST, message);
    }

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    // 存储注册的 AI 工具
    private final Map<String, AiToolDefinition> toolRegistry = new HashMap<>();

    @PostConstruct
    public void init() {
        // 扫描所有 Bean，寻找 @AiFunction 注解的方法
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            try {
                Object bean = applicationContext.getBean(Objects.requireNonNull(beanName));
                Method[] methods = ReflectionUtils.getAllDeclaredMethods(bean.getClass());
                for (Method method : methods) {
                    if (method.isAnnotationPresent(AiFunction.class)) {
                        registerTool(bean, method);
                    }
                }
            } catch (Exception e) {
                log.debug("Skip bean during AI function scan, beanName={}", beanName, e);
            }
        }
        log.info("AI Service initialized. Registered {} tools.", toolRegistry.size());
    }

    private void registerTool(Object bean, Method method) {
        AiFunction annotation = method.getAnnotation(AiFunction.class);
        String functionName = annotation.name().isEmpty() ? method.getName() : annotation.name();
        String description = annotation.description();

        AiToolDefinition tool = new AiToolDefinition();
        tool.setName(functionName);
        tool.setDescription(description);
        tool.setBean(bean);
        tool.setMethod(method);
        
        // 解析参数
        List<AiToolParameter> parameters = new ArrayList<>();
        for (Parameter param : method.getParameters()) {
            AiToolParameter toolParam = new AiToolParameter();
            toolParam.setName(param.getName());
            toolParam.setType(param.getType().getSimpleName());
            parameters.add(toolParam);
        }
        tool.setParameters(parameters);
        tool.setRequiredPermissions(List.of(annotation.requiredPermissions()));
        tool.setDangerous(annotation.dangerous());

        toolRegistry.put(functionName, tool);
        log.debug("Registered AI tool: {}", functionName);
    }

    public List<AiToolDefinition> getAccessibleTools() {
        return toolRegistry
            .values()
            .stream()
            .filter(tool -> !tool.isDangerous())
            .filter(this::hasRequiredPermission)
            .toList();
    }

    /**
     * 获取所有可用工具的描述（JSON Schema 格式简化版）
     */
    public List<Map<String, Object>> getToolsSchema() {
        List<Map<String, Object>> schemas = new ArrayList<>();
        for (AiToolDefinition tool : getAccessibleTools()) {
            Map<String, Object> schema = new HashMap<>();
            schema.put("name", tool.getName());
            schema.put("description", tool.getDescription());
            
            Map<String, Object> params = new HashMap<>();
            for (AiToolParameter param : tool.getParameters()) {
                params.put(param.getName(), Map.of("type", "string", "description", "Type: " + param.getType()));
            }
            schema.put("parameters", params);
            schemas.add(schema);
        }
        return schemas;
    }

    /**
     * 执行 AI 工具调用
     */
    public Object executeTool(String toolName, Map<String, Object> args) throws Exception {
        AiToolDefinition tool = toolRegistry.get(toolName);
        if (tool == null) {
            throw badRequest("Tool not found: " + toolName);
        }
        ensureCallable(tool);

        Method method = tool.getMethod();
        Object[] invokeArgs = new Object[method.getParameterCount()];
        Parameter[] parameters = method.getParameters();

        for (int i = 0; i < parameters.length; i++) {
            String paramName = parameters[i].getName();
            Object argValue = args.get(paramName);
            // 简单的类型转换逻辑，实际项目中需要更健壮的转换
            if (argValue != null) {
                if (parameters[i].getType() == Long.class && argValue instanceof Integer) {
                    invokeArgs[i] = ((Integer) argValue).longValue();
                } else {
                    invokeArgs[i] = objectMapper.convertValue(argValue, parameters[i].getType());
                }
            }
        }

        return method.invoke(tool.getBean(), invokeArgs);
    }

    private boolean hasRequiredPermission(AiToolDefinition tool) {
        List<String> perms = tool.getRequiredPermissions();
        if (perms == null || perms.isEmpty()) {
            return true;
        }
        try {
          PermissionUtil.checkAny(perms.toArray(new String[0]));
          return true;
        } catch (Exception e) {
          return false;
        }
    }

    private void ensureCallable(AiToolDefinition tool) {
        if (tool.isDangerous()) {
            throw new IllegalStateException("AI安全策略阻止执行危险操作: " + tool.getName());
        }
        if (!hasRequiredPermission(tool)) {
            throw new IllegalStateException("当前账号没有权限执行: " + tool.getName());
        }
    }

    // 内部类定义
    public static class AiToolDefinition {
        private String name;
        private String description;
        private Object bean;
        private Method method;
        private List<AiToolParameter> parameters;
        private List<String> requiredPermissions;
        private boolean dangerous;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Object getBean() {
            return bean;
        }

        public void setBean(Object bean) {
            this.bean = bean;
        }

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        public List<AiToolParameter> getParameters() {
            return parameters;
        }

        public void setParameters(List<AiToolParameter> parameters) {
            this.parameters = parameters;
        }

        public List<String> getRequiredPermissions() {
            return requiredPermissions;
        }

        public void setRequiredPermissions(List<String> requiredPermissions) {
            this.requiredPermissions = requiredPermissions;
        }

        public boolean isDangerous() {
            return dangerous;
        }

        public void setDangerous(boolean dangerous) {
            this.dangerous = dangerous;
        }
    }

    public static class AiToolParameter {
        private String name;
        private String type;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
