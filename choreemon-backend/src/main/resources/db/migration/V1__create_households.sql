CREATE TABLE Households (
    id           NVARCHAR(36)  NOT NULL PRIMARY KEY,
    name         NVARCHAR(100) NOT NULL,
    invite_code  NVARCHAR(12)  NOT NULL,
    pin_salt     NVARCHAR(32)  NOT NULL,
    pin_hash     NVARCHAR(64)  NOT NULL,
    week_start   NVARCHAR(10)  NOT NULL,
    created_at   NVARCHAR(50)  NOT NULL,
    CONSTRAINT UQ_invite_code UNIQUE (invite_code)
);
