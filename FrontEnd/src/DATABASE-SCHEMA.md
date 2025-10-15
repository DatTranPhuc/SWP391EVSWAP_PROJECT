# EV SWAP Database Schema

## Overview
Hệ thống quản lý đổi pin xe máy điện với SQL Server database schema.

## Tables

### 1. admin
Quản lý tài khoản admin

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| admin_id | int identity | PRIMARY KEY | ID tự động tăng |
| email | varchar(255) | NOT NULL, UNIQUE | Email đăng nhập |
| full_name | varchar(255) | | Họ và tên |
| password_hash | varchar(255) | NOT NULL | Mật khẩu đã hash |
| created_at | datetimeoffset(6) | | Ngày tạo |

### 2. driver
Quản lý tài khoản tài xế

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| driver_id | int identity | PRIMARY KEY | ID tự động tăng |
| email | varchar(255) | NOT NULL, UNIQUE | Email đăng nhập |
| full_name | varchar(255) | NOT NULL | Họ và tên |
| password_hash | varchar(255) | NOT NULL | Mật khẩu đã hash |
| phone | varchar(255) | UNIQUE (nullable) | Số điện thoại |
| email_verified | bit | | Đã xác thực email |
| email_otp | varchar(255) | | Mã OTP qua email |
| otp_expiry | datetimeoffset(6) | | Thời gian hết hạn OTP |
| created_at | datetimeoffset(6) | | Ngày tạo |

### 3. staff
Quản lý tài khoản nhân viên

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| staff_id | int identity | PRIMARY KEY | ID tự động tăng |
| email | varchar(255) | UNIQUE (nullable) | Email đăng nhập |
| full_name | varchar(255) | | Họ và tên |
| password_hash | varchar(255) | | Mật khẩu đã hash |
| is_active | bit | | Trạng thái hoạt động |
| station_id | int | FK → station | Trạm được gán |

### 4. station
Quản lý trạm đổi pin

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| station_id | int identity | PRIMARY KEY | ID tự động tăng |
| name | varchar(255) | NOT NULL | Tên trạm |
| address | varchar(255) | | Địa chỉ |
| latitude | numeric(38,2) | | Vĩ độ |
| longitude | numeric(38,2) | | Kinh độ |
| status | varchar(255) | | Trạng thái (active/inactive) |

### 5. battery
Quản lý pin

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| battery_id | int identity | PRIMARY KEY | ID tự động tăng |
| model | varchar(255) | NOT NULL | Model pin |
| soc_percent | int | | State of Charge (%) |
| soh_percent | int | | State of Health (%) |
| state | varchar(255) | | Trạng thái pin |
| station_id | int | NOT NULL, FK → station | Trạm chứa pin |

### 6. vehicle
Quản lý xe máy điện

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| vehicle_id | int identity | PRIMARY KEY | ID tự động tăng |
| model | varchar(255) | | Model xe |
| plate_number | varchar(255) | | Biển số xe |
| vin | varchar(255) | NOT NULL, UNIQUE | Số VIN (số khung) |
| driver_id | int | NOT NULL, FK → driver | Chủ sở hữu |
| created_at | datetimeoffset(6) | | Ngày đăng ký |

### 7. reservation
Quản lý đặt chỗ đổi pin

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| reservation_id | int identity | PRIMARY KEY | ID tự động tăng |
| driver_id | int | NOT NULL, FK → driver | Tài xế đặt |
| station_id | int | NOT NULL, FK → station | Trạm đặt |
| reserved_start | datetimeoffset(6) | | Thời gian bắt đầu |
| status | varchar(255) | | Trạng thái đặt chỗ |
| qr_token | varchar(255) | | Mã QR |
| qr_status | varchar(255) | | Trạng thái QR |
| qr_nonce | varchar(255) | | Nonce bảo mật |
| qr_expires_at | datetimeoffset(6) | | Hết hạn QR |
| checked_in_at | datetimeoffset(6) | | Thời gian check-in |
| created_at | datetimeoffset(6) | | Ngày tạo |

