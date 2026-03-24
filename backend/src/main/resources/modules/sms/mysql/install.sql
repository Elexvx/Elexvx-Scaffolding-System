CREATE TABLE IF NOT EXISTS verification_sms_settings (
  id BIGINT NOT NULL AUTO_INCREMENT,
  sms_enabled TINYINT NULL DEFAULT NULL,
  sms_provider VARCHAR(32) NULL DEFAULT NULL,
  sms_aliyun_enabled TINYINT NULL DEFAULT NULL,
  sms_aliyun_access_key_id VARCHAR(256) NULL DEFAULT NULL,
  sms_aliyun_access_key_secret VARCHAR(256) NULL DEFAULT NULL,
  sms_aliyun_sign_name VARCHAR(128) NULL DEFAULT NULL,
  sms_aliyun_template_code VARCHAR(64) NULL DEFAULT NULL,
  sms_aliyun_region_id VARCHAR(64) NULL DEFAULT NULL,
  sms_aliyun_endpoint VARCHAR(255) NULL DEFAULT NULL,
  sms_tencent_enabled TINYINT NULL DEFAULT NULL,
  sms_tencent_secret_id VARCHAR(256) NULL DEFAULT NULL,
  sms_tencent_secret_key VARCHAR(256) NULL DEFAULT NULL,
  sms_tencent_sign_name VARCHAR(128) NULL DEFAULT NULL,
  sms_tencent_template_id VARCHAR(64) NULL DEFAULT NULL,
  sms_tencent_region VARCHAR(64) NULL DEFAULT NULL,
  sms_tencent_endpoint VARCHAR(255) NULL DEFAULT NULL,
  sms_sdk_app_id VARCHAR(64) NULL DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO verification_sms_settings (id) VALUES (1)
  ON DUPLICATE KEY UPDATE id = id;
