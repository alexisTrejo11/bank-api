DROP FUNCTION IF EXISTS audit_records_reject_mutation();

CREATE FUNCTION audit_records_reject_mutation()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    RAISE EXCEPTION 'audit_records is append-only';
    RETURN NULL;
END;
$$;

DROP TRIGGER IF EXISTS audit_records_immutable ON audit_records;

CREATE TRIGGER audit_records_immutable
BEFORE UPDATE OR DELETE ON audit_records
FOR EACH ROW
EXECUTE FUNCTION audit_records_reject_mutation();
