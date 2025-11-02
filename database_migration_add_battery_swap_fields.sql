-- Migration script to add new fields for battery swap enhancement
-- Run this script on your database

USE SWP391EVSWAP;
GO

-- Add vehicle_type to vehicle table
ALTER TABLE vehicle ADD vehicle_type NVARCHAR(50) NULL;
GO

-- Update existing vehicles (optional - set default value if needed)
-- UPDATE vehicle SET vehicle_type = 'motorcycle' WHERE vehicle_type IS NULL;
-- GO

-- Add payment fields to reservation table
ALTER TABLE reservation ADD payment_method NVARCHAR(50) NULL;
ALTER TABLE reservation ADD payment_status NVARCHAR(50) NULL;
ALTER TABLE reservation ADD is_instant_swap BIT NULL DEFAULT 0;
GO

-- Add payment tracking to swap_transaction table
ALTER TABLE swap_transaction ADD payment_confirmed_at DATETIMEOFFSET NULL;
ALTER TABLE swap_transaction ADD payment_confirmed_by INT NULL;
GO

-- Add foreign key constraint for payment_confirmed_by
ALTER TABLE swap_transaction ADD CONSTRAINT FK_swap_transaction_payment_confirmed_by 
    FOREIGN KEY (payment_confirmed_by) REFERENCES staff(staff_id);
GO

-- Update existing reservations (optional)
-- UPDATE reservation SET payment_method = 'wallet', payment_status = 'completed', is_instant_swap = 0 
-- WHERE payment_method IS NULL AND payment_status IS NULL;
-- GO

PRINT 'Migration completed successfully!';
GO
