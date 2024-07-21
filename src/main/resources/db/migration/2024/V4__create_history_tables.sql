-- Existing table for risk
CREATE TABLE history_risk_report (
                                     id VARCHAR(36) PRIMARY KEY,
                                     report_id VARCHAR(36) UNIQUE REFERENCES risk_report(id) ON DELETE CASCADE,
                                     is_owner BOOLEAN NOT NULL,
                                     owner_ident VARCHAR(8) NOT NULL,
                                     service_name VARCHAR(50) NOT NULL,
                                     report_created TIMESTAMP DEFAULT current_timestamp,
                                     report_edited TIMESTAMP DEFAULT current_timestamp
);

-- New table for risk assessments linked to risk_report
CREATE TABLE history_risk_assessment (
                                         id VARCHAR(36) PRIMARY KEY,
                                         report_id VARCHAR(36) REFERENCES history_risk_report(report_id) ON DELETE CASCADE,
                                         assessment_id VARCHAR(36) REFERENCES risk_assessment(id) ON DELETE CASCADE,
                                         probability NUMERIC(3, 1) NOT NULL,
                                         consequence NUMERIC(3, 1) NOT NULL,
                                         dependent BOOLEAN NOT NULL,
                                         risk_level VARCHAR(50) NOT NULL,
                                         category VARCHAR(100) NOT NULL,
                                         new_consequence NUMERIC(3, 1),
                                         new_probability NUMERIC(3, 1)
);

-- New table to store measure values associated with risk assessments
CREATE TABLE history_risk_measure (
                                      id VARCHAR(36) PRIMARY KEY,
                                      risk_assessment_id VARCHAR(36) REFERENCES risk_assessment(id) ON DELETE CASCADE,
                                      risk_measure_id VARCHAR(36) REFERENCES risk_measure(id) ON DELETE CASCADE,
                                      category VARCHAR(100) NOT NULL,
                                      status VARCHAR(50) NOT NULL,
                                      started BOOLEAN NOT NULL
);