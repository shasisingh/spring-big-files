CREATE TABLE FILE_POLLER_LEADER_LOCK
(
    LOCK_KEY     CHAR(36)  NOT NULL,
    REGION       VARCHAR(100) NOT NULL,
    CLIENT_ID    CHAR(36),
    CREATED_DATE TIMESTAMP NOT NULL,
    CONSTRAINT PK_INT_LOCK PRIMARY KEY (LOCK_KEY, REGION)
);