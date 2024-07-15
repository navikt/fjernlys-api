ALTER TABLE risk_report
RENAME COLUMN isOwner TO is_owner;

ALTER TABLE risk_report
RENAME COLUMN ownerIdent TO owner_ident;

ALTER TABLE risk_report
RENAME COLUMN serviceName TO service_name;

ALTER TABLE risk_report
RENAME COLUMN opprettet TO report_created;

ALTER TABLE risk_report
RENAME COLUMN endret TO report_edited;

ALTER TABLE risk_measure
RENAME COLUMN category TO measure_category;

ALTER TABLE risk_measure
RENAME COLUMN status TO measure_status;

ALTER TABLE risk_measure
RENAME COLUMN started TO measure_started;

