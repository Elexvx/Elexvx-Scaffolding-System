CREATE SCHEMA IF NOT EXISTS plug_warehouse;

CREATE TABLE IF NOT EXISTS plug_warehouse.warehouse_inbound (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  inbound_no VARCHAR(64) NOT NULL,
  supplier_name VARCHAR(128) NOT NULL,
  total_amount DECIMAL(18,2) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_inbound_no (inbound_no)
);
