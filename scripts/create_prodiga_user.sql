CREATE USER 'prodiga'@'localhost' IDENTIFIED BY 'SuperGeheimesPassword123NacktNiemand!!';
GRANT ALL PRIVILEGES ON * . * TO 'prodiga'@'localhost';
FLUSH PRIVILEGES;
CREATE DATABASE IF NOT EXISTS prodiga;