CREATE TABLE DeviceTokens (
    id         NVARCHAR(36)  NOT NULL PRIMARY KEY,
    member_id  NVARCHAR(36)  NOT NULL,
    fcm_token  NVARCHAR(256) NOT NULL,
    platform   NVARCHAR(10)  NOT NULL DEFAULT 'ANDROID',
    updated_at NVARCHAR(50)  NOT NULL,
    CONSTRAINT FK_DeviceTokens_Member FOREIGN KEY (member_id) REFERENCES Members(id)
);
CREATE INDEX IX_DeviceTokens_Member ON DeviceTokens(member_id);
