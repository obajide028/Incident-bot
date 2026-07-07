-- ── Incidents table ──────────────────────────────────────────────────────────
-- Stores every incident detected, along with the AI diagnosis and suggested fix.

CREATE TABLE incidents (
                           id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                           service_name     VARCHAR(100) NOT NULL,
                           error_type       VARCHAR(200),
                           error_message    TEXT        NOT NULL,
                           stack_trace      TEXT,
                           environment      VARCHAR(50)  NOT NULL DEFAULT 'production',
                           severity         VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
                           status           VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
                           ai_diagnosis     TEXT,
                           suggested_fix    TEXT,
                           occurred_at      TIMESTAMP    NOT NULL,
                           diagnosed_at     TIMESTAMP,
                           created_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ── Indexes ───────────────────────────────────────────────────────────────────
-- Query incidents by service, status, severity, and time — these are the most
-- common filter patterns for a dashboard or alert system.

CREATE INDEX idx_incidents_service_name ON incidents (service_name);
CREATE INDEX idx_incidents_status       ON incidents (status);
CREATE INDEX idx_incidents_severity     ON incidents (severity);
CREATE INDEX idx_incidents_occurred_at  ON incidents (occurred_at DESC);