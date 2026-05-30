DO $$
BEGIN
	IF EXISTS (
		SELECT 1
		FROM information_schema.tables
		WHERE table_schema = 'public'
			AND table_name = 'animais'
	) THEN
		ALTER TABLE animais ADD COLUMN IF NOT EXISTS sexo VARCHAR(20);
	END IF;
END $$;
