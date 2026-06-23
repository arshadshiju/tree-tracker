# TreeTracker Rainforest Restoration System

> A Java Swing application for tracking and managing plant species in a school rainforest nursery,
> supporting both Student and Administrator roles.

---

## Table of Contents
1. [System Overview](#system-overview)
2. [File Structure](#file-structure)
3. [Database Design](#database-design)
4. [Data Dictionary](#data-dictionary)
5. [OOP & UML Design](#oop--uml-design)
6. [Key Algorithms](#key-algorithms)
7. [Validation Rules](#validation-rules)
8. [Test Plan Summary](#test-plan-summary)
9. [Security Changes Made](#security-changes-made)
10. [Setup & Build](#setup--build)

---

## System Overview

TreeTrackr. is built around a student-administrator model:

- **Students** log in to view the nursery layout, browse plant species data, and view their assigned tables.
- **Administrators** can manage student accounts, add/edit/delete plant records, view dead plants, track fertilisation schedules, and access a species-filter view.

### High-level flow

```
Login Page
  ├── UserType = Administrator  →  Admin Homepage
  │     ├── Student List (add / edit / delete)
  │     ├── Plant Species Data table
  │     ├── Dead Plants panel
  │     ├── Last Fertilised panel (date filter)
  │     ├── Species filter panel
  │     └── → Nursery Layout
  └── UserType = Student        →  Nursery Layout
        ├── Grid (A1–F4) with eye (view) and + (add) buttons
        └── Plant Species summary table
```

---

## File Structure

```
src/
├── DBConfig.java                   – Central DB credentials (update this once)
├── PlantInfo.java                  – Value object for bar-graph data
├── RainforestRestorationLogin.java – Entry point + shared DB helpers
├── LoginPanel.java                 – Login screen & forgot-password dialog
├── AdminPage.java                  – Administrator home page
├── NurseryPage.java                – Nursery grid page (both roles)
└── TableEditor.java                – Abstract grid-cell editor +
                                      A1TableEditor, A2TableEditor,
                                      DefaultTableEditor
```

---

## Database Design

### Entity-Relationship Summary

Five tables are linked through foreign keys:

| Table | Primary Key | Foreign Keys |
|---|---|---|
| `STUDENT` | `UsernameID` | — |
| `ADMINISTRATOR` | `UsernameID` | — |
| `RAINFOREST_NURSERY_TABLE` | `TableNameID` | — |
| `PLANT_SPECIES_RECORD` | `RecordID` | `UsernameID → STUDENT`, `TableNameID → RAINFOREST_NURSERY_TABLE` |
| `STUDENT_HISTORICAL_RECORD` | `UsernameID` | `RecordID → PLANT_SPECIES_RECORD` |

### CREATE statements (reference)

```sql
CREATE TABLE STUDENT (
    UsernameID INTEGER PRIMARY KEY,
    Password   VARCHAR(8)  NOT NULL,
    FirstName  VARCHAR(10) NOT NULL,
    LastName   VARCHAR(10) NOT NULL,
    Email      VARCHAR(30) NOT NULL
);

CREATE TABLE ADMINISTRATOR (
    UsernameID INTEGER PRIMARY KEY,
    Password   VARCHAR(8)  NOT NULL,
    Email      VARCHAR(30) NOT NULL
);

CREATE TABLE RAINFOREST_NURSERY_TABLE (
    TableNameID VARCHAR(2)    PRIMARY KEY,
    Notes       VARCHAR(1000)
);

CREATE TABLE PLANT_SPECIES_RECORD (
    RecordID         INTEGER PRIMARY KEY,
    UsernameID       INTEGER NOT NULL,
    TableNameID      VARCHAR(2)  NOT NULL,
    PlantIndex       INTEGER     NOT NULL UNIQUE,
    PlantSpecies     VARCHAR(40) NOT NULL,
    Quantity         INTEGER     NOT NULL CHECK (Quantity > 0),
    SoilMoisture     VARCHAR(7)  NOT NULL,
    SunlightExposure VARCHAR(13) NOT NULL,
    PlantStatus      VARCHAR(13) NOT NULL,
    PlantStage       VARCHAR(13) NOT NULL,
    PlantType        VARCHAR(13) NOT NULL,
    LastFertilised   DATE        NOT NULL,
    PlantHeight      FLOAT       NOT NULL,
    PlantWidth       FLOAT       NOT NULL,
    FOREIGN KEY (UsernameID)  REFERENCES STUDENT(UsernameID),
    FOREIGN KEY (TableNameID) REFERENCES RAINFOREST_NURSERY_TABLE(TableNameID)
);

CREATE TABLE STUDENT_HISTORICAL_RECORD (
    UsernameID INTEGER,
    RecordID   INTEGER,
    PRIMARY KEY (UsernameID),
    FOREIGN KEY (RecordID) REFERENCES PLANT_SPECIES_RECORD(RecordID)
);
```

---

## Data Dictionary

### STUDENT

| Field | Type | Length | Constraint | Description |
|---|---|---|---|---|
| UsernameID | Integer | 5 | Primary Key | Student ID card number |
| Password | String | 8 | Not null | Login password |
| FirstName | String | 10 | Not null | First name |
| LastName | String | 10 | Not null | Surname |
| Email | VarChar | 30 | Not null | Student email |

### ADMINISTRATOR

| Field | Type | Length | Constraint | Description |
|---|---|---|---|---|
| UsernameID | Integer | 5 | Primary Key | Administrator ID |
| Password | String | 8 | Not null | Login password |
| Email | VarChar | 30 | Not null | Administrator email |

### RAINFOREST_NURSERY_TABLE

| Field | Type | Length | Constraint | Description |
|---|---|---|---|---|
| TableNameID | String | 2 | Primary Key | e.g. "A1", "E4" |
| Notes | String | 1000 | Optional | Free-text description of table |

### PLANT_SPECIES_RECORD

| Field | Type | Length | Constraint | Description |
|---|---|---|---|---|
| RecordID | Integer | 7 | Primary Key | Unique record ID |
| UsernameID | Integer | 5 | FK → STUDENT | Student who added record |
| TableNameID | String | 2 | FK → NURSERY_TABLE | Nursery table location |
| PlantIndex | Integer | 5 | Unique, Not null | Unique species code |
| PlantSpecies | String | 40 | Not null | Species name |
| Quantity | Integer | 2 | >0, Not null | Number of plants |
| SoilMoisture | String | 7 | Not null | Low / Medium / High |
| SunlightExposure | String | 13 | Not null | Full Sun / Partial Shade / Full Shade |
| PlantStatus | String | 13 | Not null | Excellent / Good / Fair / Poor / Dead |
| PlantStage | String | 13 | Not null | Sprout → Senescence |
| PlantType | String | 13 | Not null | Ornamental Flower / Fruit / Leaf / Stem / Root |
| LastFertilised | Date | 8 | Not null | YYYY-MM-DD |
| PlantHeight | Float | 5 | >0, Not null | Height in cm |
| PlantWidth | Float | 5 | >0, Not null | Width in cm |

### STUDENT_HISTORICAL_RECORD

| Field | Type | Constraint | Description |
|---|---|---|---|
| UsernameID | Integer | Primary Key, FK → STUDENT | Student identifier |
| RecordID | Integer | FK → PLANT_SPECIES_RECORD | Plant record accessed |

---

## OOP & UML Design

### Internal data structures

| Structure | Declaration | Purpose |
|---|---|---|
| 2-D Array | `String UserData[][]` | Stores user info (password, name, email) dynamically |
| 1-D Array | `int UserNames[]` | Stores integer usernames for binary search |
| HashMap | `RainforestNurseryTables = new HashMap<>()` | Maps table names to `RainforestNurseryTable` objects |
| Linked List | `class plantIndex { Node head; … }` | Efficient insertion/deletion of plantIndex data |

### Class hierarchy

```
RainforestNurseryTable          PlantRecord (abstract)
  ├── E3                          ├── Coleus bellus
  └── E4                          └── Aristolochia philippinensis

RainforestRestorationLogin  ──has-a──►  User (abstract)
                                          ├── Administrator
                                          └── Student
```

Key OOP principles applied:
- **Abstraction** – `RainforestNurseryTable` and `PlantRecord` are abstract base classes
- **Inheritance** – `E3`, `E4`, `Administrator`, `Student` extend their parents
- **Polymorphism** – `getPassword()` behaves differently for `Administrator` vs `Student`
- **Encapsulation** – private fields with getters/setters throughout
- **Aggregation** – `RainforestNurseryTable` has-a `PlantRecord`

---

## Key Algorithms

### 1. Login with lockout (CFS 7.d)
Users get 5 attempts within a 3600-second window. On exceeding, an email is sent and login is blocked for one hour. Internally uses a **binary search** (`binarySearch()` on sorted `UserNames[]`) to locate the username before querying the password.

### 2. Find overdue fertilised plants (CFS 9)
```
expiry_date = current_date - INTERVAL days_expired DAY
SELECT RecordID, PlantSpecies, TableNameID, LastFertilised
FROM PLANT_SPECIES_RECORD
WHERE LastFertilised < expiry_date
```
Results are iterated and output with their nursery table location.

### 3. Total unique plant species (CFS 2.b.i)
```
SELECT DISTINCT PlantIndex FROM PLANT_SPECIES_RECORD
```
Results are added to a `Set` (no duplicates); `set.size()` gives the total — used for graph rendering.

### 4. Edit plant record (CFS 3.a)
User selects search criteria (RecordID, PlantSpecies, or LastFertilised), inputs new values for any fields (blank = keep current), and a dynamic `UPDATE` statement is built from only the changed fields.

### 5. Admin binary search for student (CFS 7.b.v)
```
query all UsernameIDs ORDER BY UsernameID ASC → userNames[]
binary search userNames[] for SEARCH_USERNAME
if found → SELECT FirstName, LastName, Email WHERE UsernameID = SEARCH_USERNAME
```

### 6. Alphabetical sort of student list (bubble sort)
Implemented in `AdminPage.bubbleSort()` — swaps rows in `studentTableModel` comparing `FirstName` column case-insensitively.

---

## Validation Rules

### Login / User fields

| Field | Rule |
|---|---|
| UsernameID | 5-digit integer, >0, unique, not null |
| Password | 1–8 alphanumeric characters, not null |
| FirstName / LastName | Max 10 alpha characters, not null |
| Email | Max 30 chars, valid `name@domain.com` format |

### Plant record fields

| Field | Rule |
|---|---|
| TableNameID | 2 chars, format `[A-F][1-4]`, not null |
| PlantIndex | 5-digit integer, >0, unique, not null |
| PlantSpecies | Max 40 characters, not null |
| Quantity | Integer, >0, not null |
| SoilMoisture | Must be one of: Low / Medium / High |
| SunlightExposure | Must be one of: Full Sun / Partial Shade / Full Shade |
| PlantStatus | Must be one of: Excellent / Good / Fair / Poor / Dead |
| PlantStage | Must be one of: Sprout / Seedling / Vegetative / Budding / Flowering / Ripening / Senescence |
| PlantType | Must be one of: Ornamental Flower / Fruit / Leaf / Stem / Root |
| LastFertilised | Valid date YYYY-MM-DD, not null |
| PlantHeight / PlantWidth | Float, >0, not null |

---

## Test Plan Summary

| CFS | Test | Test Data | Expected Result |
|---|---|---|---|
| 1 | View nursery layout | N/A | JFrame renders correctly |
| 1 | Invalid TableNameID | `0413` | Error: "Table identifiers missing" |
| 2 | Valid UsernameID | `93765` | Accepted |
| 2 | Invalid UsernameID (text) | `Jane Doe` | Error: "Usernames are 5-digit numeric codes" |
| 2 | Invalid UsernameID (short) | `9376` | Error: same |
| 2 | Invalid UsernameID (negative) | `-93765` | Error: same |
| 2 | Valid TableNameID | `E5` | Accepted |
| 2 | Invalid TableNameID (numeric) | `435` | Error: "Table Names are ALPHABET_NUMBER" |
| 2 | Valid Quantity | `4` | Accepted |
| 2 | Invalid Quantity (text) | `Forty Three` | Error: "Quantity must be 1–2 digit whole number" |
| 2 | Invalid Quantity (negative) | `-4` | Error: same |
| 2 | Valid PlantWidth / PlantHeight | `19.4` / `17.3` | Accepted |
| 3 | Change quantity to 0 | `0` | Accepted; record removed from active species |
| 3 | Change quantity negative | `-3` | Error: "Quantity must be positive" |
| 3 | Notes within limit | ≤1000 chars | Accepted |
| 3 | Notes over limit | >1000 chars | Error: "Notes cannot exceed 1000 characters" |
| 4 | Valid TableNameID label | `E5` | Displayed on nursery layout |
| 4 | Invalid TableNameID | `435` | Error: "Re-enter as ALPHABET_NUMBER code" |
| 6 | Add valid PlantIndex | `9873456` | Accepted |
| 6 | Add duplicate PlantIndex | `9873456` | Error: "Duplicate PlantIndex detected" |
| 6 | Add negative PlantIndex | `-9873456` | Error: "PlantIndexes are 7-digit codes" |
| 7 | Valid login | correct credentials | Login accepted |
| 7 | Exceed 5 login attempts | `STRIKE > 5` | Lockout + email sent |
| 7 | Invalid password length | >8 chars | Error: "Passwords are max 8 alphanumeric chars" |
| 7 | Valid email | `jane@gmail.com` | Accepted |
| 7 | Invalid email | `jane.do#gmail#con` | Error: "Must be NAME@gmail.com format" |
| 9 | Search valid UsernameID (admin) | `93765` | Student details returned |
| 9 | Search by student for another student | `93765` | Error: "Students cannot access others' data" |
| 9 | Search valid PlantIndex location | `1234567` | Nursery table returned |
| 9 | Search PlantIndex (type error) | `Aloe Vera` | Error: "PlantIndexes are 7-digit codes" |
| 9 | Invalid days_expired type | `4.5` | Error: "Must be a whole number date" |

---

## Security Changes Made

| What was redacted | Where |
|---|---|
| Hard-coded DB password constant `PASS` | Replaced with `"REDACTED"` in `DBConfig.java` |
| Hard-coded password in `updateStudentInDatabase()` | Removed; method now uses `DBConfig` |
| Hard-coded password in `createTableModel()` | Removed; method now uses `DBConfig` |
| Hard-coded password in `main()` | Removed; method now uses `DBConfig` |
| Fallback plain-text passwords in `initializeUsers()` | Replaced with `"REDACTED"` |
| Hard-coded absolute asset file paths | Replaced with `ASSETS_DIR` constant |

---

## Setup & Build

### 1. Credentials
Open `DBConfig.java` and replace `"REDACTED"` with your actual MySQL password.
**Never commit real credentials to version control.** Consider an environment variable:
```java
public static final String PASSWORD = System.getenv("DB_PASSWORD");
```

### 2. Assets
Set the `ASSETS_DIR` constant in each file to the folder containing your images
(`Rainforest Logo.png`, `Eye Icon.png`, `Add.png`, `Delete.png`, etc.).

### 3. Compile
Requires `mysql-connector-j` and `jcalendar` JARs on the classpath:
```bash
javac -cp .:mysql-connector-j-*.jar:jcalendar-*.jar src/*.java
```

### 4. Run
```bash
java -cp .:mysql-connector-j-*.jar:jcalendar-*.jar RainforestRestorationLogin
```
