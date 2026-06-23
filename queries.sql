-- =============================================================
-- TreeTrackr. – Rainforest Restoration Management System
-- queries.sql  |  All key SQL operations used by the application,
--               |  organised by feature / Criteria for Success (CFS).
-- =============================================================

USE rainforest;

-- =============================================================
-- AUTHENTICATION  (CFS 7)
-- =============================================================

-- Validate student login (called by LoginPanel.validateLogin)
SELECT *
FROM student
WHERE UsernameID = ?
  AND Password   = ?;

-- Validate administrator login
SELECT *
FROM admin
WHERE UsernameID = ?
  AND Password   = ?;

-- Retrieve student email for lockout notification (CFS 7.d)
SELECT Email
FROM student
WHERE UsernameID = ?;

-- Retrieve student password for forgot-password dialog (admin-authorised)
SELECT Password
FROM student
WHERE UsernameID = ?;

-- Validate admin credentials inside forgot-password dialog
SELECT *
FROM admin
WHERE UsernameID = ?
  AND Password   = ?;


-- =============================================================
-- USER MANAGEMENT  (CFS 7.b)
-- =============================================================

-- Load all student accounts into the admin student list table
SELECT UsernameID, FirstName, LastName, Email
FROM student;

-- Binary-search prerequisite: sorted username array (CFS 7.b.v)
SELECT UsernameID
FROM student
ORDER BY UsernameID ASC;

-- Retrieve full student details after binary search locates the username
SELECT FirstName, LastName, Email
FROM student
WHERE UsernameID = ?;

-- Add a new student account (CFS 7.b.i)
INSERT INTO student (UsernameID, FirstName, LastName, Email, Password)
VALUES (?, ?, ?, ?, ?);

-- Update student details (CFS 7.b)
UPDATE student
SET FirstName = ?,
    LastName  = ?,
    Email     = ?
WHERE UsernameID = ?;

-- Delete a student account (CFS 7.b.ii)
DELETE FROM student
WHERE UsernameID = ?;


-- =============================================================
-- PLANT SPECIES DATA  (CFS 2, 3)
-- =============================================================

-- Load all plant records into the main data table
SELECT *
FROM PLANT_SPECIES_RECORD;

-- Load records for a specific nursery table (used by TableEditor subclasses)
SELECT *
FROM PLANT_SPECIES_RECORD
WHERE TableNameID = ?;

-- Load records for a specific plant species (species drill-down panel)
SELECT *
FROM PLANT_SPECIES_RECORD
WHERE PlantSpecies = ?;

-- Get distinct species list for the species combo box
SELECT DISTINCT PlantSpecies
FROM PLANT_SPECIES_RECORD;

-- Add a new plant record (CFS 3.b)
INSERT INTO PLANT_SPECIES_RECORD
    (UsernameID, TableNameID, PlantIndex, PlantSpecies,
     Quantity, SoilMoisture, SunlightExposure, PlantStatus,
     PlantStage, PlantType, LastFertilised, PlantWidth, PlantHeight)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);

-- Update a plant record's fields (CFS 3.a)
-- (built dynamically in Java from only the changed fields)
UPDATE PLANT_SPECIES_RECORD
SET PlantSpecies     = ?,
    Quantity         = ?,
    SoilMoisture     = ?,
    SunlightExposure = ?,
    PlantStatus      = ?,
    PlantStage       = ?,
    PlantType        = ?,
    LastFertilised   = ?,
    PlantHeight      = ?,
    PlantWidth       = ?
WHERE RecordID = ?;

-- Update only PlantStatus (quick dead/alive toggle example)
UPDATE PLANT_SPECIES_RECORD
SET PlantStatus = ?
WHERE PlantSpecies = ?;

-- Update only Quantity
UPDATE PLANT_SPECIES_RECORD
SET Quantity = ?
WHERE PlantSpecies = ?;

-- Delete a plant record (CFS 3.a)
DELETE FROM PLANT_SPECIES_RECORD
WHERE RecordID = ?;


-- =============================================================
-- DEAD PLANTS PANEL  (CFS 9 / Admin homepage)
-- =============================================================

-- Retrieve all dead plant records, sorted by ID
SELECT RecordID, PlantSpecies, TableNameID, Quantity, PlantStatus
FROM PLANT_SPECIES_RECORD
WHERE PlantStatus = 'Dead'
ORDER BY RecordID;


-- =============================================================
-- LAST FERTILISED PANEL  (CFS 9 / Admin homepage)
-- =============================================================

-- Retrieve plants fertilised before a user-selected date,
-- most overdue first
SELECT RecordID, PlantSpecies, TableNameID, Quantity, LastFertilised
FROM PLANT_SPECIES_RECORD
WHERE LastFertilised < ?
ORDER BY LastFertilised DESC;


-- =============================================================
-- OVERDUE FERTILISATION ALGORITHM  (CFS 9)
-- The Java layer computes expiry_date = CURRENT_DATE - INTERVAL n DAY
-- and passes it as the parameter below.
-- =============================================================

SELECT RecordID, PlantSpecies, TableNameID, LastFertilised
FROM PLANT_SPECIES_RECORD
WHERE LastFertilised < ?
ORDER BY LastFertilised ASC;

-- Confirm the nursery table exists for each returned record
SELECT TableNameID
FROM RAINFOREST_NURSERY_TABLE
WHERE TableNameID = ?;


-- =============================================================
-- STATISTICS & GRAPHS  (CFS 2.b)
-- =============================================================

-- Total quantity of all plants in the nursery
SELECT SUM(Quantity) AS TotalPlantsInNursery
FROM PLANT_SPECIES_RECORD;

-- Total number of distinct plant species (CFS 2.b.i)
SELECT COUNT(DISTINCT PlantIndex) AS TotalDistinctPlantSpecies
FROM PLANT_SPECIES_RECORD;

-- Per-species aggregation for the login page bar chart
SELECT PlantSpecies,
       SUM(Quantity)                        AS TotalQuantity,
       GROUP_CONCAT(DISTINCT TableNameID)   AS Locations,
       GROUP_CONCAT(DISTINCT RecordID)      AS RecordIDs
FROM PLANT_SPECIES_RECORD
GROUP BY PlantSpecies
ORDER BY TotalQuantity DESC;

-- Retrieve dead plant record IDs for the status report
SELECT PlantStatus, RecordID
FROM PLANT_SPECIES_RECORD
WHERE PlantStatus = 'Dead';


-- =============================================================
-- NURSERY TABLE NOTES  (CFS 5 / TableEditor)
-- =============================================================

-- Fetch notes for a specific nursery table
SELECT Notes
FROM RAINFOREST_NURSERY_TABLE
WHERE TableNameID = ?;

-- Save updated notes for a nursery table
UPDATE RAINFOREST_NURSERY_TABLE
SET Notes = ?
WHERE TableNameID = ?;


-- =============================================================
-- ADMIN CREDENTIALS  (loaded at startup by initializeUsers)
-- =============================================================

SELECT UsernameID, Password
FROM admin;

SELECT UsernameID, Password
FROM student;
