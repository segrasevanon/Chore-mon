CREATE TABLE Completions (
    id          NVARCHAR(36)  NOT NULL PRIMARY KEY,
    chore_id    NVARCHAR(36)  NOT NULL,
    member_id   NVARCHAR(36)  NOT NULL,
    creature_id NVARCHAR(36)  NOT NULL,
    date        NVARCHAR(10)  NOT NULL,
    title       NVARCHAR(100) NOT NULL,
    points      INT           NOT NULL,
    xp          INT           NOT NULL,
    status      NVARCHAR(10)  NOT NULL DEFAULT 'PENDING',
    photo_url   NVARCHAR(500) NOT NULL DEFAULT '',
    CONSTRAINT FK_Completions_Chore    FOREIGN KEY (chore_id)    REFERENCES Chores(id),
    CONSTRAINT FK_Completions_Member   FOREIGN KEY (member_id)   REFERENCES Members(id),
    CONSTRAINT FK_Completions_Creature FOREIGN KEY (creature_id) REFERENCES Creatures(id)
);
CREATE INDEX IX_Completions_Member ON Completions(member_id);
CREATE INDEX IX_Completions_Status ON Completions(status);
