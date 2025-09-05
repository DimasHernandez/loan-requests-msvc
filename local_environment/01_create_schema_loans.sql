CREATE TABLE IF NOT EXISTS public.statuses
(
    status_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(60) NOT NULL UNIQUE,
    description VARCHAR(255)
);

INSERT INTO public.statuses (name, description)
VALUES  ('PENDING_REVIEW', 'The request has been submitted and is waiting to be reviewed automatically or manually.'),
        ('MANUAL_REVIEW', 'The request requires manual evaluation by an analyst or authorized personnel.'),
        ('APPROVED', 'The request has passed all validations and has been successfully approved.'),
        ('REJECTED', 'The request was reviewed and has been declined due to unmet criteria or validations.');

CREATE TABLE IF NOT EXISTS public.loans_types (
    loan_type_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                 VARCHAR(150) NOT NULL UNIQUE,
    amount_min           DECIMAL(10,2),
    amount_max           DECIMAL(10,2),
    term_month_min       INTEGER NOT NULL CHECK (term_month_min > 0),
    term_month_max       INTEGER NOT NULL CHECK (term_month_max > 0),
    interest_rate        DECIMAL(5,4),
    automatic_validation BOOLEAN DEFAULT FALSE
);

INSERT INTO public.loans_types (name, amount_min, amount_max, term_month_min, term_month_max, interest_rate, automatic_validation)
VALUES  ('FREE_INVESTMENT',        500000.00, 99000000.00, 10, 240, 0.0235, FALSE),
        ('PERSONAL_LOAN',      300000.00, 80000000.00, 4, 96, 0.0210, FALSE),
        ('VEHICLE_LOAN',     5000000.00, 60000000.00, 3, 72, 0.0180, TRUE),
        ('PAYROLL_LOAN',   1000000.00, 50000000.00, 2,  60, 0.0150, TRUE),
        ('MICROCREDIT',           20000.00, 5000000.00, 1,24 ,  0.0250, TRUE);

CREATE TABLE IF NOT EXISTS public.loan_applications (
    loan_app_id                             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_number                         VARCHAR(50) NOT NULL,
    amount                                  DECIMAL(10,2) NOT NULL CHECK (amount > 0),
    term_month                              INTEGER NOT NULL CHECK (term_month > 0),
    email                                   VARCHAR(150) NOT NULL,
    total_month_debt_approved_applications  DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    loan_type_id                            UUID NOT NULL,
    status_id                               UUID NOT NULL,

    CONSTRAINT fk_loan_type
    FOREIGN KEY (loan_type_id)
    REFERENCES public.loans_types (loan_type_id),

    CONSTRAINT fk_status
    FOREIGN KEY (status_id)
    REFERENCES public.statuses (status_id)
    );

-- Indexes for faster searching
CREATE INDEX idx_loan_app_document_number ON public.loan_applications (document_number);
CREATE INDEX idx_loan_app_email ON public.loan_applications (email);