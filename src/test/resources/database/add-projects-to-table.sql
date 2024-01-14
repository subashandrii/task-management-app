INSERT INTO projects (id, name, description, start_date, end_date, user_id, status)
VALUES (1, 'project1', 'description1', CURRENT_DATE - INTERVAL 4 DAY, CURRENT_DATE - INTERVAL 1 DAY, 1, 'IN_PROGRESS');

INSERT INTO projects (id, name, description, start_date, end_date, user_id, status)
VALUES (2, 'project2', 'description2', CURRENT_DATE - INTERVAL 3 DAY, CURRENT_DATE, 1, 'COMPLETED');

INSERT INTO projects (id, name, description, start_date, end_date, user_id, status)
VALUES (3, 'project3', 'description3', CURRENT_DATE - INTERVAL 1 DAY, CURRENT_DATE, 1, 'IN_PROGRESS');

INSERT INTO projects (id, name, description, start_date, end_date, user_id, status)
VALUES (4, 'project4', 'description4', CURRENT_DATE - INTERVAL 1 DAY, CURRENT_DATE + INTERVAL 2 DAY, 2, 'IN_PROGRESS');

INSERT INTO projects (id, name, description, start_date, end_date, user_id, status)
VALUES (5, 'project5', 'description5', CURRENT_DATE, CURRENT_DATE + INTERVAL 3 DAY, 1, 'INITIATED');
