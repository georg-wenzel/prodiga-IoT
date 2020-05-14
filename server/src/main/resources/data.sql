INSERT INTO user (enabled, first_name, last_name, password, username, create_user_username, create_date) VALUES(TRUE, 'Admin', 'Istrator', '$2a$10$d8cQ7Euz2hM43HOHWolUGeCEZSS/ltJVJYiJAmczl1X5FKzCjg6PC', 'admin', 'admin', '2016-01-01 00:00:00');
INSERT INTO user_user_role (user_username, ROLES) VALUES ('admin', 'ADMIN');
INSERT INTO user_user_role (user_username, ROLES) VALUES ('admin', 'EMPLOYEE');
INSERT INTO user_user_role (user_username, ROLES) VALUES ('admin', 'TEAMLEADER');

INSERT INTO booking_category (id, name, object_created_user_username, object_created_date_time, object_changed_user_username, object_changed_date_time) VALUES (1, 'Pause / Vacation', 'admin', NOW(), 'admin', NOW());
INSERT INTO booking_category (id, name, object_created_user_username, object_created_date_time, object_changed_user_username, object_changed_date_time) VALUES (2, 'Conceptualizing', 'admin', NOW(), 'admin', NOW());
INSERT INTO booking_category (id, name, object_created_user_username, object_created_date_time, object_changed_user_username, object_changed_date_time) VALUES (3, 'Design', 'admin', NOW(), 'admin', NOW());
INSERT INTO booking_category (id, name, object_created_user_username, object_created_date_time, object_changed_user_username, object_changed_date_time) VALUES (4, 'Implementation', 'admin', NOW(), 'admin', NOW());
INSERT INTO booking_category (id, name, object_created_user_username, object_created_date_time, object_changed_user_username, object_changed_date_time) VALUES (5, 'Testing', 'admin', NOW(), 'admin', NOW());
INSERT INTO booking_category (id, name, object_created_user_username, object_created_date_time, object_changed_user_username, object_changed_date_time) VALUES (6, 'Documentation', 'admin', NOW(), 'admin', NOW());
INSERT INTO booking_category (id, name, object_created_user_username, object_created_date_time, object_changed_user_username, object_changed_date_time) VALUES (7, 'Debugging', 'admin', NOW(), 'admin', NOW());
INSERT INTO booking_category (id, name, object_created_user_username, object_created_date_time, object_changed_user_username, object_changed_date_time) VALUES (8, 'Meeting', 'admin', NOW(), 'admin', NOW());
INSERT INTO booking_category (id, name, object_created_user_username, object_created_date_time, object_changed_user_username, object_changed_date_time) VALUES (9, 'Customer Support', 'admin', NOW(), 'admin', NOW());
INSERT INTO booking_category (id, name, object_created_user_username, object_created_date_time, object_changed_user_username, object_changed_date_time) VALUES (10, 'Education and Training', 'admin', NOW(), 'admin', NOW());
INSERT INTO booking_category (id, name, object_created_user_username, object_created_date_time, object_changed_user_username, object_changed_date_time) VALUES (11, 'Project Management', 'admin', NOW(), 'admin', NOW());
INSERT INTO booking_category (id, name, object_created_user_username, object_created_date_time, object_changed_user_username, object_changed_date_time) VALUES (12, 'Other', 'admin', NOW(), 'admin', NOW());

INSERT INTO department (ID, NAME, object_created_date_time, object_created_user_username) VALUES (1, 'IT services',CURRENT_TIMESTAMP, 'admin');
INSERT INTO department (ID, NAME, object_created_date_time, object_created_user_username) VALUES (2, 'Development',CURRENT_TIMESTAMP, 'admin');
INSERT INTO department (ID, NAME, object_created_date_time, object_created_user_username) VALUES (3, 'Testing',CURRENT_TIMESTAMP, 'admin');

INSERT INTO team (ID, NAME, department_id, object_created_date_time, object_created_user_username) VALUES (1, 'Team 1', 1, CURRENT_TIMESTAMP, 'admin');
INSERT INTO team (ID, NAME, department_id, object_created_date_time, object_created_user_username) VALUES (2, 'Team 2', 1, CURRENT_TIMESTAMP, 'admin');

