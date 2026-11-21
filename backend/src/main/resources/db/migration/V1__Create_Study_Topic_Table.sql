create table study_topic
(
    id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    is_nice_to_have BOOLEAN NOT NULL,
    CONSTRAINT pk_study_topic PRIMARY KEY (id)
)