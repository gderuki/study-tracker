CREATE UNIQUE INDEX ux_study_session_single_started
    ON study_session (status)
    WHERE status = 'STARTED';