INSERT INTO booking_category_teams VALUES (2,1);
INSERT INTO booking_category_teams VALUES (3,1);
INSERT INTO booking_category_teams VALUES (4,1);
INSERT INTO booking_category_teams VALUES (6,1);
INSERT INTO booking_category_teams VALUES (7,1);
INSERT INTO booking_category_teams VALUES (9,1);
INSERT INTO booking_category_teams VALUES (4,2);
INSERT INTO booking_category_teams VALUES (7,2);
INSERT INTO booking_category_teams VALUES (8,2);
INSERT INTO booking_category_teams VALUES (10,2);
INSERT INTO booking_category_teams VALUES (11,2);

INSERT INTO user (enabled, first_name, last_name, password, username, create_user_username, create_date, assigned_department_id, assigned_team_id) VALUES(TRUE, 'Susi', 'Kaufgern', '$2a$10$d8cQ7Euz2hM43HOHWolUGeCEZSS/ltJVJYiJAmczl1X5FKzCjg6PC', 'user1', 'admin', '2016-01-01 00:00:00',1,1);
INSERT INTO user_user_role (user_username, ROLES) VALUES ('user1', 'TEAMLEADER');
INSERT INTO user_user_role (user_username, ROLES) VALUES ('user1', 'DEPARTMENTLEADER');
INSERT INTO user_user_role (user_username, ROLES) VALUES ('user1', 'EMPLOYEE');
INSERT INTO user (enabled, first_name, last_name, password, username, create_user_username, create_date, assigned_department_id, assigned_team_id, may_edit_historic_data) VALUES(TRUE, 'Max', 'Mustermann', '$2a$10$d8cQ7Euz2hM43HOHWolUGeCEZSS/ltJVJYiJAmczl1X5FKzCjg6PC', 'user2', 'admin', '2016-01-01 00:00:00',1,1, 1);
INSERT INTO user_user_role (user_username, ROLES) VALUES ('user2', 'EMPLOYEE');
INSERT INTO user (enabled, first_name, last_name, password, username, create_user_username, create_date, assigned_department_id, assigned_team_id) VALUES(TRUE, 'Frank', 'Merkwürdig', '$2a$10$d8cQ7Euz2hM43HOHWolUGeCEZSS/ltJVJYiJAmczl1X5FKzCjg6PC', 'user3', 'admin', '2016-01-01 00:00:00',1,1);
INSERT INTO user_user_role (user_username, ROLES) VALUES ('user3', 'EMPLOYEE');
INSERT INTO user (enabled, first_name, last_name, password, username, create_user_username, create_date, assigned_department_id) VALUES(TRUE, 'John', 'Doe', '$2a$10$d8cQ7Euz2hM43HOHWolUGeCEZSS/ltJVJYiJAmczl1X5FKzCjg6PC', 'ITS leader', 'admin', '2016-01-01 00:00:00', 1);
INSERT INTO user (enabled, first_name, last_name, password, username, create_user_username, create_date, assigned_department_id) VALUES(TRUE, 'Sandra', 'Sun', '$2a$10$d8cQ7Euz2hM43HOHWolUGeCEZSS/ltJVJYiJAmczl1X5FKzCjg6PC', 'RD leader', 'admin', '2016-01-01 00:00:00', 2);
INSERT INTO user_user_role (user_username, ROLES) VALUES ('RD leader', 'DEPARTMENTLEADER');
INSERT INTO user (enabled, first_name, last_name, password, username, create_user_username, create_date) VALUES(TRUE, 'Spongebob', 'Schwammkopf', '$2a$10$d8cQ7Euz2hM43HOHWolUGeCEZSS/ltJVJYiJAmczl1X5FKzCjg6PC', 'Schwamm', 'admin', '2016-01-01 00:00:00');
INSERT INTO user_user_role (user_username, ROLES) VALUES ('Schwamm', 'EMPLOYEE');
INSERT INTO user (enabled, first_name, last_name, password, username, create_user_username, create_date, assigned_team_id) VALUES(TRUE, 'Patrick', 'Star', '$2a$10$d8cQ7Euz2hM43HOHWolUGeCEZSS/ltJVJYiJAmczl1X5FKzCjg6PC', 'Team leader Stern', 'admin', '2016-01-01 00:00:00', 2);
INSERT INTO user_user_role (user_username, ROLES) VALUES ('Team leader Stern', 'TEAMLEADER');
INSERT INTO user (enabled, first_name, last_name, password, username, create_user_username, create_date, assigned_team_id, assigned_department_id) VALUES(TRUE, 'Corey', 'Taylor', '$2a$10$d8cQ7Euz2hM43HOHWolUGeCEZSS/ltJVJYiJAmczl1X5FKzCjg6PC', 'slipknotfan', 'admin', '2016-01-01 00:00:00', 2, 2);
INSERT INTO user_user_role (user_username, ROLES) VALUES ('slipknotfan', 'EMPLOYEE');
INSERT INTO user (enabled, first_name, last_name, password, username, create_user_username, create_date, assigned_team_id, assigned_department_id) VALUES(TRUE, 'Matthew', 'Heafy', '$2a$10$d8cQ7Euz2hM43HOHWolUGeCEZSS/ltJVJYiJAmczl1X5FKzCjg6PC', 'triviumfan', 'admin', '2016-01-01 00:00:00', 2, 2);
INSERT INTO user_user_role (user_username, ROLES) VALUES ('triviumfan', 'EMPLOYEE');

