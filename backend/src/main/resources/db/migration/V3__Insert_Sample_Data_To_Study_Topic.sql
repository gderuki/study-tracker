INSERT INTO study_topic (title, category, description, is_nice_to_have)
VALUES
    (
        'Code Quality',
        'Implementation',
        'Clean code; DRY, KISS, YAGNI, RTFM, SOLID; API design; library design, modularization and packaging; legacy code refactoring; code quality tooling; technical debt; decomposition; code comments; coupling and cohesion.',
        FALSE
    ),
    (
        'ORM',
        'Implementation',
        'ORM trade-offs and use cases; association mapping; performance tuning: fetch types, native queries, N+1, fetch size, scrolling, stateless sessions, batch inserts and updates; first-level and second-level caching; caching in multi-node setups.',
        FALSE
    ),
    (
        'Microservices',
        'Architecture & Design',
        'Microservices use cases and trade-offs; differences from monolithic architecture and SOA; Saga pattern.',
        TRUE
    );