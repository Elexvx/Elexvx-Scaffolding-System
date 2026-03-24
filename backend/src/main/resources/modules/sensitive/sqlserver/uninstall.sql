IF OBJECT_ID('sensitive_page_settings', 'U') IS NOT NULL
BEGIN
  DROP TABLE sensitive_page_settings;
END;

IF OBJECT_ID('sensitive_words', 'U') IS NOT NULL
BEGIN
  DROP TABLE sensitive_words;
END;

IF OBJECT_ID('sensitive_settings', 'U') IS NOT NULL
BEGIN
  DROP TABLE sensitive_settings;
END;
