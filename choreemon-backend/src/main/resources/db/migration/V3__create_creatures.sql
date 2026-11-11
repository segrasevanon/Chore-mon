CREATE TABLE Creatures (
    id           NVARCHAR(36) NOT NULL PRIMARY KEY,
    owner_id     NVARCHAR(36) NOT NULL,
    element      NVARCHAR(10) NOT NULL,
    rarity       NVARCHAR(12) NOT NULL,
    xp           INT          NOT NULL DEFAULT 0,
    stage        INT          NOT NULL DEFAULT 0,
    nickname     NVARCHAR(64) NOT NULL DEFAULT '',
    adopted_date NVARCHAR(10) NOT NULL,
    loved_on     NVARCHAR(10) NOT NULL DEFAULT '',
    is_favorite  BIT          NOT NULL DEFAULT 0,
    is_active    BIT          NOT NULL DEFAULT 0,
    CONSTRAINT FK_Creatures_Member FOREIGN KEY (owner_id) REFERENCES Members(id)
);
CREATE INDEX IX_Creatures_Owner ON Creatures(owner_id);
