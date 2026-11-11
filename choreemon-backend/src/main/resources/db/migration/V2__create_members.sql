CREATE TABLE Members (
    id                  NVARCHAR(36)  NOT NULL PRIMARY KEY,
    household_id        NVARCHAR(36)  NOT NULL,
    auth_email          NVARCHAR(256) NULL,
    auth_password_hash  NVARCHAR(256) NULL,
    name                NVARCHAR(64)  NOT NULL,
    avatar              INT           NOT NULL DEFAULT 0,
    points              INT           NOT NULL DEFAULT 0,
    weekly_points       INT           NOT NULL DEFAULT 0,
    is_parent           BIT           NOT NULL DEFAULT 0,
    CONSTRAINT FK_Members_Household FOREIGN KEY (household_id) REFERENCES Households(id)
);
CREATE INDEX IX_Members_Household ON Members(household_id);
CREATE UNIQUE INDEX UQ_Members_Email ON Members(auth_email) WHERE auth_email IS NOT NULL;
