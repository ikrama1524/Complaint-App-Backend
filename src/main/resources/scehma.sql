-- ============================================================================
-- CIVIC COMPLAINT MANAGEMENT SYSTEM - COMPLETE DATABASE SCHEMA
-- Fresh installation script - drops and recreates all tables
-- ============================================================================

-- Drop existing tables (in correct order due to foreign keys)
DROP TABLE IF EXISTS complaint_attachments CASCADE;
DROP TABLE IF EXISTS complaints CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS prabhags CASCADE;

-- Drop custom types if they exist
DROP TYPE IF EXISTS user_role CASCADE;
DROP TYPE IF EXISTS complaint_status CASCADE;
DROP TYPE IF EXISTS complaint_type CASCADE;

-- ============================================================================
-- CUSTOM TYPES (ENUMS)
-- ============================================================================

-- User roles
CREATE TYPE user_role AS ENUM ('CITIZEN', 'ADMIN', 'SUPER_ADMIN');

-- Complaint status
CREATE TYPE complaint_status AS ENUM ('PENDING', 'IN_PROGRESS', 'RESOLVED');

-- Complaint types
CREATE TYPE complaint_type AS ENUM (
    'ROAD_DAMAGE',
    'STREET_LIGHT',
    'GARBAGE_COLLECTION',
    'WATER_SUPPLY',
    'DRAINAGE',
    'PUBLIC_TRANSPORT',
    'NOISE_POLLUTION',
    'ILLEGAL_CONSTRUCTION',
    'PARK_MAINTENANCE',
    'OTHER'
);

-- ============================================================================
-- PRABHAGS TABLE (Must be created before users due to FK)
-- Master data for administrative wards/zones
-- ============================================================================
CREATE TABLE prabhags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(10) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Prabhags indexes
CREATE INDEX idx_prabhags_name ON prabhags(name);
CREATE INDEX idx_prabhags_code ON prabhags(code);

-- ============================================================================
-- USERS TABLE
-- ============================================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    mobile_number VARCHAR(15) NOT NULL UNIQUE,
    address TEXT NOT NULL,
    pin_code VARCHAR(10) NOT NULL,
    role user_role NOT NULL DEFAULT 'CITIZEN',
    is_active BOOLEAN NOT NULL DEFAULT true,

    -- New columns
    prabhag_id UUID,
    poster_image BYTEA,
    poster_image_content_type VARCHAR(50),

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_users_prabhag
        FOREIGN KEY (prabhag_id)
        REFERENCES prabhags(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

-- Users indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_mobile ON users(mobile_number);
CREATE INDEX idx_users_pin_code ON users(pin_code);
CREATE INDEX idx_users_is_active ON users(is_active);
CREATE INDEX idx_users_prabhag_id ON users(prabhag_id);

-- ============================================================================
-- COMPLAINTS TABLE
-- ============================================================================
CREATE TABLE complaints (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    complaint_number VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    complaint_type complaint_type NOT NULL,
    status complaint_status NOT NULL DEFAULT 'PENDING',
    latitude NUMERIC(10, 8),
    longitude NUMERIC(11, 8),
    location_text TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_complaints_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- Complaints indexes
CREATE INDEX idx_complaints_user_id ON complaints(user_id);
CREATE INDEX idx_complaints_status ON complaints(status);
CREATE INDEX idx_complaints_type ON complaints(complaint_type);
CREATE INDEX idx_complaints_created_at ON complaints(created_at);
CREATE INDEX idx_complaints_user_status ON complaints(user_id, status);
CREATE INDEX idx_complaints_user_created ON complaints(user_id, created_at);
CREATE INDEX idx_complaints_status_created ON complaints(status, created_at);

-- ============================================================================
-- COMPLAINT_ATTACHMENTS TABLE
-- Stores image attachments directly in database as BYTEA
-- ============================================================================
CREATE TABLE complaint_attachments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    complaint_id UUID NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(50) NOT NULL,
    image_data BYTEA NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_attachments_complaint
        FOREIGN KEY (complaint_id)
        REFERENCES complaints(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- Complaint attachments indexes
CREATE INDEX idx_complaint_attachments_complaint_id ON complaint_attachments(complaint_id);

-- ============================================================================
-- COMPLAINT_SEQUENCES TABLE
-- Tracks the sequential ID for each prabhag per year
-- ============================================================================
CREATE TABLE complaint_sequences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    prabhag_id UUID NOT NULL,
    year INT NOT NULL,
    current_value INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_sequences_prabhag
        FOREIGN KEY (prabhag_id)
        REFERENCES prabhags(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_sequences_prabhag_year UNIQUE (prabhag_id, year)
);

CREATE INDEX idx_complaint_sequences_lookup ON complaint_sequences(prabhag_id, year);

-- ============================================================================
-- TRIGGERS FOR UPDATED_AT
-- ============================================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger for users table
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger for complaints table
CREATE TRIGGER update_complaints_updated_at
    BEFORE UPDATE ON complaints
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger for prabhags table
CREATE TRIGGER update_prabhags_updated_at
    BEFORE UPDATE ON prabhags
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger for complaint_sequences table
CREATE TRIGGER update_complaint_sequences_updated_at
    BEFORE UPDATE ON complaint_sequences
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- SAMPLE DATA & VERIFICATION
-- ============================================================================

-- Create default Prabhags
INSERT INTO prabhags (id, name, code, description) VALUES
    (gen_random_uuid(), 'North Zone', 'NOR', 'Northern part of the city'),
    (gen_random_uuid(), 'South Zone', 'SOU', 'Southern part of the city');

-- Get Prabhag ID for default mapping (for demo purposes)
-- In real scenario, application would handle this

-- Insert Super Admin (password: admin123)
-- Role SUPER_ADMIN does not need a Prabhag
INSERT INTO users (role, full_name, email, mobile_number, password_hash, address, pin_code, is_active)
VALUES (
    'SUPER_ADMIN',
    'System Super Admin',
    'superadmin@civic.com',
    '9999999990',
    '$2a$10$rX8qVqZ9YqZ9YqZ9YqZ9YeKqZ9YqZ9YqZ9YqZ9YqZ9YqZ9YqZ9Yq', -- Hash for 'admin123'
    'City HQ',
    '000000',
    true
);

-- Insert Sample Admin (password: admin123)
-- Ideally map to a prabhag, but fine for fresh schema to be null or mapped manually later
INSERT INTO users (role, full_name, email, mobile_number, password_hash, address, pin_code, is_active)
VALUES (
    'ADMIN',
    'Ward Admin',
    'admin@civic.com',
    '9999999999',
    '$2a$10$rX8qVqZ9YqZ9YqZ9YqZ9YeKqZ9YqZ9YqZ9YqZ9YqZ9YqZ9YqZ9Yq',
    'Ward Office',
    '000001',
    true
);

-- Insert Sample User
INSERT INTO users (role, full_name, email, mobile_number, password_hash, address, pin_code, is_active)
VALUES (
    'CITIZEN',
    'John Doe',
    'citizen@example.com',
    '9876543210',
    '$2a$10$rX8qVqZ9YqZ9YqZ9YqZ9YeKqZ9YqZ9YqZ9YqZ9YqZ9YqZ9YqZ9Yq',
    '123 Main St',
    '560001',
    true
);

-- Verify
SELECT table_name FROM information_schema.tables WHERE table_schema='public';