INSERT INTO room (ID, NAME, object_created_date_time, object_changed_date_time, object_created_user_username, object_changed_user_username) VALUE (1, 'Test Room 1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'admin', 'admin');
INSERT INTO room (ID, NAME, object_created_date_time, object_changed_date_time, object_created_user_username, object_changed_user_username) VALUE (2, 'Test Room 2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'admin', 'admin');
INSERT INTO room (ID, NAME, object_created_date_time, object_changed_date_time, object_created_user_username, object_changed_user_username) VALUE (3, 'Test Room 3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'admin', 'admin');
INSERT INTO room (ID, NAME, object_created_date_time, object_changed_date_time, object_created_user_username, object_changed_user_username) VALUE (7, 'Test Room 7', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'admin', 'admin');

INSERT INTO raspberry_pi (id, internal_id, object_changed_date_time, object_created_date_time, password, object_changed_user_username, object_created_user_username, assigned_room_id) VALUES (1, 'test 1', NOW(), NOW(), '$2a$10$d8cQ7Euz2hM43HOHWolUGeCEZSS/ltJVJYiJAmczl1X5FKzCjg6PC', 'admin', 'admin', 2);
INSERT INTO raspberry_pi (id, internal_id, object_changed_date_time, object_created_date_time, password, object_changed_user_username, object_created_user_username, assigned_room_id) VALUES (2, 'test 2', NOW(), NOW(), '$2a$10$d8cQ7Euz2hM43HOHWolUGeCEZSS/ltJVJYiJAmczl1X5FKzCjg6PC', 'admin', 'admin', 3);
INSERT INTO raspberry_pi (id, internal_id, object_changed_date_time, object_created_date_time, password, object_changed_user_username, object_created_user_username, assigned_room_id) VALUES (3, 'test 3', NOW(), NOW(), '$2a$10$d8cQ7Euz2hM43HOHWolUGeCEZSS/ltJVJYiJAmczl1X5FKzCjg6PC', 'admin', 'admin', 7);

INSERT INTO dice(ID, internal_id, object_created_date_time, object_changed_date_time, object_created_user_username, object_changed_user_username, is_active, user_username, assigned_raspberry_id) VALUES (1, 'test1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'admin', 'admin', FALSE, 'admin', 1);
INSERT INTO dice(ID, internal_id, object_created_date_time, object_changed_date_time, object_created_user_username, object_changed_user_username, is_active, user_username, assigned_raspberry_id) VALUES (2, 'user2dice', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'admin', 'admin', TRUE, 'user2', 1);
INSERT INTO dice(ID, internal_id, object_created_date_time, object_changed_date_time, object_created_user_username, object_changed_user_username, is_active, user_username, assigned_raspberry_id) VALUES (3, 'user1dice', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'admin', 'admin', TRUE, 'user1', 1);
INSERT INTO dice(ID, internal_id, object_created_date_time, object_changed_date_time, object_created_user_username, object_changed_user_username, is_active, user_username, assigned_raspberry_id) VALUES (4, 'user3dice', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'admin', 'admin', TRUE, 'user3', 1);

INSERT INTO dice_side(ID, object_created_date_time, object_changed_date_time, side, side_friendly_name, booking_category_id, dice_id, object_changed_user_username, object_created_user_username) VALUES (1,NOW(),NOW(),5,1,9,2,'user2','user2');
INSERT INTO dice_side(ID, object_created_date_time, object_changed_date_time, side, side_friendly_name, booking_category_id, dice_id, object_changed_user_username, object_created_user_username) VALUES (2,NOW(),NOW(),6,2,2,2,'user2','user2');
INSERT INTO dice_side(ID, object_created_date_time, object_changed_date_time, side, side_friendly_name, booking_category_id, dice_id, object_changed_user_username, object_created_user_username) VALUES (3,NOW(),NOW(),7,3,3,2,'user2','user2');
INSERT INTO dice_side(ID, object_created_date_time, object_changed_date_time, side, side_friendly_name, booking_category_id, dice_id, object_changed_user_username, object_created_user_username) VALUES (4,NOW(),NOW(),8,4,4,2,'user2','user2');
INSERT INTO dice_side(ID, object_created_date_time, object_changed_date_time, side, side_friendly_name, booking_category_id, dice_id, object_changed_user_username, object_created_user_username) VALUES (6,NOW(),NOW(),9,1,9,1,'admin','admin');
INSERT INTO dice_side(ID, object_created_date_time, object_changed_date_time, side, side_friendly_name, booking_category_id, dice_id, object_changed_user_username, object_created_user_username) VALUES (7,NOW(),NOW(),10,2,2,1,'admin','admin');
INSERT INTO dice_side(ID, object_created_date_time, object_changed_date_time, side, side_friendly_name, booking_category_id, dice_id, object_changed_user_username, object_created_user_username) VALUES (8,NOW(),NOW(),11,3,3,1,'admin','admin');
INSERT INTO dice_side(ID, object_created_date_time, object_changed_date_time, side, side_friendly_name, booking_category_id, dice_id, object_changed_user_username, object_created_user_username) VALUES (9,NOW(),NOW(),12,4,4,1,'admin','admin');

INSERT INTO vacation (ID, user_username, begin_date, end_date, object_created_user_username, object_created_date_time) VALUES (1, 'admin', DATE(NOW() - INTERVAL 2 YEAR - INTERVAL 3 DAY), DATE(NOW() - INTERVAL 2 YEAR), 'admin', NOW());
INSERT INTO vacation (ID, user_username, begin_date, end_date, object_created_user_username, object_created_date_time) VALUES (2, 'admin', DATE(NOW() - INTERVAL 35 DAY), DATE(NOW() - INTERVAL 33 DAY), 'admin', NOW());
INSERT INTO vacation (ID, user_username, begin_date, end_date, object_created_user_username, object_created_date_time) VALUES (3, 'admin', DATE(NOW() + INTERVAL 1 YEAR - INTERVAL 3 DAY), DATE(NOW() + INTERVAL 1 YEAR), 'admin', NOW());
INSERT INTO vacation (ID, user_username, begin_date, end_date, object_created_user_username, object_created_date_time) VALUES (4, 'admin', DATE(NOW() - INTERVAL 3 DAY), DATE(NOW() + INTERVAL 2 DAY), 'admin', NOW());
INSERT INTO vacation (ID, user_username, begin_date, end_date, object_created_user_username, object_created_date_time) VALUES (5, 'admin', DATE(NOW() + INTERVAL 10 DAY), DATE(NOW() + INTERVAL 12 DAY), 'admin', NOW());
INSERT INTO vacation (ID, user_username, begin_date, end_date, object_created_user_username, object_created_date_time) VALUES (6, 'admin', DATE(NOW() + INTERVAL 32 DAY), DATE(NOW() + INTERVAL 39 DAY), 'admin', NOW());

INSERT INTO booking (id, activity_end_date, activity_start_date, object_created_date_time, object_created_user_username, booking_category_id, dept_id, dice_id, team_id) VALUES (1, NOW() - INTERVAL 1 HOUR - INTERVAL 30 MINUTE, NOW() - INTERVAL 2 HOUR, NOW(), 'user2', 9,1,2,1);
INSERT INTO booking (id, activity_end_date, activity_start_date, object_created_date_time, object_created_user_username, booking_category_id, dept_id, dice_id, team_id) VALUES (2,NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 3 HOUR, NOW(), 'user2', 3,1,2,1);
INSERT INTO booking (id, activity_end_date, activity_start_date, object_created_date_time, object_created_user_username, booking_category_id, dept_id, dice_id, team_id) VALUES (3, NOW() - INTERVAL 3 HOUR, NOW() - INTERVAL 4 HOUR, NOW(), 'user2', 2,1,2,1);
INSERT INTO booking (id, activity_end_date, activity_start_date, object_created_date_time, object_created_user_username, booking_category_id, dept_id, dice_id, team_id) VALUES (4,NOW() - INTERVAL 4 HOUR, NOW() - INTERVAL 5 HOUR, NOW(), 'user2', 3,1,2,1);
INSERT INTO booking (id, activity_end_date, activity_start_date, object_created_date_time, object_created_user_username, booking_category_id, dept_id, dice_id, team_id) VALUES (5,NOW() - INTERVAL 13 DAY, NOW() - INTERVAL 13 DAY - INTERVAL 3 HOUR, NOW(), 'user2', 3,1,2,1);
INSERT INTO booking (id, activity_end_date, activity_start_date, object_created_date_time, object_created_user_username, booking_category_id, dept_id, dice_id, team_id) VALUES (6,NOW() - INTERVAL 28 HOUR, NOW() - INTERVAL 34 HOUR, NOW(), 'admin', 2,1,1,1);
INSERT INTO booking (id, activity_end_date, activity_start_date, object_created_date_time, object_created_user_username, booking_category_id, dept_id, dice_id, team_id) VALUES (7,NOW() - INTERVAL 3 HOUR, NOW() - INTERVAL 8 HOUR , NOW(), 'admin', 3,1,1,1);
INSERT INTO booking (id, activity_end_date, activity_start_date, object_created_date_time, object_created_user_username, booking_category_id, dept_id, dice_id, team_id) VALUES (8,DATE(NOW() - INTERVAL 1 WEEK), DATE(NOW() - INTERVAL 4 WEEK), NOW(), 'admin', 9,1,1,1);
INSERT INTO booking (id, activity_end_date, activity_start_date, object_created_date_time, object_created_user_username, booking_category_id, dept_id, dice_id, team_id) VALUES (9,NOW() - INTERVAL 1 HOUR, NOW() - INTERVAL 2 HOUR, NOW(), 'admin', 12,1,1,1);
INSERT INTO booking (id, activity_end_date, activity_start_date, object_created_date_time, object_created_user_username, booking_category_id, dept_id, dice_id, team_id) VALUES (10,NOW() - INTERVAL 1 HOUR, NOW() - INTERVAL 3 HOUR, NOW(), 'user1', 3,1,3,1);
INSERT INTO booking (id, activity_end_date, activity_start_date, object_created_date_time, object_created_user_username, booking_category_id, dept_id, dice_id, team_id) VALUES (11,DATE(NOW() - INTERVAL 1 WEEK), DATE(NOW() - INTERVAL 2 WEEK), NOW(), 'user1', 2,1,3,1);
INSERT INTO booking (id, activity_end_date, activity_start_date, object_created_date_time, object_created_user_username, booking_category_id, dept_id, dice_id, team_id) VALUES (12,NOW() - INTERVAL 8 HOUR, DATE(NOW() - INTERVAL 1 WEEK), NOW(), 'user1', 1,1,3,1);
INSERT INTO booking (id, activity_end_date, activity_start_date, object_created_date_time, object_created_user_username, booking_category_id, dept_id, dice_id, team_id) VALUES (13,DATE(NOW() - INTERVAL 1 WEEK), DATE(NOW() - INTERVAL 4 WEEK), NOW(), 'user1', 9,1,3,1);
INSERT INTO booking (id, activity_end_date, activity_start_date, object_created_date_time, object_created_user_username, booking_category_id, dept_id, dice_id, team_id) VALUES (14,NOW() - INTERVAL 3 HOUR, NOW() - INTERVAL 8 HOUR, NOW(), 'user1', 2,1,3,1);
INSERT INTO booking (id, activity_end_date, activity_start_date, object_created_date_time, object_created_user_username, booking_category_id, dept_id, dice_id, team_id) VALUES (15,NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 5 HOUR, NOW(), 'user1', 12,1,3,1);
INSERT INTO booking (id, activity_end_date, activity_start_date, object_created_date_time, object_created_user_username, booking_category_id, dept_id, dice_id, team_id) VALUES (16,DATE(NOW() - INTERVAL 1 DAY), DATE(NOW() - INTERVAL 3 DAY), NOW(), 'user1', 8,1,3,1);
INSERT INTO booking (id, activity_end_date, activity_start_date, object_created_date_time, object_created_user_username, booking_category_id, dept_id, dice_id, team_id) VALUES (17,DATE(NOW() - INTERVAL 4 DAY), DATE(NOW() - INTERVAL 5 DAY), NOW(), 'user3', 5,1,4,1);
INSERT INTO booking (id, activity_end_date, activity_start_date, object_created_date_time, object_created_user_username, booking_category_id, dept_id, dice_id, team_id) VALUES (18,(NOW() - INTERVAL 5 HOUR), DATE(NOW() - INTERVAL 1 DAY), NOW(), 'user3', 11,1,4,1);
INSERT INTO booking (id, activity_end_date, activity_start_date, object_created_date_time, object_created_user_username, booking_category_id, dept_id, dice_id, team_id) VALUES (19,DATE(NOW() - INTERVAL 1 DAY), DATE(NOW() - INTERVAL 3 DAY), NOW(), 'user1', 7,1,3,1);
INSERT INTO booking (id, activity_end_date, activity_start_date, object_created_date_time, object_created_user_username, booking_category_id, dept_id, dice_id, team_id) VALUES (20,DATE(NOW() - INTERVAL 1 DAY), DATE(NOW() - INTERVAL 3 DAY), NOW(), 'admin', 7,1,1,1);
INSERT INTO booking (id, activity_end_date, activity_start_date, object_created_date_time, object_created_user_username, booking_category_id, dept_id, dice_id, team_id) VALUES (21,(NOW() - INTERVAL 5 HOUR), NOW() - INTERVAL 7 HOUR , NOW(), 'admin', 11,1,1,1);
INSERT INTO booking (id, activity_end_date, activity_start_date, object_created_date_time, object_created_user_username, booking_category_id, dept_id, dice_id, team_id) VALUES (22,NOW() - INTERVAL 3 HOUR, NOW() - INTERVAL 10 HOUR , NOW(), 'admin', 3,1,1,1);
INSERT INTO booking (id, activity_end_date, activity_start_date, object_created_date_time, object_created_user_username, booking_category_id, dept_id, dice_id, team_id) VALUES (23,NOW() - INTERVAL 3 HOUR, NOW() - INTERVAL 5 HOUR , NOW(), 'admin', 4,1,1,1);
INSERT INTO booking (id, activity_end_date, activity_start_date, object_created_date_time, object_created_user_username, booking_category_id, dept_id, dice_id, team_id) VALUES (24,NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 9 HOUR , NOW(), 'admin', 6,1,1,1);
INSERT INTO booking (id, activity_end_date, activity_start_date, object_created_date_time, object_created_user_username, booking_category_id, dept_id, dice_id, team_id) VALUES (25,NOW() - INTERVAL 3 HOUR, NOW() - INTERVAL 7 HOUR , NOW(), 'admin', 7,1,1,1);

INSERT INTO badgedb (id, badge_name, from_date, to_date, user_username, explanation) values(1, 'Bugsimilian', DATE(NOW() - INTERVAL 7 DAY), DATE(NOW()), 'admin', 'Most hours debugging');
INSERT INTO badgedb (id, badge_name, from_date, to_date, user_username, explanation) values(2, 'Code Raptor Georg', DATE(NOW() - INTERVAL 7 DAY), DATE(NOW()), 'admin', 'Most hours implementation');
INSERT INTO badgedb (id, badge_name, from_date, to_date, user_username, explanation) values(3, 'Bugsimilian', DATE(NOW() - INTERVAL 7 DAY), DATE(NOW()), 'user1', 'Most hours debugging');
INSERT INTO badgedb (id, badge_name, from_date, to_date, user_username, explanation) values(4, 'Frontend Laura', DATE(NOW() - INTERVAL 7 DAY), DATE(NOW()), 'user1', 'Most hours frontend');


INSERT INTO badgedb (id, badge_name, from_date, to_date, user_username, explanation) values(5, 'Bugsimilian', DATE(NOW() - INTERVAL 13 DAY), DATE(NOW() - INTERVAL 8 DAY), 'user2', 'Most hours debugging');
INSERT INTO badgedb (id, badge_name, from_date, to_date, user_username, explanation) values(7, 'Code Raptor Georg', DATE(NOW() - INTERVAL 13 DAY), DATE(NOW() - INTERVAL 8 DAY), 'user3', 'Most hours implementation');
INSERT INTO badgedb (id, badge_name, from_date, to_date, user_username, explanation) values(8, 'Frontend Laura', DATE(NOW() - INTERVAL 13 DAY), DATE(NOW() - INTERVAL 8 DAY), 'admin', 'Most hours frontend');
INSERT INTO badgedb (id, badge_name, from_date, to_date, user_username, explanation) values(6, 'Busy Bee Jamie', DATE(NOW() - INTERVAL 13 DAY), DATE(NOW() - INTERVAL 8 DAY), 'admin', 'Most hours managing');
INSERT INTO badgedb (id, badge_name, from_date, to_date, user_username, explanation) values(9, 'Educated Gabbo', DATE(NOW() - INTERVAL 13 DAY), DATE(NOW() - INTERVAL 8 DAY), 'admin', 'Most hours training and testing');
INSERT INTO badgedb (id, badge_name, from_date, to_date, user_username, explanation) values(10, 'The Sloth', DATE(NOW() - INTERVAL 13 DAY), DATE(NOW() - INTERVAL 8 DAY), 'admin', 'Most hours pause/vacation');


INSERT INTO badgedb (id, badge_name, from_date, to_date, user_username, explanation) values(11, 'Bugsimilian', DATE(NOW() - INTERVAL 7 DAY), DATE(NOW() - INTERVAL 1 DAY), 'user2', 'Most hours debugging');
INSERT INTO badgedb (id, badge_name, from_date, to_date, user_username, explanation) values(12, 'Code Raptor Georg', DATE(NOW() - INTERVAL 7 DAY), DATE(NOW() - INTERVAL 1 DAY), 'user3', 'Most hours implementation');
INSERT INTO badgedb (id, badge_name, from_date, to_date, user_username, explanation) values(13, 'Frontend Laura', DATE(NOW() - INTERVAL 7 DAY), DATE(NOW() - INTERVAL 1 DAY), 'admin', 'Most hours frontend');
INSERT INTO badgedb (id, badge_name, from_date, to_date, user_username, explanation) values(14, 'Busy Bee Jamie', DATE(NOW() - INTERVAL 7 DAY), DATE(NOW() - INTERVAL 1 DAY), 'admin', 'Most hours managing');
INSERT INTO badgedb (id, badge_name, from_date, to_date, user_username, explanation) values(15, 'Educated Gabbo', DATE(NOW() - INTERVAL 7 DAY), DATE(NOW() - INTERVAL 1 DAY), 'admin', 'Most hours training and testing');
INSERT INTO badgedb (id, badge_name, from_date, to_date, user_username, explanation) values(16, 'The Sloth', DATE(NOW() - INTERVAL 7 DAY), DATE(NOW() - INTERVAL 1 DAY), 'admin', 'Most hours pause/vacation');
