-- ============================================
-- POSTGRESQL SCHEMA FOR COUPON MANAGEMENT SYSTEM
-- (Used for Replit deployment)
-- ============================================

-- ============================================
-- SEQUENCES
-- ============================================

CREATE SEQUENCE IF NOT EXISTS campaign_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS coupon_batch_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS coupon_seq START WITH 1 INCREMENT BY 1 CACHE 1000;

-- ============================================
-- TABLES
-- ============================================

CREATE TABLE IF NOT EXISTS campaign (
    id BIGINT DEFAULT nextval('campaign_seq') PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(2000),
    pos_code VARCHAR(50),
    atg_code VARCHAR(50),
    start_date DATE NOT NULL,
    expiry_date DATE NOT NULL CHECK (expiry_date >= start_date),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    active BOOLEAN DEFAULT TRUE NOT NULL
);

CREATE TABLE IF NOT EXISTS coupon_batch (
    id BIGINT DEFAULT nextval('coupon_batch_seq') PRIMARY KEY,
    campaign_id BIGINT NOT NULL REFERENCES campaign(id) ON DELETE CASCADE,
    prefix VARCHAR(6) NOT NULL CHECK (prefix LIKE 'FF%' AND LENGTH(prefix) = 6),
    coupon_count INTEGER NOT NULL CHECK (coupon_count > 0 AND coupon_count <= 3000000),
    max_usages INTEGER DEFAULT 1 NOT NULL CHECK (max_usages > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    active BOOLEAN DEFAULT TRUE NOT NULL
);

CREATE TABLE IF NOT EXISTS coupon (
    id BIGINT DEFAULT nextval('coupon_seq') PRIMARY KEY,
    batch_id BIGINT NOT NULL REFERENCES coupon_batch(id) ON DELETE CASCADE,
    code VARCHAR(14) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE', 'EXPIRED', 'MAX_USED')),
    usage_count INTEGER DEFAULT 0 NOT NULL CHECK (usage_count >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- ============================================
-- INDEXES
-- ============================================

CREATE UNIQUE INDEX IF NOT EXISTS coupon_code_uk ON coupon(code);
CREATE INDEX IF NOT EXISTS coupon_batch_idx ON coupon(batch_id);
CREATE INDEX IF NOT EXISTS coupon_status_idx ON coupon(status);
CREATE INDEX IF NOT EXISTS coupon_created_idx ON coupon(created_at);
CREATE INDEX IF NOT EXISTS coupon_code_prefix_idx ON coupon(SUBSTRING(code, 1, 6));

CREATE INDEX IF NOT EXISTS batch_campaign_idx ON coupon_batch(campaign_id);
CREATE INDEX IF NOT EXISTS batch_active_idx ON coupon_batch(active);

CREATE INDEX IF NOT EXISTS campaign_active_idx ON campaign(active);
CREATE INDEX IF NOT EXISTS campaign_dates_idx ON campaign(start_date, expiry_date);

-- ============================================
-- TRIGGER FUNCTION FOR UPDATED_AT
-- ============================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

DROP TRIGGER IF EXISTS campaign_update_trg ON campaign;
CREATE TRIGGER campaign_update_trg
    BEFORE UPDATE ON campaign
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS batch_update_trg ON coupon_batch;
CREATE TRIGGER batch_update_trg
    BEFORE UPDATE ON coupon_batch
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS coupon_update_trg ON coupon;
CREATE TRIGGER coupon_update_trg
    BEFORE UPDATE ON coupon
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
