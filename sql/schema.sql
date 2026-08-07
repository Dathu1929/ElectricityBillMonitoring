CREATE DATABASE IF NOT EXISTS electricity_bill_monitoring;
USE electricity_bill_monitoring;

CREATE TABLE users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  full_name VARCHAR(150) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role ENUM('consumer','admin','staff') NOT NULL DEFAULT 'consumer',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL DEFAULT NULL
) ENGINE=InnoDB;

CREATE TABLE electricity_boards (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  code VARCHAR(50) NOT NULL UNIQUE,
  country VARCHAR(100) NOT NULL,
  connector_class VARCHAR(100) NOT NULL,
  api_config JSON NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE consumer_connections (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  board_id INT NOT NULL,
  service_number VARCHAR(50) NOT NULL,
  consumer_number VARCHAR(50) NOT NULL,
  address VARCHAR(255) NOT NULL,
  meter_type VARCHAR(50) NOT NULL,
  connection_status ENUM('active','inactive') DEFAULT 'active',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (board_id) REFERENCES electricity_boards(id),
  INDEX idx_service_number (service_number),
  INDEX idx_consumer_number (consumer_number)
) ENGINE=InnoDB;

CREATE TABLE bills (
  id INT AUTO_INCREMENT PRIMARY KEY,
  consumer_number VARCHAR(50) NOT NULL,
  bill_month VARCHAR(20) NOT NULL,
  units_used INT NOT NULL,
  rate_per_unit DECIMAL(8,2) NOT NULL,
  amount_due DECIMAL(10,2) NOT NULL,
  amount_paid DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  due_date DATE NOT NULL,
  status ENUM('pending','paid','overdue') NOT NULL DEFAULT 'pending',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_consumer_bill (consumer_number, bill_month)
) ENGINE=InnoDB;

CREATE TABLE bill_details (
  id INT AUTO_INCREMENT PRIMARY KEY,
  bill_id INT NOT NULL,
  description VARCHAR(150) NOT NULL,
  quantity INT NOT NULL,
  unit_rate DECIMAL(8,2) NOT NULL,
  total DECIMAL(10,2) NOT NULL,
  FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE payments (
  id INT AUTO_INCREMENT PRIMARY KEY,
  bill_id INT NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  method VARCHAR(50) NOT NULL,
  gateway_reference VARCHAR(100) NULL,
  status ENUM('pending','captured','failed','refunded') NOT NULL DEFAULT 'pending',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE notifications (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  title VARCHAR(150) NOT NULL,
  message TEXT NOT NULL,
  is_read TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE usage_history (
  id INT AUTO_INCREMENT PRIMARY KEY,
  consumer_number VARCHAR(50) NOT NULL,
  usage_kwh INT NOT NULL,
  recorded_at DATE NOT NULL,
  INDEX idx_usage_consumer (consumer_number, recorded_at)
) ENGINE=InnoDB;

CREATE TABLE settings (
  id INT AUTO_INCREMENT PRIMARY KEY,
  app_name VARCHAR(100) NOT NULL,
  currency VARCHAR(10) NOT NULL DEFAULT 'INR',
  language VARCHAR(20) NOT NULL DEFAULT 'en',
  notifications_enabled TINYINT(1) NOT NULL DEFAULT 1,
  dark_mode TINYINT(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB;

DELIMITER $$
CREATE PROCEDURE get_bill_summary()
BEGIN
  SELECT COUNT(*) AS total_bills, SUM(amount_due) AS total_due, SUM(amount_paid) AS total_paid FROM bills;
END$$
DELIMITER ;
