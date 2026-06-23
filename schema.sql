-- =============================================================
-- TreeTrackr. – Rainforest Restoration Management System
-- schema.sql  |  Database schema with all tables, constraints,
--              |  and foreign key relationships.
-- =============================================================

CREATE DATABASE IF NOT EXISTS rainforest
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE rainforest;

-- -------------------------------------------------------------
-- STUDENT
-- Stores student user accounts. UsernameID maps to the
-- student's physical school ID card (5-digit integer).
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS student (
    UsernameID  INTEGER      NOT NULL,
    Password    VARCHAR(8)   NOT NULL,
    FirstName   VARCHAR(10)  NOT NULL,
    LastName    VARCHAR(10)  NOT NULL,
    Email       VARCHAR(30)  NOT NULL,
    CONSTRAINT pk_student         PRIMARY KEY (UsernameID),
    CONSTRAINT chk_student_uid    CHECK (UsernameID > 0),
    CONSTRAINT chk_student_pwd    CHECK (LENGTH(Password) BETWEEN 1 AND 8)
);

-- -------------------------------------------------------------
-- ADMINISTRATOR
-- Stores administrator user accounts. Separate table from
-- STUDENT so role validation is enforced at the schema level.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS admin (
    UsernameID  INTEGER      NOT NULL,
    Password    VARCHAR(8)   NOT NULL,
    Email       VARCHAR(30)  NOT NULL,
    CONSTRAINT pk_admin      PRIMARY KEY (UsernameID),
    CONSTRAINT chk_admin_uid CHECK (UsernameID > 0)
);

-- -------------------------------------------------------------
-- RAINFOREST_NURSERY_TABLE
-- Registry of physical nursery tables (A1–F4).
-- TableNameID is a 2-character code: one letter + one digit.
-- Notes are optional free-text descriptions entered by staff.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS RAINFOREST_NURSERY_TABLE (
    TableNameID  VARCHAR(2)    NOT NULL,
    Notes        VARCHAR(1000) NULL,
    CONSTRAINT pk_nursery_table  PRIMARY KEY (TableNameID),
    CONSTRAINT chk_table_name_id CHECK (TableNameID REGEXP '^[A-F][1-4]$')
);

-- -------------------------------------------------------------
-- PLANT_SPECIES_RECORD
-- Central fact table. One row per plant species per nursery
-- table. RecordID is auto-incremented by MySQL.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS PLANT_SPECIES_RECORD (
    RecordID         INTEGER      NOT NULL AUTO_INCREMENT,
    UsernameID       INTEGER      NOT NULL,
    TableNameID      VARCHAR(2)   NOT NULL,
    PlantIndex       INTEGER      NOT NULL,
    PlantSpecies     VARCHAR(40)  NOT NULL,
    Quantity         INTEGER      NOT NULL,
    SoilMoisture     VARCHAR(7)   NOT NULL,
    SunlightExposure VARCHAR(13)  NOT NULL,
    PlantStatus      VARCHAR(13)  NOT NULL,
    PlantStage       VARCHAR(13)  NOT NULL,
    PlantType        VARCHAR(13)  NOT NULL,
    LastFertilised   DATE         NOT NULL,
    PlantHeight      FLOAT        NOT NULL,
    PlantWidth       FLOAT        NOT NULL,
    CONSTRAINT pk_plant_record        PRIMARY KEY (RecordID),
    CONSTRAINT uq_plant_index         UNIQUE      (PlantIndex),
    CONSTRAINT fk_plant_student       FOREIGN KEY (UsernameID)
        REFERENCES student(UsernameID)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_plant_nursery_table FOREIGN KEY (TableNameID)
        REFERENCES RAINFOREST_NURSERY_TABLE(TableNameID)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_quantity           CHECK (Quantity > 0),
    CONSTRAINT chk_plant_height       CHECK (PlantHeight > 0),
    CONSTRAINT chk_plant_width        CHECK (PlantWidth  > 0),
    CONSTRAINT chk_soil_moisture      CHECK (SoilMoisture     IN ('Low','Medium','High')),
    CONSTRAINT chk_sunlight           CHECK (SunlightExposure IN ('Full Sun','Partial Shade','Full Shade')),
    CONSTRAINT chk_plant_status       CHECK (PlantStatus      IN ('Excellent','Good','Fair','Poor','Dead')),
    CONSTRAINT chk_plant_stage        CHECK (PlantStage       IN ('Sprout','Seedling','Vegetative',
                                                                   'Budding','Flowering','Ripening','Senescence')),
    CONSTRAINT chk_plant_type         CHECK (PlantType        IN ('Ornamental Flower','Fruit','Leaf','Stem','Root'))
);

-- -------------------------------------------------------------
-- STUDENT_HISTORICAL_RECORD
-- Junction / audit table tracking which students have accessed
-- or modified which plant records.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS STUDENT_HISTORICAL_RECORD (
    UsernameID  INTEGER NOT NULL,
    RecordID    INTEGER NOT NULL,
    CONSTRAINT pk_history          PRIMARY KEY (UsernameID),
    CONSTRAINT fk_history_student  FOREIGN KEY (UsernameID)
        REFERENCES student(UsernameID)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_history_record   FOREIGN KEY (RecordID)
        REFERENCES PLANT_SPECIES_RECORD(RecordID)
        ON UPDATE CASCADE ON DELETE CASCADE
);
