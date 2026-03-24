IF OBJECT_ID('verification_email_settings', 'U') IS NULL
BEGIN
  CREATE TABLE verification_email_settings (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    email_enabled TINYINT,
    email_host NVARCHAR(255),
    email_port INT,
    email_username NVARCHAR(128),
    email_password NVARCHAR(256),
    email_from NVARCHAR(128),
    email_ssl TINYINT
  );
END;

IF NOT EXISTS (SELECT 1 FROM verification_email_settings WHERE id = 1)
BEGIN
  SET IDENTITY_INSERT verification_email_settings ON;
  INSERT INTO verification_email_settings (id) VALUES (1);
  SET IDENTITY_INSERT verification_email_settings OFF;
END;
