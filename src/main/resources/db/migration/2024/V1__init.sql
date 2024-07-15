-- Existing table for risk
CREATE TABLE risk_report (
    id              VARCHAR(36) PRIMARY KEY,
    isOwner         BOOLEAN     NOT NULL,
    ownerIdent      VARCHAR(8)  NOT NULL,
    serviceName     VARCHAR(50) NOT NULL,
    opprettet       TIMESTAMP DEFAULT current_timestamp,
    endret          TIMESTAMP DEFAULT current_timestamp
);

-- New table for risk assessments linked to risk_report
CREATE TABLE risk_assessment (
    id varchar(36) primary key,
    report_id VARCHAR(36) REFERENCES risk_report(id) ON DELETE CASCADE,
    probability NUMERIC(3, 1) NOT NULL,
    consequence NUMERIC(3, 1) NOT NULL,
    dependent BOOLEAN NOT NULL,
    risk_level VARCHAR(50) NOT NULL,
    category VARCHAR(100) NOT NULL,
    new_consequence NUMERIC(3, 1),
    new_probability NUMERIC(3, 1)
);

-- New table to store measure values associated with risk assessments
CREATE TABLE risk_measure (
    id  varchar(36) primary key,
    risk_assessment_id VARCHAR(36) REFERENCES risk_assessment(id) ON DELETE CASCADE,
    category VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    started BOOLEAN NOT NULL
);
