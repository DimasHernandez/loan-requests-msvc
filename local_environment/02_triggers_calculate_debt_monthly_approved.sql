-- TRIGGER INSERT

CREATE OR REPLACE FUNCTION update_total_debt_on_insert()
RETURNS TRIGGER
AS $$
DECLARE
	STATUS_APPROVED CONSTANT VARCHAR := 'APPROVED';
	v_total NUMERIC := 0;
BEGIN

	SELECT COALESCE(SUM(
		(la.amount * lt.interest_rate) / (1 - POWER(1 + lt.interest_rate, - la.term_month))
	), 0)
	INTO v_total
	FROM loan_applications la
	JOIN loans_types lt       ON la.loan_type_id = lt.loan_type_id
	JOIN statuses s     ON la.status_id = s.status_id
	WHERE la.email = NEW.email
	    AND s.name = STATUS_APPROVED;


	UPDATE loan_applications
	SET
			total_month_debt_approved_applications = v_total
	WHERE
			  email = NEW.email;

	RETURN NEW;

END;
$$ LANGUAGE plpgsql;

-- Trigger
CREATE OR REPLACE TRIGGER trg_update_debt_insert
AFTER INSERT ON loan_applications
FOR EACH ROW
EXECUTE FUNCTION update_total_debt_on_insert();

-------------------------------------------------------------------------------------------------------------------------------------------------------------
-- TRIGGER UPDATE

CREATE OR REPLACE FUNCTION update_total_debt_on_update()
RETURNS TRIGGER
AS $$
DECLARE
	STATUS_APPROVED CONSTANT VARCHAR := 'APPROVED';
	v_total                  NUMERIC := 0;
	v_new_status             VARCHAR;
	v_old_status             VARCHAR;
BEGIN

	SELECT name INTO v_new_status FROM statuses WHERE status_id = NEW.status_id;
	SELECT name INTO v_old_status FROM statuses WHERE status_id = OLD.status_id;


	IF (v_new_status = STATUS_APPROVED AND v_old_status != STATUS_APPROVED)
		OR (v_new_status != STATUS_APPROVED AND v_old_status = STATUS_APPROVED)
		OR (v_new_status = STATUS_APPROVED AND (
		NEW.amount != OLD.amount OR
        NEW.term_month != OLD.term_month OR
        NEW.loan_type_id != OLD.loan_type_id
	)) THEN

		-- Calculate the total debt
		SELECT COALESCE(SUM(
			(la.amount * lt.interest_rate) / (1 - POWER(1 + lt.interest_rate, - la.term_month))
		), 0)
		INTO v_total
		FROM loan_applications la
		JOIN loans_types lt       ON la.loan_type_id = lt.loan_type_id
		JOIN statuses s     ON la.status_id = s.status_id
		WHERE la.email = NEW.email
	      	AND s.name = STATUS_APPROVED;


		UPDATE loan_applications
		SET
			total_month_debt_approved_applications = v_total
		WHERE
			  email = NEW.email;
	END IF;

	RETURN NEW;

END;
$$ LANGUAGE plpgsql;

-- Trigger
CREATE OR REPLACE TRIGGER trg_update_debt_update
AFTER UPDATE ON loan_applications
FOR EACH ROW
EXECUTE FUNCTION update_total_debt_on_update();


------ ORIGINAL --------
-- TRIGGER INSERT

-- CREATE OR REPLACE FUNCTION update_total_debt_on_insert()
-- RETURNS TRIGGER
-- AS $$
-- DECLARE
-- STATUS_APPROVED CONSTANT VARCHAR := 'APPROVED';
-- 	v_total NUMERIC := 0;
-- BEGIN
--
-- 	IF (SELECT name FROM statuses WHERE status_id = NEW.status_id) = STATUS_APPROVED THEN
--
-- SELECT COALESCE(SUM(
--                         (la.amount * lt.interest_rate) / (1 - POWER(1 + lt.interest_rate, - la.term_month))
--                 ), 0)
-- INTO v_total
-- FROM loan_applications la
--          JOIN loans_types lt       ON la.loan_type_id = lt.loan_type_id
--          JOIN statuses s     ON la.status_id = s.status_id
-- WHERE la.email = NEW.email
--   AND s.name = STATUS_APPROVED;
--
--
-- UPDATE loan_applications
-- SET
--     total_month_debt_approved_applications = v_total
-- WHERE
--     email = NEW.email;
-- END IF;
--
-- RETURN NEW;
--
-- END;
-- $$ LANGUAGE plpgsql;
--
-- -- Trigger
-- CREATE OR REPLACE TRIGGER trg_update_debt_insert
-- AFTER INSERT ON loan_applications
-- FOR EACH ROW
-- EXECUTE FUNCTION update_total_debt_on_insert();

------------------ TRIGGER INSERT FINISH ------------------

--Version 1.0
-- CREATE OR REPLACE FUNCTION update_user_total_debt()
-- RETURNS TRIGGER

-- AS $$
-- DECLARE
-- 	STATUS_APPROVED CONSTANT VARCHAR := 'APPROVED';
-- 	v_email VARCHAR;
-- 	v_total NUMERIC := 0;

-- BEGIN

-- 	-- Identify user with email
-- 	v_email := NEW.email;

-- 	-- Calculate the total of monthly debt with status loan APPROVED
-- 	SELECT COALESCE(SUM(
-- 		(la.amount * lt.interest_rate) / (1 - POWER(1 + lt.interest_rate, - la.term_month))
-- 	), 0)
-- 	INTO v_total
-- 	FROM loan_applications la
-- 	JOIN loans_types lt       ON la.loan_type_id = lt.loan_type_id
-- 	JOIN statuses s           ON la.status_id = s.status_id
-- 	WHERE la.email = v_email
-- 	      AND s."name" = STATUS_APPROVED;


-- 	-- Update rows to the insert for this user
-- 	NEW.total_month_debt_approved_applications = v_total;

-- 	-- Return all the fields of row
-- 	RETURN NEW;

-- END;
-- $$ LANGUAGE 'plpgsql';

-- -- Create the trigger
-- CREATE OR REPLACE TRIGGER trg_update_total_debt
-- BEFORE INSERT OR UPDATE ON loan_applications
-- FOR EACH ROW
-- EXECUTE FUNCTION update_user_total_debt();