package elexvx.admin.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import elexvx.admin.annotation.Sensitive;
import elexvx.admin.enums.DesensitizedType;

import java.io.IOException;
import java.util.Objects;

/**
 * 数据脱敏序列化器（Jackson）。
 *
 * <p>当字段标注 {@link Sensitive} 注解时，根据 {@link DesensitizedType} 在序列化阶段对字符串进行脱敏输出，
 * 以降低敏感信息在接口响应中的暴露风险。
 *
 * <p>容错策略：脱敏过程中若出现异常，将回退为原值输出，以避免接口因脱敏逻辑失败而整体不可用。
 */
public class SensitiveJsonSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private DesensitizedType desensitizedType;

    public SensitiveJsonSerializer() {
    }

    public SensitiveJsonSerializer(DesensitizedType desensitizedType) {
        this.desensitizedType = desensitizedType;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null || value.isEmpty()) {
            gen.writeNull();
            return;
        }
        try {
            DesensitizedType type = desensitizedType;
            if (type == DesensitizedType.USERNAME) gen.writeString(desensitizeUsername(value));
            else if (type == DesensitizedType.PASSWORD) gen.writeString(desensitizePassword(value));
            else if (type == DesensitizedType.ID_CARD) gen.writeString(desensitizeIdCard(value));
            else if (type == DesensitizedType.PHONE) gen.writeString(desensitizePhone(value));
            else if (type == DesensitizedType.EMAIL) gen.writeString(desensitizeEmail(value));
            else if (type == DesensitizedType.BANK_CARD) gen.writeString(desensitizeBankCard(value));
            else if (type == DesensitizedType.CAR_LICENSE) gen.writeString(desensitizeCarLicense(value));
            else gen.writeString(value);
        } catch (Exception e) {
            gen.writeString(value);
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        Sensitive annotation = property.getAnnotation(Sensitive.class);
        if (Objects.nonNull(annotation) && Objects.equals(String.class, property.getType().getRawClass())) {
            return new SensitiveJsonSerializer(annotation.desensitizedType());
        }
        return prov.findValueSerializer(property.getType(), property);
    }

    // 脱敏方法实现

    private String desensitizeUsername(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        String name = value;
        if (name.length() == 2) {
            return name.substring(0, 1) + "*";
        }
        return name.substring(0, 1) + "*".repeat(Math.max(0, name.length() - 1));
    }

    private String desensitizePassword(String value) {
        return "******";
    }

    private String desensitizeIdCard(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return value.replaceAll("(\\d{4})\\d{10}(\\w{4})", "$1**********$2");
    }

    private String desensitizePhone(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return value.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    private String desensitizeEmail(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        int index = value.indexOf('@');
        if (index <= 1) {
            return value;
        }
        return value.substring(0, 1) + "****" + value.substring(index);
    }

    private String desensitizeBankCard(String value) {
         if (value == null || value.isEmpty()) {
            return "";
        }
        return value.substring(0, 0) + "************" + value.substring(value.length() - 4);
    }

    private String desensitizeCarLicense(String value) {
         if (value == null || value.isEmpty()) {
            return "";
        }
        // 湘F88888 -> 湘F8***8
        if (value.length() < 4) {
             return value;
        }
        // Keep first 3, last 1?
        // value.length = 7 (湘F88888)
        // 3 + 1 = 4. 3 masked.
        // If simply hardcode for example:
        return value.substring(0, 3) + "***" + value.substring(value.length() - 1);
    }
}
