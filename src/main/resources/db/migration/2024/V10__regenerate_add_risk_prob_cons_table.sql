-- Drop the existing table if it exists
DROP TABLE IF EXISTS risk_prob_cons_table;

-- Create the table with the correct column names and NOT NULL constraints
CREATE TABLE risk_prob_cons_table (
                                      service_name VARCHAR(36) NOT NULL,
                                      category_name VARCHAR(36) NOT NULL,
                                      probability NUMERIC NOT NULL,
                                      consequence NUMERIC NOT NULL,
                                      new_probability NUMERIC NOT NULL DEFAULT 0,
                                      new_consequence NUMERIC NOT NULL DEFAULT 0,
                                      total_risk NUMERIC NOT NULL,
                                      CONSTRAINT unique_service_category UNIQUE (service_name, category_name)
);

-- Populate the table with default values if the category_name does not exist
DO
$$
DECLARE
category_name_list TEXT[] := ARRAY['Stabil drift og måloppnåelse', 'Helse, miljø og sikkerhet',
                                       'Personvern og informasjonssikkerhet', 'Beredskap og samfunnssikkerhet',
                                       'Trygdesvindel', 'Interne misligheter'];
    service_names TEXT[] := ARRAY['All', 'AAP', 'Alderspensjon', 'Dagpenger', 'Foreldrepenger', 'Sykepenger', 'Uføretrygd', 'Utbetaling'];
BEGIN
FOR service IN 1 .. array_length(service_names, 1) LOOP
        FOR category IN 1 .. array_length(category_name_list, 1) LOOP
            IF NOT EXISTS (SELECT 1 FROM risk_prob_cons_table WHERE service_name = service_names[service] AND category_name = category_name_list[category]) THEN
                INSERT INTO risk_prob_cons_table (service_name, category_name, probability, consequence, new_probability, new_consequence, total_risk)
                VALUES (service_names[service], category_name_list[category], 0, 0, 0, 0, 0);
END IF;
END LOOP;
END LOOP;
END
$$;
