INSERT INTO user (enabled, first_name, last_name, password, username, create_user_username, create_date) VALUES(TRUE, 'Admin', 'Istrator', '$2a$10$d8cQ7Euz2hM43HOHWolUGeCEZSS/ltJVJYiJAmczl1X5FKzCjg6PC', 'admin', 'admin', '2016-01-01 00:00:00');
INSERT INTO user_user_role (user_username, ROLES) VALUES ('admin', 'ADMIN');
INSERT INTO user_user_role (user_username, ROLES) VALUES ('admin', 'EMPLOYEE');
INSERT INTO user (enabled, first_name, last_name, password, username, create_user_username, create_date) VALUES(TRUE, 'Susi', 'Kaufgern', '$2a$10$d8cQ7Euz2hM43HOHWolUGeCEZSS/ltJVJYiJAmczl1X5FKzCjg6PC', 'user1', 'admin', '2016-01-01 00:00:00');
INSERT INTO user_user_role (user_username, ROLES) VALUES ('user1', 'TEAMLEADER');
INSERT INTO user_user_role (user_username, ROLES) VALUES ('user1', 'EMPLOYEE');
INSERT INTO user (enabled, first_name, last_name, password, username, create_user_username, create_date) VALUES(TRUE, 'Max', 'Mustermann', '$2a$10$d8cQ7Euz2hM43HOHWolUGeCEZSS/ltJVJYiJAmczl1X5FKzCjg6PC', 'user2', 'admin', '2016-01-01 00:00:00');
INSERT INTO user_user_role (user_username, ROLES) VALUES ('user2', 'EMPLOYEE');

INSERT INTO department (ID, NAME, object_created_date_time, object_created_user_username) VALUES (1, 'IT services',CURRENT_TIMESTAMP, 'admin');
INSERT INTO department (ID, NAME, object_created_date_time, object_created_user_username) VALUES (2, 'Research and development',CURRENT_TIMESTAMP, 'admin');

INSERT INTO user (enabled, first_name, last_name, password, username, create_user_username, create_date, assigned_department_id) VALUES(TRUE, 'John', 'Doe', '$2a$10$d8cQ7Euz2hM43HOHWolUGeCEZSS/ltJVJYiJAmczl1X5FKzCjg6PC', 'ITS leader', 'admin', '2016-01-01 00:00:00', 1);
INSERT INTO user_user_role (user_username, ROLES) VALUES ('ITS leader', 'DEPARTMENTLEADER');
INSERT INTO user (enabled, first_name, last_name, password, username, create_user_username, create_date, assigned_department_id) VALUES(TRUE, 'Sandra', 'Sun', '$2a$10$d8cQ7Euz2hM43HOHWolUGeCEZSS/ltJVJYiJAmczl1X5FKzCjg6PC', 'RD leader', 'admin', '2016-01-01 00:00:00', 2);
INSERT INTO user_user_role (user_username, ROLES) VALUES ('RD leader', 'DEPARTMENTLEADER');

INSERT IGNORE INTO raspberry_pi (id, internal_id, object_changed_date_time, object_created_date_time, password, object_changed_user_username, object_created_user_username) VALUES (1, 'test', NOW(), NOW(), '$2a$10$d8cQ7Euz2hM43HOHWolUGeCEZSS/ltJVJYiJAmczl1X5FKzCjg6PC', 'admin', 'admin');
