INSERT INTO user (enabled, first_name, last_name, password, username, create_user_username, create_date) VALUES(TRUE, 'Admin', 'Istrator', 'passwd', 'admin', 'admin', '2016-01-01 00:00:00');
INSERT INTO user_user_role (user_username, ROLES) VALUES ('admin', 'ADMIN');
INSERT INTO user_user_role (user_username, ROLES) VALUES ('admin', 'EMPLOYEE');
INSERT INTO user (enabled, first_name, last_name, password, username, create_user_username, create_date) VALUES(TRUE, 'Susi', 'Kaufgern', 'passwd', 'user1', 'admin', '2016-01-01 00:00:00');
INSERT INTO user_user_role (user_username, ROLES) VALUES ('user1', 'TEAMLEADER');
INSERT INTO user_user_role (user_username, ROLES) VALUES ('user1', 'EMPLOYEE');
INSERT INTO user (enabled, first_name, last_name, password, username, create_user_username, create_date) VALUES(TRUE, 'Max', 'Mustermann', 'passwd', 'user2', 'admin', '2016-01-01 00:00:00');
INSERT INTO user_user_role (user_username, ROLES) VALUES ('user2', 'EMPLOYEE');
