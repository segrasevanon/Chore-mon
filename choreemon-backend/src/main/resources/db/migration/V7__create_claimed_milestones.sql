CREATE TABLE ClaimedMilestones (
    member_id     NVARCHAR(36) NOT NULL,
    milestone_key NVARCHAR(64) NOT NULL,
    claimed_at    NVARCHAR(50) NOT NULL,
    PRIMARY KEY (member_id, milestone_key),
    CONSTRAINT FK_Milestones_Member FOREIGN KEY (member_id) REFERENCES Members(id)
);
