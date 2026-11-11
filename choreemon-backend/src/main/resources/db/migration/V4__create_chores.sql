CREATE TABLE Chores (
    id                NVARCHAR(36)   NOT NULL PRIMARY KEY,
    household_id      NVARCHAR(36)   NOT NULL,
    title             NVARCHAR(100)  NOT NULL,
    description       NVARCHAR(500)  NOT NULL DEFAULT '',
    points            INT            NOT NULL,
    element           NVARCHAR(10)   NOT NULL,
    schedule          NVARCHAR(12)   NOT NULL DEFAULT 'DAILY',
    requires_approval BIT            NOT NULL DEFAULT 0,
    archived          BIT            NOT NULL DEFAULT 0,
    steps             NVARCHAR(MAX)  NOT NULL DEFAULT '[]',
    CONSTRAINT FK_Chores_Household FOREIGN KEY (household_id) REFERENCES Households(id)
);
CREATE INDEX IX_Chores_Household ON Chores(household_id);

CREATE TABLE ChoreAssignments (
    chore_id  NVARCHAR(36) NOT NULL,
    member_id NVARCHAR(36) NOT NULL,
    PRIMARY KEY (chore_id, member_id),
    CONSTRAINT FK_CA_Chore  FOREIGN KEY (chore_id)  REFERENCES Chores(id),
    CONSTRAINT FK_CA_Member FOREIGN KEY (member_id) REFERENCES Members(id)
);
