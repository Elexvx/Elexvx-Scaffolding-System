IF OBJECT_ID('sensitive_words', 'U') IS NULL
BEGIN
  CREATE TABLE sensitive_words (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    word NVARCHAR(200) NOT NULL UNIQUE,
    enabled TINYINT NOT NULL,
    created_at DATETIME2,
    updated_at DATETIME2
  );
END;

IF OBJECT_ID('sensitive_page_settings', 'U') IS NULL
BEGIN
  CREATE TABLE sensitive_page_settings (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    page_key NVARCHAR(255) NOT NULL UNIQUE,
    page_name NVARCHAR(255),
    enabled TINYINT NOT NULL,
    created_at DATETIME2,
    updated_at DATETIME2
  );
END;

IF OBJECT_ID('sensitive_settings', 'U') IS NULL
BEGIN
  CREATE TABLE sensitive_settings (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    enabled TINYINT NOT NULL,
    updated_at DATETIME2
  );
END;

IF NOT EXISTS (SELECT 1 FROM sensitive_settings WHERE id = 1)
BEGIN
  SET IDENTITY_INSERT sensitive_settings ON;
  INSERT INTO sensitive_settings (id, enabled) VALUES (1, 0);
  SET IDENTITY_INSERT sensitive_settings OFF;
END;
