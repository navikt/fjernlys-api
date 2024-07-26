-- Drop the existing table if it exists
DROP TABLE IF EXISTS risk_prob_cons_table;

-- Create the table with the correct column names and NOT NULL constraints
CREATE TABLE risk_prob_cons_table (
                                      category_name VARCHAR(36) UNIQUE,
                                      probability NUMERIC NOT NULL,
                                      consequence NUMERIC NOT NULL,
                                      new_probability NUMERIC NOT NULL DEFAULT 0,
                                      new_consequence NUMERIC NOT NULL DEFAULT 0,
                                      total_risk NUMERIC NOT NULL
);

-- Populate the table with default values if the category_name does not exist
DO
$$
DECLARE
category_name_list TEXT[] := ARRAY[
        'Stabil drift og måloppnåelse', 
        'Helse, miljø og sikkerhet',
        'Personvern og informasjonssikkerhet', 
        'Beredskap og samfunnssikkerhet', 
        'Trygdesvindel', 
        'Interne misligheter'
    ];
BEGIN
FOR i IN 1 .. array_length(category_name_list, 1) LOOP
        IF NOT EXISTS (SELECT 1 FROM risk_prob_cons_table WHERE category_name = category_name_list[i]) THEN
            INSERT INTO risk_prob_cons_table (category_name, probability, consequence, new_probability, new_consequence, total_risk)
            VALUES (category_name_list[i], 0, 0, 0, 0, 0);
END IF;
END LOOP;
END
$$;