### 8. swap_transaction
Giao dịch đổi pin

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| swap_id | int identity | PRIMARY KEY | ID tự động tăng |
| reservation_id | int | NOT NULL, UNIQUE, FK → reservation | Đơn đặt liên kết |
| station_id | int | NOT NULL, FK → station | Trạm đổi |
| battery_in_id | int | FK → battery | Pin cũ thu vào |
| battery_out_id | int | FK → battery | Pin mới đưa ra |
| swapped_at | datetimeoffset(6) | | Thời gian đổi |
| result | varchar(255) | | Kết quả giao dịch |

### 9. payment
Quản lý thanh toán

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| payment_id | int identity | PRIMARY KEY | ID tự động tăng |
| driver_id | int | NOT NULL, FK → driver | Tài xế thanh toán |
| reservation_id | int | FK → reservation | Đơn đặt liên kết |
| amount | numeric(12,2) | NOT NULL | Số tiền |
| currency | varchar(255) | | Loại tiền tệ |
| method | varchar(255) | | Phương thức |
| status | varchar(255) | | Trạng thái |
| paid_at | datetimeoffset(6) | | Thời gian thanh toán |
| provider_txn_id | varchar(255) | | ID giao dịch từ provider |

### 10. feedback
Đánh giá từ tài xế

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| feedback_id | int identity | PRIMARY KEY | ID tự động tăng |
| driver_id | int | NOT NULL, FK → driver | Tài xế đánh giá |
| station_id | int | NOT NULL, FK → station | Trạm được đánh giá |
| rating | int | | Điểm số (1-5) |
| comment | text | | Nhận xét |
| created_at | datetimeoffset(6) | | Ngày tạo |

### 11. ticket_support
Yêu cầu hỗ trợ

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| ticket_id | int identity | PRIMARY KEY | ID tự động tăng |
| driver_id | int | NOT NULL, FK → driver | Tài xế yêu cầu |
| staff_id | int | FK → staff | Staff xử lý |
| category | varchar(255) | | Danh mục |
| comment | text | | Nội dung yêu cầu |
| note | text | | Ghi chú xử lý |
| status | varchar(255) | | Trạng thái |
| created_at | datetimeoffset(6) | | Ngày tạo |
| resolved_at | datetimeoffset(6) | | Ngày giải quyết |

### 12. notification
Thông báo cho tài xế

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| noti_id | int identity | PRIMARY KEY | ID tự động tăng |
| driver_id | int | NOT NULL, FK → driver | Tài xế nhận |
| type | varchar(255) | | Loại thông báo |
| title | varchar(255) | NOT NULL | Tiêu đề |
| is_read | bit | | Đã đọc |
| sent_at | datetimeoffset(6) | | Thời gian gửi |
| payment_id | int | FK → payment | Liên kết payment |
| reservation_id | int | FK → reservation | Liên kết reservation |

### 13. vehicle_battery_compatibility
Quan hệ nhiều-nhiều: xe tương thích với pin

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| vehicle_id | int | PRIMARY KEY (composite), FK → vehicle | ID xe |
| battery_id | int | PRIMARY KEY (composite), FK → battery | ID pin |

## Field Name Changes (So với KV Store cũ)

| Old Name | New Name | Note |
|----------|----------|------|
| license_plate | plate_number | Tên biển số xe |
| name | full_name | Họ tên đầy đủ |
| - | driver_id, admin_id, staff_id | ID theo từng table |
| timestamp | created_at, swapped_at, etc. | Tên cụ thể hơn |

## Important Notes

1. **Identity Columns**: Tất cả các `_id` fields đều dùng `int identity` (auto-increment)
2. **Datetime**: Sử dụng `datetimeoffset(6)` cho timestamp với timezone
3. **Unique Constraints**: 
   - Email của driver, admin phải unique
   - Phone của driver có unique index (nullable)
   - VIN của vehicle phải unique
4. **Foreign Keys**: Đã thiết lập đầy đủ các mối quan hệ giữa tables
5. **Text Fields**: Sử dụng `text` cho comment/description dài

## API Mapping

Frontend cần update các field names khi gọi API:
- `license_plate` → `plate_number`
- `name` → `full_name`
- Các ID fields theo convention mới

## Current Implementation

Hiện tại backend đang sử dụng KV Store (Supabase Auth + KV) thay vì SQL Server trực tiếp.
Schema này phục vụ làm tài liệu reference cho migration trong tương lai.
