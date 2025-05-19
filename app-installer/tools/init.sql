CREATE DATABASE IF NOT EXISTS buildtask_db;
CREATE USER IF NOT EXISTS 'buildtask_user'@'localhost' IDENTIFIED BY 'buildtask_password';
GRANT ALL PRIVILEGES ON buildtask_db.* TO 'buildtask_user'@'localhost';
FLUSH PRIVILEGES;