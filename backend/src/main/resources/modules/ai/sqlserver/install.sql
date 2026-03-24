IF OBJECT_ID('ai_provider_settings', 'U') IS NULL
BEGIN
  CREATE TABLE ai_provider_settings (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(64) NOT NULL,
    vendor NVARCHAR(32) NOT NULL,
    base_url NVARCHAR(255) NOT NULL,
    endpoint_path NVARCHAR(255),
    model NVARCHAR(128),
    api_key NVARCHAR(512),
    api_version NVARCHAR(64),
    temperature FLOAT,
    max_tokens INT,
    is_default TINYINT,
    enabled TINYINT,
    extra_headers NVARCHAR(2000),
    remark NVARCHAR(512),
    last_test_status NVARCHAR(32),
    last_test_message NVARCHAR(512),
    last_tested_at DATETIME2,
    created_at DATETIME2,
    updated_at DATETIME2
  );
END;
