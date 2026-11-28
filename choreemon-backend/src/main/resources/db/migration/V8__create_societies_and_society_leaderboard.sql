-- V8: Societies and Society-Level Leaderboard support in Azure SQL
CREATE TABLE Societies (
    id         NVARCHAR(36)  NOT NULL PRIMARY KEY,
    name       NVARCHAR(100) NOT NULL,
    code       NVARCHAR(12)  NOT NULL,
    city       NVARCHAR(100) NOT NULL DEFAULT '',
    created_at NVARCHAR(50)  NOT NULL,
    CONSTRAINT UQ_Societies_Code UNIQUE (code)
);
CREATE INDEX IX_Societies_Name ON Societies(name);

-- Link Households to Societies
ALTER TABLE Households ADD society_id NVARCHAR(36) NULL;
ALTER TABLE Households ADD CONSTRAINT FK_Households_Society FOREIGN KEY (society_id) REFERENCES Societies(id);
CREATE INDEX IX_Households_Society ON Households(society_id);

-- Performance Indexes for Azure SQL Leaderboard queries
CREATE INDEX IX_Members_WeeklyPoints ON Members(weekly_points DESC);
CREATE INDEX IX_Members_TotalPoints ON Members(points DESC);
CREATE INDEX IX_Members_Household_WeeklyPoints ON Members(household_id, weekly_points DESC);

-- Seed default initial community society so existing data has immediate society context
INSERT INTO Societies (id, name, code, city, created_at)
VALUES (
    'soc-default-001',
    'Sunshine Valley Community',
    'SUNVALLEY',
    'San Francisco, CA',
    '2026-10-17T00:00:00Z'
);

UPDATE Households
SET society_id = 'soc-default-001'
WHERE society_id IS NULL;
