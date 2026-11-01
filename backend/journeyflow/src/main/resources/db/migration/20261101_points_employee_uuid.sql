-- Run once against the existing database before restarting the application.
-- Legacy bigint references remain untouched; new records use employee_uuid.
ALTER TABLE public.points_log
    ADD COLUMN IF NOT EXISTS employee_uuid UUID;

DO $$
DECLARE
    legacy_column TEXT;
BEGIN
    FOREACH legacy_column IN ARRAY ARRAY[
        'user_id',
        'employee_id',
        'sender',
        'content',
        'source',
        'author',
        'date_sent',
        'embedding'
    ]
    LOOP
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'points_log'
              AND column_name = legacy_column
              AND is_nullable = 'NO'
        ) THEN
            EXECUTE format(
                'ALTER TABLE public.points_log ALTER COLUMN %I DROP NOT NULL',
                legacy_column
            );
        END IF;
    END LOOP;
END;
$$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_points_log_employee_uuid'
          AND conrelid = 'public.points_log'::regclass
    ) THEN
        ALTER TABLE public.points_log
            ADD CONSTRAINT fk_points_log_employee_uuid
            FOREIGN KEY (employee_uuid) REFERENCES public.employee(id)
            NOT VALID;
    END IF;
END;
$$;

CREATE INDEX IF NOT EXISTS idx_points_log_employee_uuid
    ON public.points_log (employee_uuid)
    WHERE employee_uuid IS NOT NULL;
