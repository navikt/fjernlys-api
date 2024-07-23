CREATE TABLE risk_level_table (
                                      service_name VARCHAR(36) UNIQUE,
                                      high NUMERIC NOT NULL,
                                      moderate NUMERIC NOT NULL,
                                      low NUMERIC NOT NULL
);

DO
$$
DECLARE
    service_names TEXT[] := ARRAY['All', 'AAP', 'Alderspensjon', 'Dagpenger', 'Foreldrepenger', 'Sykepenger','Uføretrydgd', 'Utbetaling'];
    service TEXT;
BEGIN
    FOREACH service IN ARRAY service_names
    LOOP
        IF NOT EXISTS (SELECT 1 FROM risk_level_table WHERE service_name = service) THEN
            INSERT INTO risk_level_table (service_name, high, moderate, low) VALUES (service, 0, 0, 0);
        END IF;
    END LOOP;
END
$$;
