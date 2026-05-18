INSERT INTO outbox_event (
    key,
    topic,
    event_body,
    status
)
VALUES (
    999,
    'link-updates',
    '{
      "id": 999,
      "url": "https://github.com/test/repo",
      "description": "kafka e2e test",
      "tgChatIds": [1, 2, 3],
      "resourceType": "GITHUB_REPOSITORY",
      "lastUpdate": "2025-01-01T12:00:00Z"
    }'::jsonb,
    'PENDING'
);
