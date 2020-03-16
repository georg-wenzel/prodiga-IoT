INSERT INTO user (enabled, first_name, last_name, password, username, create_user_username, create_date) VALUES(TRUE, 'Admin', 'Istrator', 'passwd', 'admin', 'admin', '2016-01-01 00:00:00');
INSERT INTO user_user_role (user_username, ROLES) VALUES ('admin', 'ADMIN');
INSERT INTO user_user_role (user_username, ROLES) VALUES ('admin', 'EMPLOYEE');
INSERT INTO user (enabled, first_name, last_name, password, username, create_user_username, create_date) VALUES(TRUE, 'Susi', 'Kaufgern', 'passwd', 'user1', 'admin', '2016-01-01 00:00:00');
INSERT INTO user_user_role (user_username, ROLES) VALUES ('user1', 'TEAMLEADER');
INSERT INTO user_user_role (user_username, ROLES) VALUES ('user1', 'EMPLOYEE');
INSERT INTO user (enabled, first_name, last_name, password, username, create_user_username, create_date) VALUES(TRUE, 'Max', 'Mustermann', 'passwd', 'user2', 'admin', '2016-01-01 00:00:00');
INSERT INTO user_user_role (user_username, ROLES) VALUES ('user2', 'EMPLOYEE');


INSERT INTO user (enabled, first_name, last_name, password, username, create_user_username, create_date) VALUES(TRUE, 'Name', 'Nachname', 'passwd', 'dept_leader_1', 'admin', '2016-01-01 00:00:00');
INSERT INTO user_user_role (user_username, ROLES) VALUES ('dept_leader_1', 'DEPARTMENTLEADER');
INSERT INTO user (enabled, first_name, last_name, password, username, create_user_username, create_date) VALUES(TRUE, 'Name', 'Nachname', 'passwd', 'dept_leader_2', 'admin', '2016-01-01 00:00:00');
INSERT INTO user_user_role (user_username, ROLES) VALUES ('dept_leader_2', 'DEPARTMENTLEADER');

INSERT INTO department (id, name, object_created_date_time, department_leader_username, object_created_user_username)
    VALUES (1, 'sales', '2017-01-01', 'dept_leader_1', 'admin');

    INSERT INTO department (id, name, object_created_date_time, department_leader_username, object_created_user_username)
    VALUES (2, 'marketing', '2017-01-01', 'dept_leader_2', 'admin');