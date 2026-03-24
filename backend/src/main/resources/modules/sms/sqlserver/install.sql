IF OBJECT_ID('verification_sms_settings', 'U') IS NULL
BEGIN
  CREATE TABLE verification_sms_settings (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    sms_enabled TINYINT,
    sms_provider NVARCHAR(32),
    sms_aliyun_enabled TINYINT,
    sms_aliyun_access_key_id NVARCHAR(256),
    sms_aliyun_access_key_secret NVARCHAR(256),
    sms_aliyun_sign_name NVARCHAR(128),
    sms_aliyun_template_code NVARCHAR(64),
    sms_aliyun_region_id NVARCHAR(64),
    sms_aliyun_endpoint NVARCHAR(255),
    sms_tencent_enabled TINYINT,
    sms_tencent_secret_id NVARCHAR(256),
    sms_tencent_secret_key NVARCHAR(256),
    sms_tencent_sign_name NVARCHAR(128),
    sms_tencent_template_id NVARCHAR(64),
    sms_tencent_region NVARCHAR(64),
    sms_tencent_endpoint NVARCHAR(255),
    sms_sdk_app_id NVARCHAR(64)
  );
END;

IF NOT EXISTS (SELECT 1 FROM verification_sms_settings WHERE id = 1)
BEGIN
  SET IDENTITY_INSERT verification_sms_settings ON;
  INSERT INTO verification_sms_settings (id) VALUES (1);
  SET IDENTITY_INSERT verification_sms_settings OFF;
END;
