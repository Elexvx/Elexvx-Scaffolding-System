IF OBJECT_ID('verification_email_settings', 'U') IS NOT NULL
BEGIN
  DROP TABLE verification_email_settings;
END;
