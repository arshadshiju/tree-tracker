-- =============================================================
-- TreeTrackr. – Rainforest Restoration Management System
-- seed.sql  |  Sample data for development and testing.
--            |  Covers all tables with representative values
--            |  drawn from the design document examples.
-- =============================================================

USE rainforest;

-- -------------------------------------------------------------
-- Nursery tables  (must exist before plant records reference them)
-- -------------------------------------------------------------
INSERT INTO RAINFOREST_NURSERY_TABLE (TableNameID, Notes) VALUES
    ('A1', 'North-facing row. Check drainage after rain.'),
    ('A2', NULL),
    ('A3', NULL),
    ('A4', NULL),
    ('B1', NULL),
    ('B2', NULL),
    ('B3', NULL),
    ('B4', NULL),
    ('C1', 'Shaded section – suitable for full-shade species only.'),
    ('D1', NULL),
    ('E1', NULL),
    ('E2', NULL),
    ('E3', NULL),
    ('E4', 'Main display table. High visitor traffic.'),
    ('F1', NULL),
    ('F2', NULL),
    ('F3', NULL),
    ('F4', NULL);

-- -------------------------------------------------------------
-- Administrator account
-- Password is a placeholder – replace before deploying.
-- -------------------------------------------------------------
INSERT INTO admin (UsernameID, Password, Email) VALUES
    (10001, 'REDACTED', 'admin@school.edu.au');

-- -------------------------------------------------------------
-- Student accounts
-- Passwords are placeholders – replace before deploying.
-- -------------------------------------------------------------
INSERT INTO student (UsernameID, Password, FirstName, LastName, Email) VALUES
    (13450, 'REDACTED', 'Jane',   'Doe',    'jane.doe@school.edu.au'),
    (13451, 'REDACTED', 'Marcus', 'Lee',    'marcus.lee@school.edu.au'),
    (13452, 'REDACTED', 'Priya',  'Sharma', 'priya.sharma@school.edu.au');

-- -------------------------------------------------------------
-- Plant species records
-- RecordID is AUTO_INCREMENT; values shown for reference only.
-- -------------------------------------------------------------
INSERT INTO PLANT_SPECIES_RECORD
    (UsernameID, TableNameID, PlantIndex, PlantSpecies,
     Quantity, SoilMoisture, SunlightExposure, PlantStatus,
     PlantStage, PlantType, LastFertilised, PlantHeight, PlantWidth)
VALUES
    -- E4 records (from design doc example)
    (13450, 'E4', 10022, 'Lorem Ipsum',              4, 'High',   'Partial Shade', 'Fair',      'Seedling',   'Ornamental Flower', '2024-07-10', 32.5, 12.0),
    (13450, 'E4', 10023, 'Dolor Sit',                2, 'Low',    'Full Sun',      'Senescence','Senescence', 'Fruit',             '2024-09-10', 18.0,  8.3),
    -- A1 records
    (13451, 'A1', 10045, 'Coleus bellus',            3, 'Medium', 'Partial Shade', 'Good',      'Vegetative', 'Ornamental Flower', '2024-10-01', 22.1,  9.5),
    (13451, 'A1', 10046, 'Aristolochia philippinensis', 1, 'High', 'Full Shade',   'Excellent', 'Budding',    'Ornamental Flower', '2024-09-28', 45.0, 15.2),
    -- A2 records
    (13452, 'A2', 10067, 'Aloe Vera',                7, 'Low',    'Full Sun',      'Excellent', 'Vegetative', 'Leaf',              '2024-08-15', 30.0, 20.0),
    -- C1 records (full shade)
    (13452, 'C1', 10089, 'Monstera deliciosa',       2, 'High',   'Full Shade',    'Good',      'Flowering',  'Ornamental Flower', '2024-07-22', 60.0, 25.0),
    -- Rose example from design doc INSERT example
    (13450, 'E4', 93765, 'Rose',                     3, 'Medium', 'Full Sun',      'Good',      'Flowering',  'Ornamental Flower', '2024-04-10', 23.4, 25.7);

-- -------------------------------------------------------------
-- Historical records  (links students to records they accessed)
-- -------------------------------------------------------------
INSERT INTO STUDENT_HISTORICAL_RECORD (UsernameID, RecordID) VALUES
    (13450, 1),
    (13451, 3),
    (13452, 5);
