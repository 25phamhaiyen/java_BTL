-- Drop tables in correct order to avoid foreign key constraints
DROP TABLE IF EXISTS account_permission;
DROP TABLE IF EXISTS booking_detail;
DROP TABLE IF EXISTS order_detail;
DROP TABLE IF EXISTS invoice;
DROP TABLE IF EXISTS `order`;
DROP TABLE IF EXISTS booking;
DROP TABLE IF EXISTS pet;
DROP TABLE IF EXISTS work_schedule;
DROP TABLE IF EXISTS staff;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS account;
-- Drop parent or independent tables
DROP TABLE IF EXISTS pet_type;
DROP TABLE IF EXISTS service;
DROP TABLE IF EXISTS promotion;
DROP TABLE IF EXISTS permission;
DROP TABLE IF EXISTS `role`;
DROP TABLE IF EXISTS person;

-- Ensure we're using the correct database
USE bestpets;

-- Insert into `role`
INSERT INTO `role` (role_name) VALUES 
('ADMIN'), 
('STAFF_CARE'), 
('STAFF_CASHIER'), 
('STAFF_RECEPTION'),
('OUT');

-- Insert into person
INSERT INTO person (person_id, full_name, gender, phone, address, email) VALUES
(1, 'Admin Boss', 'OTHER', '0900000000', '123 Admin St', 'admin@bestpets.com'),
(2, 'Nguyễn Văn A', 'MALE', '0900000001', '123 Pet St', 'care01@pets.com'),
(3, 'Trần Thị B', 'FEMALE', '0900000002', '456 Pet St', 'care02@pets.com'),
(4, 'Lê Văn C', 'MALE', '0900000003', '789 Pet St', 'care03@pets.com'),
(5, 'Phạm Thị D', 'FEMALE', '0900000004', '135 Pet St', 'care04@pets.com'),
(6, 'Ngô Văn E', 'MALE', '0900000005', '246 Pet St', 'care05@pets.com'),
(7, 'Đỗ Thị F', 'FEMALE', '0900000006', '357 Pet St', 'care06@pets.com'),
(8, 'Vũ Văn G', 'MALE', '0900000007', '468 Pet St', 'care07@pets.com'),
(9, 'Nguyễn Thị H', 'FEMALE', '0900000008', '579 Pet St', 'cashier01@pets.com'),
(10, 'Trần Văn I', 'MALE', '0900000009', '680 Pet St', 'cashier02@pets.com'),
(11, 'Hoàng Thị J', 'FEMALE', '0900000010', '791 Pet St', 'reception01@pets.com'),
(12, 'Phan Văn K', 'MALE', '0900000011', '802 Pet St', 'reception02@pets.com'),
(13, 'Customer 01', 'MALE', '0910000001', 'CT1, City A', 'cus01@mail.com'),
(14, 'Customer 02', 'FEMALE', '0910000002', 'CT2, City A', 'cus02@mail.com'),
(15, 'Customer 03', 'MALE', '0910000003', 'CT3, City A', 'cus03@mail.com'),
(16, 'Customer 04', 'FEMALE', '0910000004', 'CT4, City A', 'cus04@mail.com'),
(17, 'Customer 05', 'MALE', '0910000005', 'CT5, City A', 'cus05@mail.com'),
(18, 'Customer 06', 'FEMALE', '0910000006', 'CT6, City A', 'cus06@mail.com'),
(19, 'Customer 07', 'MALE', '0910000007', 'CT7, City A', 'cus07@mail.com'),
(20, 'Customer 08', 'FEMALE', '0910000008', 'CT8, City A', 'cus08@mail.com'),
(21, 'Customer 09', 'MALE', '0910000009', 'CT9, City A', 'cus09@mail.com'),
(22, 'Customer 10', 'FEMALE', '0910000010', 'CT10, City A', 'cus10@mail.com');

-- Insert into account
INSERT INTO `account` (account_id, username, `password`, role_id) VALUES
(1, 'admin01', '$2a$10$bxB8Fa1fAwS7BZoplZQCQOyc6CQEMDixqUWZ1e6O/6QJRUVW5mYlG', 1),
(2, 'care01', '$2a$10$PjIlPrb1fRBqqqCHvtqeseCYKlJo2XX1ETPKseRMGnEwCuSIAQpcu', 2),
(3, 'care02', '$2a$10$PjIlPrb1fRBqqqCHvtqeseCYKlJo2XX1ETPKseRMGnEwCuSIAQpcu', 2),
(4, 'care03', '$2a$10$PjIlPrb1fRBqqqCHvtqeseCYKlJo2XX1ETPKseRMGnEwCuSIAQpcu', 2),
(5, 'care04', '$2a$10$PjIlPrb1fRBqqqCHvtqeseCYKlJo2XX1ETPKseRMGnEwCuSIAQpcu', 2),
(6, 'care05', '$2a$10$PjIlPrb1fRBqqqCHvtqeseCYKlJo2XX1ETPKseRMGnEwCuSIAQpcu', 2),
(7, 'care06', '$2a$10$PjIlPrb1fRBqqqCHvtqeseCYKlJo2XX1ETPKseRMGnEwCuSIAQpcu', 2),
(8, 'care07', '$2a$10$PjIlPrb1fRBqqqCHvtqeseCYKlJo2XX1ETPKseRMGnEwCuSIAQpcu', 2),
(9, 'cashier01', '$2a$10$PjIlPrb1fRBqqqCHvtqeseCYKlJo2XX1ETPKseRMGnEwCuSIAQpcu', 3),
(10, 'cashier02', '$2a$10$PjIlPrb1fRBqqqCHvtqeseCYKlJo2XX1ETPKseRMGnEwCuSIAQpcu', 3),
(11, 'reception01', '$2a$10$PjIlPrb1fRBqqqCHvtqeseCYKlJo2XX1ETPKseRMGnEwCuSIAQpcu', 4),
(12, 'reception02', '$2a$10$PjIlPrb1fRBqqqCHvtqeseCYKlJo2XX1ETPKseRMGnEwCuSIAQpcu', 4),
(13, 'reception03', '$2a$10$PjIlPrb1fRBqqqCHvtqeseCYKlJo2XX1ETPKseRMGnEwCuSIAQpcu', 4);

-- Insert into permission
INSERT INTO permission (permission_code, description) VALUES
('CREATE_BOOKING', 'Đặt lịch hẹn cho khách'),
('MANAGE_PAYMENT', 'Thực hiện thanh toán, áp dụng KM, ghi nhận hóa đơn'),
('VIEW_CUSTOMER', 'Xem thông tin khách hàng, lịch sử booking'),
('PRINT_RECEIPT', 'In hóa đơn hoặc phiếu thanh toán'),
('APPLY_PROMOTION', 'Áp dụng chương trình khuyến mãi'),
('VIEW_INVOICE', 'Xem danh sách hóa đơn đã lập'),
('VIEW_BOOKING_ASSIGNED', 'Xem các lịch hẹn được phân công (Groomer)'),
('MARK_SERVICE_DONE', 'Đánh dấu đã hoàn thành dịch vụ chăm sóc'),
('UPDATE_PET_STATUS', 'Cập nhật tình trạng thú cưng'),
('VIEW_SCHEDULE', 'Xem lịch làm việc cá nhân'),
('MANAGE_ACCOUNT', 'Thêm, sửa, xoá tài khoản nhân viên'),
('ASSIGN_PERMISSION', 'Gán quyền cho tài khoản STAFF'),
('MANAGE_SCHEDULE', 'Lên lịch làm việc cho nhân viên'),
('ADD_EMPLOYEE', 'Thêm nhân viên mới vào hệ thống'),
('MANAGE_SERVICE', 'Thêm, sửa, xoá dịch vụ trong cửa hàng'),
('VIEW_FINANCE', 'Xem báo cáo tài chính, doanh thu, chi phí'),
('VIEW_DASHBOARD', 'Xem tổng quan số liệu cửa hàng'),
('UPDATE_PROFILE', 'Cập nhật thông tin tài khoản cá nhân'),
('APPROVE_LEAVE', 'Phê duyệt yêu cầu nghỉ phép'),
('APPROVE_SHIFT_CHANGE', 'Phê duyệt yêu cầu đổi ca'),
('REGISTER_SHIFT', 'Đăng ký ca làm việc'),
('REQUEST_LEAVE', 'Yêu cầu nghỉ phép');

-- Insert into staff
INSERT INTO staff (staff_id, dob, salary, hire_date, account_id, role_id) VALUES
(1, '1990-01-01', 20000000, '2023-01-01', 1, 1),
(2, '1991-02-01', 10000000, '2024-01-01', 2, 2),
(3, '1992-03-01', 10000000, '2024-01-01', 3, 2),
(4, '1993-04-01', 10000000, '2024-01-01', 4, 2),
(5, '1994-05-01', 10000000, '2024-01-01', 5, 2),
(6, '1995-06-01', 10000000, '2024-01-01', 6, 2),
(7, '1996-07-01', 10000000, '2024-01-01', 7, 2),
(8, '1997-08-01', 10000000, '2024-01-01', 8, 2),
(9, '1991-09-01', 8000000, '2025-01-01', 9, 3),
(10, '1992-10-01', 8000000, '2025-01-01', 10, 3),
(11, '1993-11-01', 9000000, '2025-01-01', 11, 4),
(12, '1994-12-01', 9000000, '2025-01-01', 12, 4);

-- Insert into customer
INSERT INTO customer (customer_id, `point`) VALUES
(13, 100),
(14, 200),
(15, 150),
(16, 250),
(17, 300),
(18, 120),
(19, 220),
(20, 80),
(21, 90),
(22, 60);

-- Insert into pet_type
INSERT INTO pet_type (type_id, species, breed) VALUES
(1, 'Dog', 'Poodle'),
(2, 'Dog', 'Shiba'),
(3, 'Cat', 'Mèo Anh Lông Ngắn'),
(4, 'Cat', 'Mèo Ba Tư'),
(5, 'Hamster', 'Hamster Bear');

-- Insert into pet
INSERT INTO pet (name, pet_gender, dob, customer_id, type_id, weight) VALUES
('Milo', 'MALE', '2022-01-01', 22, 1, 5.2),
('Luna', 'FEMALE', '2021-03-12', 22, 3, 4.0),
('Coco', 'MALE', '2020-06-15', 21, 2, 6.1),
('Daisy', 'FEMALE', '2019-08-20', 21, 4, 3.5),
('Max', 'MALE', '2022-11-11', 13, 1, 5.8),
('Bella', 'FEMALE', '2021-09-09', 13, 3, 4.1),
('Rocky', 'MALE', '2023-01-10', 14, 2, 7.0),
('Lily', 'FEMALE', '2022-12-22', 14, 4, 3.9),
('Nemo', 'MALE', '2023-02-14', 15, 5, 0.3),
('Mimi', 'FEMALE', '2021-05-30', 15, 3, 4.2),
('Chó Mập', 'MALE', '2020-04-01', 16, 1, 10.5),
('Mèo Mun', 'FEMALE', '2021-07-17', 16, 4, 3.3),
('Susu', 'FEMALE', '2022-10-10', 17, 5, 0.2),
('Bunbun', 'MALE', '2020-03-03', 17, 5, 0.4),
('Dora', 'FEMALE', '2021-01-25', 18, 2, 6.7),
('Kitty', 'FEMALE', '2022-08-08', 18, 3, 3.8),
('Chibi', 'MALE', '2019-09-09', 19, 1, 5.0),
('Toto', 'MALE', '2023-03-03', 19, 5, 0.5),
('Mun', 'FEMALE', '2021-04-04', 20, 3, 4.3),
('Misa', 'FEMALE', '2022-06-06', 20, 4, 3.2);

-- Insert into service
INSERT INTO service (service_id, name, description, price, duration_minutes, active) VALUES
(1, 'Tắm thú cưng', 'Tắm, sấy khô và chải lông cho thú cưng', 100000, 30, true),
(2, 'Cắt tỉa lông', 'Cắt tỉa tạo kiểu theo yêu cầu', 150000, 45, true),
(3, 'Khám sức khỏe', 'Kiểm tra tổng quát sức khỏe thú cưng', 200000, 30, true),
(4, 'Tiêm phòng', 'Tiêm các loại vaccine cơ bản', 250000, 15, true),
(5, 'Gửi trông thú cưng', 'Dịch vụ giữ thú cưng theo giờ', 50000, 60, true),
(6, 'Huấn luyện cơ bản', 'Dạy ngồi, đứng, bắt tay,...', 300000, 60, false);

-- Insert into promotion
INSERT INTO promotion (promotion_id, code, description, discount_percent, start_date, end_date, active) VALUES
(1, 'KM1', 'Giảm 10% cho tất cả các dịch vụ', 10, '2025-05-06', '2025-07-17', true),
(2, 'KM2', 'Chỉ áp dụng cho khách hàng lần đầu sử dụng', 20, '2025-05-06', '2025-07-17', true),
(3, 'KM3', 'Giảm 15% khi sử dụng combo 2 dịch vụ', 15, '2025-05-06', '2025-07-17', false),
(4, 'KM4', 'Giảm 5% toàn bộ đơn hàng >300k', 5, '2025-05-06', '2025-07-17', true),
(5, 'KM5', 'Giảm 25% cho đơn hàng từ 500k trở lên', 25, '2025-05-07', '2025-05-21', true),
(6, 'KM6', 'Giảm 30% cho khách hàng VIP', 30, '2025-05-12', '2025-07-11', true),
(7, 'KM7', 'Giảm 10% khi đặt lịch trước 3 ngày', 10, '2025-05-17', '2025-07-16', true),
(8, 'KM8', 'Giảm 20% cho dịch vụ thứ 3 trong combo', 20, '2025-05-06', '2025-07-02', false),
(9, 'KM9', 'Giảm 15% cho đơn hàng cuối tuần', 15, '2025-06-03', '2025-07-02', true);

-- Insert into booking (12–16/05/2025)
INSERT INTO booking (booking_id, customer_id, pet_id, staff_id, booking_time, status, note) VALUES
(71, 13, 5, 2, '2025-05-12 08:30:00', 'PENDING', 'Tắm sạch lông'),
(72, 14, 7, 3, '2025-05-12 09:00:00', 'CONFIRMED', NULL),
(73, 15, 9, 4, '2025-05-12 10:00:00', 'COMPLETED', 'Khám kỹ'),
(74, 16, 11, 5, '2025-05-12 11:00:00', 'PENDING', NULL),
(75, 17, 13, 6, '2025-05-12 13:30:00', 'CONFIRMED', 'Cắt lông gọn'),
(76, 18, 15, 7, '2025-05-12 14:00:00', 'CANCELLED', 'Khách hủy'),
(77, 19, 17, 8, '2025-05-12 15:00:00', 'PENDING', NULL),
(78, 20, 19, 2, '2025-05-12 16:00:00', 'COMPLETED', 'Tiêm vaccine'),
(79, 21, 1, 3, '2025-05-12 17:30:00', 'CONFIRMED', NULL),
(80, 22, 2, 4, '2025-05-12 18:00:00', 'PENDING', 'Giữ lâu dài'),
(81, 13, 6, 5, '2025-05-12 19:00:00', 'COMPLETED', NULL),
(82, 14, 8, 6, '2025-05-12 20:00:00', 'CONFIRMED', NULL),
(83, 15, 10, 7, '2025-05-13 08:30:00', 'PENDING', NULL),
(84, 16, 12, 8, '2025-05-13 09:00:00', 'CONFIRMED', 'Tắm nhẹ nhàng'),
(85, 17, 14, 2, '2025-05-13 10:00:00', 'COMPLETED', NULL),
(86, 18, 16, 3, '2025-05-13 11:00:00', 'PENDING', 'Cắt lông đẹp'),
(90, 22, 4, 7, '2025-05-13 16:00:00', 'COMPLETED', NULL),
(91, 13, 5, 8, '2025-05-13 17:30:00', 'CONFIRMED', 'Khám sức khỏe'),
(92, 14, 7, 2, '2025-05-13 18:00:00', 'PENDING', NULL),
(93, 15, 9, 3, '2025-05-13 19:00:00', 'COMPLETED', NULL),
(94, 16, 11, 4, '2025-05-13 20:00:00', 'CONFIRMED', NULL),
(95, 17, 13, 5, '2025-05-14 08:30:00', 'PENDING', 'Tắm sạch'),
(96, 18, 15, 6, '2025-05-14 09:00:00', 'CONFIRMED', NULL),
(97, 19, 17, 7, '2025-05-14 10:00:00', 'COMPLETED', NULL),
(98, 20, 19, 8, '2025-05-14 11:00:00', 'PENDING', 'Cắt tỉa lông'),
(99, 21, 1, 2, '2025-05-14 13:30:00', 'CONFIRMED', NULL),
(100, 22, 2, 3, '2025-05-14 14:00:00', 'CANCELLED', NULL),
(101, 13, 6, 4, '2025-05-14 15:00:00', 'PENDING', NULL),
(102, 14, 8, 5, '2025-05-14 16:00:00', 'COMPLETED', 'Tiêm phòng'),
(103, 15, 10, 6, '2025-05-14 17:30:00', 'CONFIRMED', NULL),
(104, 16, 12, 7, '2025-05-14 18:00:00', 'PENDING', NULL),
(105, 17, 14, 8, '2025-05-14 19:00:00', 'COMPLETED', NULL),
(106, 18, 16, 2, '2025-05-14 20:00:00', 'CONFIRMED', 'Giữ thú cưng'),
(107, 19, 18, 3, '2025-05-15 08:30:00', 'PENDING', NULL),
(108, 20, 20, 4, '2025-05-15 09:00:00', 'CONFIRMED', 'Tắm sạch'),
(109, 21, 3, 5, '2025-05-15 10:00:00', 'COMPLETED', NULL),
(110, 22, 4, 6, '2025-05-15 11:00:00', 'PENDING', NULL),
(111, 13, 5, 7, '2025-05-15 13:30:00', 'CONFIRMED', 'Cắt lông'),
(112, 14, 7, 8, '2025-05-15 14:00:00', 'CANCELLED', 'Khách hủy'),
(113, 15, 9, 2, '2025-05-15 15:00:00', 'PENDING', NULL),
(114, 16, 11, 3, '2025-05-15 16:00:00', 'COMPLETED', NULL),
(115, 17, 13, 4, '2025-05-15 17:30:00', 'CONFIRMED', 'Khám sức khỏe'),
(116, 18, 15, 5, '2025-05-15 18:00:00', 'PENDING', NULL),
(117, 19, 17, 6, '2025-05-15 19:00:00', 'COMPLETED', NULL),
(118, 20, 19, 7, '2025-05-15 20:00:00', 'CONFIRMED', NULL),
(119, 21, 1, 8, '2025-05-16 08:30:00', 'PENDING', 'Tắm sạch'),
(120, 22, 2, 2, '2025-05-16 09:00:00', 'CONFIRMED', NULL),
(121, 13, 6, 3, '2025-05-16 10:00:00', 'COMPLETED', NULL),
(122, 14, 8, 4, '2025-05-16 11:00:00', 'PENDING', 'Cắt tỉa lông'),
(123, 15, 10, 5, '2025-05-16 13:30:00', 'CONFIRMED', NULL),
(124, 16, 12, 6, '2025-05-16 14:00:00', 'CANCELLED', NULL),
(125, 17, 14, 7, '2025-05-16 15:00:00', 'PENDING', NULL),
(126, 18, 16, 8, '2025-05-16 16:00:00', 'COMPLETED', 'Tiêm phòng'),
(127, 19, 18, 2, '2025-05-16 17:30:00', 'CONFIRMED', NULL),
(128, 20, 20, 3, '2025-05-16 18:00:00', 'PENDING', NULL),
(129, 21, 3, 4, '2025-05-16 19:00:00', 'COMPLETED', NULL),
(130, 22, 4, 5, '2025-05-16 20:00:00', 'CONFIRMED', 'Giữ thú cưng');

-- Insert into booking_detail
INSERT INTO booking_detail (booking_id, service_id, quantity, price) VALUES
(71, 1, 1, 100000), (71, 2, 1, 150000),
(72, 3, 1, 200000),
(73, 4, 1, 250000), (73, 1, 1, 100000),
(74, 2, 1, 150000),
(75, 5, 2, 50000),
(76, 1, 1, 100000),
(77, 2, 1, 150000), (77, 3, 1, 200000),
(78, 4, 1, 250000),
(79, 1, 1, 100000),
(80, 5, 3, 50000),
(81, 2, 1, 150000), (81, 4, 1, 250000),
(82, 3, 1, 200000),
(83, 1, 1, 100000),
(84, 2, 1, 150000), (84, 1, 1, 100000),
(85, 5, 1, 50000),
(86, 1, 1, 100000),
(90, 5, 2, 50000),
(91, 1, 1, 100000), (91, 2, 1, 150000),
(92, 4, 1, 250000),
(93, 3, 1, 200000), (93, 1, 1, 100000),
(94, 2, 1, 150000),
(95, 5, 2, 50000),
(96, 1, 1, 100000),
(97, 2, 1, 150000), (97, 3, 1, 200000),
(98, 4, 1, 250000),
(99, 1, 1, 100000),
(100, 5, 3, 50000),
(101, 2, 1, 150000), (101, 4, 1, 250000),
(102, 3, 1, 200000),
(103, 1, 1, 100000),
(104, 2, 1, 150000), (104, 1, 1, 100000),
(105, 5, 1, 50000),
(106, 1, 1, 100000),
(107, 3, 1, 200000), (107, 4, 1, 250000),
(108, 2, 1, 150000),
(109, 3, 1, 200000),
(110, 5, 2, 50000),
(111, 1, 1, 100000), (111, 2, 1, 150000),
(112, 4, 1, 250000),
(113, 3, 1, 200000), (113, 1, 1, 100000),
(114, 2, 1, 150000),
(115, 5, 2, 50000),
(116, 1, 1, 100000),
(117, 2, 1, 150000), (117, 3, 1, 200000),
(118, 4, 1, 250000),
(119, 1, 1, 100000),
(120, 5, 3, 50000),
(121, 2, 1, 150000), (121, 4, 1, 250000),
(122, 3, 1, 200000),
(123, 1, 1, 100000),
(124, 2, 1, 150000), (124, 1, 1, 100000),
(125, 5, 1, 50000),
(126, 1, 1, 100000),
(127, 3, 1, 200000), (127, 4, 1, 250000),
(128, 2, 1, 150000),
(129, 3, 1, 200000),
(130, 5, 2, 50000);

-- Insert into order (12–16/05/2025)
INSERT INTO `order` (order_id, customer_id, order_date, total_amount, status, voucher_code) VALUES
(22, 13, '2025-05-12 08:45:00', 250000, 'PENDING', NULL),
(23, 14, '2025-05-12 09:15:00', 200000, 'COMPLETED', 'KM1'),
(24, 15, '2025-05-12 10:15:00', 350000, 'COMPLETED', NULL),
(25, 16, '2025-05-12 11:15:00', 150000, 'PENDING', NULL),
(26, 17, '2025-05-12 13:45:00', 100000, 'COMPLETED', 'KM5'),
(27, 18, '2025-05-12 16:15:00', 250000, 'COMPLETED', NULL),
(28, 19, '2025-05-13 09:15:00', 350000, 'PENDING', NULL),
(29, 20, '2025-05-13 10:15:00', 200000, 'COMPLETED', 'KM6'),
(30, 21, '2025-05-13 15:15:00', 400000, 'COMPLETED', NULL),
(31, 22, '2025-05-13 17:45:00', 150000, 'PENDING', 'KM7'),
(32, 13, '2025-05-13 19:15:00', 300000, 'COMPLETED', NULL),
(33, 14, '2025-05-14 09:15:00', 250000, 'PENDING', NULL),
(34, 15, '2025-05-14 10:15:00', 350000, 'COMPLETED', 'KM1'),
(35, 16, '2025-05-14 11:15:00', 200000, 'COMPLETED', NULL),
(36, 17, '2025-05-14 15:15:00', 400000, 'PENDING', NULL),
(37, 18, '2025-05-14 16:15:00', 150000, 'COMPLETED', 'KM4'),
(38, 19, '2025-05-15 09:15:00', 300000, 'PENDING', NULL),
(39, 20, '2025-05-15 10:15:00', 250000, 'COMPLETED', 'KM5'),
(40, 21, '2025-05-15 15:15:00', 350000, 'COMPLETED', NULL),
(41, 22, '2025-05-15 17:45:00', 200000, 'PENDING', NULL),
(42, 13, '2025-05-15 19:15:00', 400000, 'COMPLETED', 'KM7'),
(43, 14, '2025-05-16 09:15:00', 150000, 'PENDING', NULL),
(44, 15, '2025-05-16 10:15:00', 300000, 'COMPLETED', 'KM1'),
(45, 16, '2025-05-16 11:15:00', 250000, 'COMPLETED', NULL),
(46, 17, '2025-05-16 15:15:00', 350000, 'PENDING', NULL),
(47, 18, '2025-05-16 16:15:00', 200000, 'COMPLETED', 'KM6'),
(48, 13, '2025-05-19 08:30:00', 250000, 'PENDING', NULL),
(49, 14, '2025-05-19 09:00:00', 200000, 'COMPLETED', 'KM1'),
(50, 15, '2025-05-19 10:00:00', 350000, 'COMPLETED', NULL),
(51, 16, '2025-05-19 11:00:00', 150000, 'PENDING', NULL),
(52, 17, '2025-05-19 13:30:00', 100000, 'COMPLETED', 'KM5'),
(53, 18, '2025-05-19 14:00:00', 250000, 'COMPLETED', NULL),
(54, 19, '2025-05-19 15:00:00', 350000, 'PENDING', NULL),
(55, 20, '2025-05-19 16:00:00', 200000, 'COMPLETED', 'KM6'),
(56, 21, '2025-05-19 17:30:00', 400000, 'COMPLETED', NULL),
(57, 22, '2025-05-19 18:00:00', 150000, 'PENDING', 'KM7'),
(58, 13, '2025-05-20 08:30:00', 300000, 'COMPLETED', NULL),
(59, 14, '2025-05-20 09:00:00', 250000, 'PENDING', NULL),
(60, 15, '2025-05-20 10:00:00', 350000, 'COMPLETED', 'KM1'),
(61, 16, '2025-05-20 11:00:00', 200000, 'COMPLETED', NULL),
(62, 17, '2025-05-20 13:30:00', 400000, 'PENDING', NULL),
(63, 18, '2025-05-20 14:00:00', 150000, 'COMPLETED', 'KM4'),
(64, 19, '2025-05-21 08:30:00', 300000, 'PENDING', NULL),
(65, 20, '2025-05-21 09:00:00', 250000, 'COMPLETED', 'KM5'),
(66, 21, '2025-05-21 10:00:00', 350000, 'COMPLETED', NULL),
(67, 22, '2025-05-21 11:00:00', 200000, 'PENDING', NULL),
(68, 13, '2025-05-21 13:30:00', 400000, 'COMPLETED', 'KM7'),
(69, 14, '2025-05-22 08:30:00', 150000, 'PENDING', NULL),
(70, 15, '2025-05-22 09:00:00', 300000, 'COMPLETED', 'KM1'),
(71, 16, '2025-05-22 10:00:00', 250000, 'COMPLETED', NULL),
(72, 17, '2025-05-22 11:00:00', 350000, 'PENDING', NULL),
(73, 18, '2025-05-22 13:30:00', 200000, 'COMPLETED', 'KM6'),
(74, 19, '2025-05-23 08:30:00', 400000, 'PENDING', NULL),
(75, 20, '2025-05-23 09:00:00', 150000, 'COMPLETED', 'KM1'),
(76, 21, '2025-05-23 10:00:00', 300000, 'COMPLETED', NULL),
(77, 22, '2025-05-23 11:00:00', 250000, 'PENDING', NULL),
(78, 13, '2025-05-23 13:30:00', 350000, 'COMPLETED', 'KM7'),
(79, 14, '2025-05-24 08:30:00', 200000, 'PENDING', NULL),
(80, 15, '2025-05-24 09:00:00', 400000, 'COMPLETED', 'KM1'),
(81, 16, '2025-05-24 10:00:00', 150000, 'COMPLETED', NULL),
(82, 17, '2025-05-24 11:00:00', 300000, 'PENDING', NULL),
(83, 18, '2025-05-24 13:30:00', 250000, 'COMPLETED', 'KM6'),
(84, 19, '2025-05-25 08:30:00', 350000, 'PENDING', NULL),
(85, 20, '2025-05-25 09:00:00', 200000, 'COMPLETED', 'KM1'),
(86, 21, '2025-05-25 10:00:00', 400000, 'COMPLETED', NULL),
(87, 22, '2025-05-25 11:00:00', 150000, 'PENDING', NULL),
(88, 13, '2025-05-25 13:30:00', 300000, 'COMPLETED', 'KM7');

-- Insert into order_detail
INSERT INTO order_detail (order_detail_id, order_id, service_id, quantity, price) VALUES
(33, 22, 1, 1, 100000), (34, 22, 2, 1, 150000),
(35, 23, 3, 1, 200000),
(36, 24, 4, 1, 250000), (37, 24, 1, 1, 100000),
(38, 25, 2, 1, 150000),
(39, 26, 5, 2, 50000),
(40, 27, 4, 1, 250000),
(41, 28, 2, 1, 150000), (42, 28, 3, 1, 200000),
(43, 29, 3, 1, 200000),
(44, 30, 4, 1, 250000), (45, 30, 1, 1, 100000),
(46, 31, 2, 1, 150000),
(47, 32, 3, 1, 200000), (48, 32, 1, 1, 100000),
(49, 33, 4, 1, 250000),
(50, 34, 2, 1, 150000), (51, 34, 3, 1, 200000),
(52, 35, 3, 1, 200000),
(53, 36, 4, 1, 250000), (54, 36, 1, 1, 100000),
(55, 37, 2, 1, 150000),
(56, 38, 3, 1, 200000), (57, 38, 1, 1, 100000),
(58, 39, 4, 1, 250000),
(59, 40, 2, 1, 150000), (60, 40, 3, 1, 200000),
(61, 41, 3, 1, 200000),
(62, 42, 4, 1, 250000), (63, 42, 1, 1, 100000),
(64, 43, 2, 1, 150000),
(65, 44, 3, 1, 200000), (66, 44, 1, 1, 100000),
(67, 45, 4, 1, 250000),
(68, 46, 2, 1, 150000), (69, 46, 3, 1, 200000),
(70, 47, 3, 1, 200000),
(71, 48, 1, 1, 100000), (72, 48, 2, 1, 150000),
(73, 49, 3, 1, 200000),
(74, 50, 4, 1, 250000), (75, 50, 1, 1, 100000),
(76, 51, 2, 1, 150000),
(77, 52, 5, 2, 50000),
(78, 53, 4, 1, 250000),
(79, 54, 2, 1, 150000), (80, 54, 3, 1, 200000),
(81, 55, 3, 1, 200000),
(82, 56, 4, 1, 250000), (83, 56, 1, 1, 100000),
(84, 57, 2, 1, 150000),
(85, 58, 3, 1, 200000), (86, 58, 1, 1, 100000),
(87, 59, 4, 1, 250000),
(88, 60, 2, 1, 150000), (89, 60, 3, 1, 200000),
(90, 61, 3, 1, 200000),
(91, 62, 4, 1, 250000), (92, 62, 1, 1, 100000),
(93, 63, 2, 1, 150000),
(94, 64, 3, 1, 200000), (95, 64, 1, 1, 100000),
(96, 65, 4, 1, 250000),
(97, 66, 2, 1, 150000), (98, 66, 3, 1, 200000),
(99, 67, 3, 1, 200000),
(100, 68, 4, 1, 250000), (101, 68, 1, 1, 100000),
(102, 69, 2, 1, 150000),
(103, 70, 3, 1, 200000), (104, 70, 1, 1, 100000),
(105, 71, 4, 1, 250000),
(106, 72, 2, 1, 150000), (107, 72, 3, 1, 200000),
(108, 73, 3, 1, 200000),
(109, 74, 4, 1, 250000), (110, 74, 1, 1, 100000),
(111, 75, 2, 1, 150000),
(112, 76, 3, 1, 200000), (113, 76, 1, 1, 100000),
(114, 77, 4, 1, 250000),
(115, 78, 2, 1, 150000), (116, 78, 3, 1, 200000),
(117, 79, 3, 1, 200000),
(118, 80, 4, 1, 250000), (119, 80, 1, 1, 100000),
(120, 81, 2, 1, 150000),
(121, 82, 3, 1, 200000), (122, 82, 1, 1, 100000),
(123, 83, 4, 1, 250000),
(124, 84, 2, 1, 150000), (125, 84, 3, 1, 200000),
(126, 85, 3, 1, 200000),
(127, 86, 4, 1, 250000), (128, 86, 1, 1, 100000),
(129, 87, 2, 1, 150000),
(130, 88, 3, 1, 200000), (131, 88, 1, 1, 100000);

-- Insert into invoice (12–16/05/2025)
-- Updated to include transaction_id, provider_transaction_id, payment_provider for QR payments
INSERT INTO invoice (order_id, payment_date, subtotal, discount_percent, discount_amount, points_used, promotion_code, total, amount_paid, payment_method, status, staff_id, note, transaction_id, provider_transaction_id, payment_provider) VALUES
(22, '2025-05-12 09:00:00', 250000, 0, 0, 0, NULL, 250000, 0, 'QR', 'PENDING', 9, 'Thanh toán QR cho hóa đơn #22', 'PCC22_202505120900', 'PAYOS_22_001', 'PAYOS'),
(23, '2025-05-12 09:30:00', 200000, 10, 20000, 0, 'KM1', 180000, 180000, 'CARD', 'COMPLETED', 10, NULL, NULL, NULL, NULL),
(24, '2025-05-12 10:30:00', 350000, 0, 0, 0, NULL, 350000, 350000, 'MOMO', 'COMPLETED', 9, NULL, NULL, NULL, NULL),
(25, '2025-05-12 11:30:00', 150000, 0, 0, 0, NULL, 150000, 0, 'QR', 'PENDING', 10, 'Thanh toán QR cho hóa đơn #25', 'PCC25_202505121130', 'PAYOS_25_002', 'PAYOS'),
(26, '2025-05-12 14:00:00', 100000, 25, 25000, 0, 'KM5', 75000, 75000, 'CASH', 'COMPLETED', 9, NULL, NULL, NULL, NULL),
(27, '2025-05-12 16:30:00', 250000, 0, 0, 0, NULL, 250000, 250000, 'CARD', 'COMPLETED', 10, NULL, NULL, NULL, NULL),
(28, '2025-05-13 09:30:00', 350000, 0, 0, 0, NULL, 350000, 0, 'QR', 'PENDING', 9, 'Thanh toán QR cho hóa đơn #28', 'PCC28_202505130930', 'PAYOS_28_003', 'PAYOS'),
(29, '2025-05-13 10:30:00', 200000, 30, 60000, 0, 'KM6', 140000, 140000, 'BANKING', 'COMPLETED', 10, NULL, NULL, NULL, NULL),
(30, '2025-05-13 15:30:00', 400000, 0, 0, 0, NULL, 400000, 400000, 'CASH', 'COMPLETED', 9, NULL, NULL, NULL, NULL),
(31, '2025-05-13 18:00:00', 150000, 10, 15000, 0, 'KM7', 135000, 0, 'QR', 'PENDING', 10, 'Thanh toán QR cho hóa đơn #31', 'PCC31_202505131800', 'PAYOS_31_004', 'PAYOS'),
(32, '2025-05-13 19:30:00', 300000, 0, 0, 0, NULL, 300000, 300000, 'MOMO', 'COMPLETED', 9, NULL, NULL, NULL, NULL),
(33, '2025-05-14 09:30:00', 250000, 0, 0, 0, NULL, 250000, 0, 'QR', 'PENDING', 10, 'Thanh toán QR cho hóa đơn #33', 'PCC33_202505140930', 'PAYOS_33_005', 'PAYOS'),
(34, '2025-05-14 10:30:00', 350000, 10, 35000, 0, 'KM1', 315000, 315000, 'CASH', 'COMPLETED', 9, NULL, NULL, NULL, NULL),
(35, '2025-05-14 11:30:00', 200000, 0, 0, 0, NULL, 200000, 200000, 'CARD', 'COMPLETED', 10, NULL, NULL, NULL, NULL),
(36, '2025-05-14 15:30:00', 400000, 0, 0, 0, NULL, 400000, 0, 'QR', 'PENDING', 9, 'Thanh toán QR cho hóa đơn #36', 'PCC36_202505141530', 'PAYOS_36_006', 'PAYOS'),
(37, '2025-05-14 16:30:00', 150000, 5, 7500, 0, 'KM4', 142500, 142500, 'BANKING', 'COMPLETED', 10, NULL, NULL, NULL, NULL),
(38, '2025-05-15 09:30:00', 300000, 0, 0, 0, NULL, 300000, 0, 'QR', 'PENDING', 9, 'Thanh toán QR cho hóa đơn #38', 'PCC38_202505150930', 'PAYOS_38_007', 'PAYOS'),
(39, '2025-05-15 10:30:00', 250000, 25, 62500, 0, 'KM5', 187500, 187500, 'CARD', 'COMPLETED', 10, NULL, NULL, NULL, NULL),
(40, '2025-05-15 15:30:00', 350000, 0, 0, 0, NULL, 350000, 350000, 'MOMO', 'COMPLETED', 9, NULL, NULL, NULL, NULL),
(41, '2025-05-15 18:00:00', 200000, 0, 0, 0, NULL, 200000, 0, 'QR', 'PENDING', 10, 'Thanh toán QR cho hóa đơn #41', 'PCC41_202505151800', 'PAYOS_41_008', 'PAYOS'),
(42, '2025-05-15 19:30:00', 400000, 10, 40000, 0, 'KM7', 360000, 360000, 'CASH', 'COMPLETED', 9, NULL, NULL, NULL, NULL),
(43, '2025-05-16 09:30:00', 150000, 0, 0, 0, NULL, 150000, 0, 'QR', 'PENDING', 10, 'Thanh toán QR cho hóa đơn #43', 'PCC43_202505160930', 'PAYOS_43_009', 'PAYOS'),
(44, '2025-05-16 10:30:00', 300000, 10, 30000, 0, 'KM1', 270000, 270000, 'MOMO', 'COMPLETED', 9, NULL, NULL, NULL, NULL),
(45, '2025-05-16 11:30:00', 250000, 0, 0, 0, NULL, 250000, 250000, 'BANKING', 'COMPLETED', 10, NULL, NULL, NULL, NULL),
(46, '2025-05-16 15:30:00', 350000, 0, 0, 0, NULL, 350000, 0, 'QR', 'PENDING', 9, 'Thanh toán QR cho hóa đơn #46', 'PCC46_202505161530', 'PAYOS_46_010', 'PAYOS'),
(47, '2025-05-16 16:30:00', 200000, 30, 60000, 0, 'KM6', 140000, 140000, 'CARD', 'COMPLETED', 10, NULL, NULL, NULL, NULL),
(48, '2025-05-19 08:45:00', 250000, 0, 0, 0, NULL, 250000, 0, 'QR', 'PENDING', 9, 'Thanh toán QR cho hóa đơn #48', 'PCC48_202505190845', 'PAYOS_48_011', 'PAYOS'),
(49, '2025-05-19 09:15:00', 200000, 10, 20000, 0, 'KM1', 180000, 180000, 'CARD', 'COMPLETED', 10, NULL, NULL, NULL, NULL),
(50, '2025-05-19 10:15:00', 350000, 0, 0, 0, NULL, 350000, 350000, 'MOMO', 'COMPLETED', 9, NULL, NULL, NULL, NULL),
(51, '2025-05-19 11:15:00', 150000, 0, 0, 0, NULL, 150000, 0, 'QR', 'PENDING', 10, 'Thanh toán QR cho hóa đơn #51', 'PCC51_202505191115', 'PAYOS_51_012', 'PAYOS'),
(52, '2025-05-19 13:45:00', 100000, 25, 25000, 0, 'KM5', 75000, 75000, 'CASH', 'COMPLETED', 9, NULL, NULL, NULL, NULL),
(53, '2025-05-19 14:15:00', 250000, 0, 0, 0, NULL, 250000, 250000, 'CARD', 'COMPLETED', 10, NULL, NULL, NULL, NULL),
(54, '2025-05-19 15:15:00', 350000, 0, 0, 0, NULL, 350000, 0, 'QR', 'PENDING', 9, 'Thanh toán QR cho hóa đơn #54', 'PCC54_202505191515', 'PAYOS_54_013', 'PAYOS'),
(55, '2025-05-19 16:15:00', 200000, 30, 60000, 0, 'KM6', 140000, 140000, 'BANKING', 'COMPLETED', 10, NULL, NULL, NULL, NULL),
(56, '2025-05-19 17:45:00', 400000, 0, 0, 0, NULL, 400000, 400000, 'CASH', 'COMPLETED', 9, NULL, NULL, NULL, NULL),
(57, '2025-05-19 18:15:00', 150000, 10, 15000, 0, 'KM7', 135000, 0, 'QR', 'PENDING', 10, 'Thanh toán QR cho hóa đơn #57', 'PCC57_202505191815', 'PAYOS_57_014', 'PAYOS'),
(58, '2025-05-20 08:45:00', 300000, 0, 0, 0, NULL, 300000, 300000, 'MOMO', 'COMPLETED', 9, NULL, NULL, NULL, NULL),
(59, '2025-05-20 09:15:00', 250000, 0, 0, 0, NULL, 250000, 0, 'QR', 'PENDING', 10, 'Thanh toán QR cho hóa đơn #59', 'PCC59_202505200915', 'PAYOS_59_015', 'PAYOS'),
(60, '2025-05-20 10:15:00', 350000, 10, 35000, 0, 'KM1', 315000, 315000, 'CASH', 'COMPLETED', 9, NULL, NULL, NULL, NULL),
(61, '2025-05-20 11:15:00', 200000, 0, 0, 0, NULL, 200000, 200000, 'CARD', 'COMPLETED', 10, NULL, NULL, NULL, NULL),
(62, '2025-05-20 13:45:00', 400000, 0, 0, 0, NULL, 400000, 0, 'QR', 'PENDING', 9, 'Thanh toán QR cho hóa đơn #62', 'PCC62_202505201345', 'PAYOS_62_016', 'PAYOS'),
(63, '2025-05-20 14:15:00', 150000, 5, 7500, 0, 'KM4', 142500, 142500, 'BANKING', 'COMPLETED', 10, NULL, NULL, NULL, NULL),
(64, '2025-05-21 08:45:00', 300000, 0, 0, 0, NULL, 300000, 0, 'QR', 'PENDING', 9, 'Thanh toán QR cho hóa đơn #64', 'PCC64_202505210845', 'PAYOS_64_017', 'PAYOS'),
(65, '2025-05-21 09:15:00', 250000, 25, 62500, 0, 'KM5', 187500, 187500, 'CARD', 'COMPLETED', 10, NULL, NULL, NULL, NULL),
(66, '2025-05-21 10:15:00', 350000, 0, 0, 0, NULL, 350000, 350000, 'MOMO', 'COMPLETED', 9, NULL, NULL, NULL, NULL),
(67, '2025-05-21 11:15:00', 200000, 0, 0, 0, NULL, 200000, 0, 'QR', 'PENDING', 10, 'Thanh toán QR cho hóa đơn #67', 'PCC67_202505211115', 'PAYOS_67_018', 'PAYOS'),
(68, '2025-05-21 13:45:00', 400000, 10, 40000, 0, 'KM7', 360000, 360000, 'CASH', 'COMPLETED', 9, NULL, NULL, NULL, NULL),
(69, '2025-05-22 08:45:00', 150000, 0, 0, 0, NULL, 150000, 0, 'QR', 'PENDING', 10, 'Thanh toán QR cho hóa đơn #69', 'PCC69_202505220845', 'PAYOS_69_019', 'PAYOS'),
(70, '2025-05-22 09:15:00', 300000, 10, 30000, 0, 'KM1', 270000, 270000, 'MOMO', 'COMPLETED', 9, NULL, NULL, NULL, NULL),
(71, '2025-05-22 10:15:00', 250000, 0, 0, 0, NULL, 250000, 250000, 'BANKING', 'COMPLETED', 10, NULL, NULL, NULL, NULL),
(72, '2025-05-22 11:15:00', 350000, 0, 0, 0, NULL, 350000, 0, 'QR', 'PENDING', 9, 'Thanh toán QR cho hóa đơn #72', 'PCC72_202505221115', 'PAYOS_72_020', 'PAYOS'),
(73, '2025-05-22 13:45:00', 200000, 30, 60000, 0, 'KM6', 140000, 140000, 'CARD', 'COMPLETED', 10, NULL, NULL, NULL, NULL),
(74, '2025-05-23 08:45:00', 400000, 0, 0, 0, NULL, 400000, 0, 'QR', 'PENDING', 9, 'Thanh toán QR cho hóa đơn #74', 'PCC74_202505230845', 'PAYOS_74_021', 'PAYOS'),
(75, '2025-05-23 09:15:00', 150000, 10, 15000, 0, 'KM1', 135000, 135000, 'CASH', 'COMPLETED', 10, NULL, NULL, NULL, NULL),
(76, '2025-05-23 10:15:00', 300000, 0, 0, 0, NULL, 300000, 300000, 'CARD', 'COMPLETED', 9, NULL, NULL, NULL, NULL),
(77, '2025-05-23 11:15:00', 250000, 0, 0, 0, NULL, 250000, 0, 'QR', 'PENDING', 10, 'Thanh toán QR cho hóa đơn #77', 'PCC77_202505231115', 'PAYOS_77_022', 'PAYOS'),
(78, '2025-05-23 13:45:00', 350000, 10, 35000, 0, 'KM7', 315000, 315000, 'MOMO', 'COMPLETED', 9, NULL, NULL, NULL, NULL),
(79, '2025-05-24 08:45:00', 200000, 0, 0, 0, NULL, 200000, 0, 'QR', 'PENDING', 10, 'Thanh toán QR cho hóa đơn #79', 'PCC79_202505240845', 'PAYOS_79_023', 'PAYOS'),
(80, '2025-05-24 09:15:00', 400000, 10, 40000, 0, 'KM1', 360000, 360000, 'BANKING', 'COMPLETED', 9, NULL, NULL, NULL, NULL),
(81, '2025-05-24 10:15:00', 150000, 0, 0, 0, NULL, 150000, 150000, 'CARD', 'COMPLETED', 10, NULL, NULL, NULL, NULL),
(82, '2025-05-24 11:15:00', 300000, 0, 0, 0, NULL, 300000, 0, 'QR', 'PENDING', 9, 'Thanh toán QR cho hóa đơn #82', 'PCC82_202505241115', 'PAYOS_82_024', 'PAYOS'),
(83, '2025-05-24 13:45:00', 250000, 30, 75000, 0, 'KM6', 175000, 175000, 'CASH', 'COMPLETED', 10, NULL, NULL, NULL, NULL),
(84, '2025-05-25 08:45:00', 350000, 0, 0, 0, NULL, 350000, 0, 'QR', 'PENDING', 9, 'Thanh toán QR cho hóa đơn #84', 'PCC84_202505250845', 'PAYOS_84_025', 'PAYOS'),
(85, '2025-05-25 09:15:00', 200000, 10, 20000, 0, 'KM1', 180000, 180000, 'MOMO', 'COMPLETED', 10, NULL, NULL, NULL, NULL),
(86, '2025-05-25 10:15:00', 400000, 0, 0, 0, NULL, 400000, 400000, 'CARD', 'COMPLETED', 9, NULL, NULL, NULL, NULL),
(87, '2025-05-25 11:15:00', 150000, 0, 0, 0, NULL, 150000, 0, 'QR', 'PENDING', 10, 'Thanh toán QR cho hóa đơn #87', 'PCC87_202505251115', 'PAYOS_87_026', 'PAYOS'),
(88, '2025-05-25 13:45:00', 300000, 10, 30000, 0, 'KM7', 270000, 270000, 'BANKING', 'COMPLETED', 9, NULL, NULL, NULL, NULL);


-- Insert into work_schedule
INSERT INTO work_schedule (staff_id, work_date, shift, note, start_time, end_time, location, task) VALUES
(2, '2025-06-19', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(3, '2025-05-12', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(4, '2025-05-12', 'EVENING', 'WORKING', '17:00:00', '21:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(5, '2025-05-12', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(6, '2025-05-12', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(7, '2025-05-12', 'EVENING', 'WORKING', '17:00:00', '21:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(8, '2025-05-12', 'MORNING', 'OFF', NULL, NULL, 'Store 1', NULL),
(9, '2025-05-12', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Thu ngân'),
(10, '2025-05-12', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Thu ngân'),
(11, '2025-05-12', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Tiếp nhận khách'),
(12, '2025-05-12', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Tiếp nhận khách'),
(2, '2025-05-13', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(3, '2025-05-13', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(4, '2025-05-13', 'EVENING', 'ABSENT', '17:00:00', '21:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(5, '2025-05-13', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(6, '2025-05-13', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(7, '2025-05-13', 'EVENING', 'WORKING', '17:00:00', '21:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(8, '2025-05-13', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(9, '2025-05-13', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Thu ngân'),
(10, '2025-05-13', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Thu ngân'),
(11, '2025-05-13', 'EVENING', 'WORKING', '17:00:00', '21:00:00', 'Store 1', 'Tiếp nhận khách'),
(12, '2025-05-13', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Tiếp nhận khách'),
(2, '2025-05-14', 'EVENING', 'WORKING', '17:00:00', '21:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(3, '2025-05-14', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(4, '2025-05-14', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(5, '2025-05-14', 'EVENING', 'WORKING', '17:00:00', '21:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(6, '2025-05-14', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(7, '2025-05-14', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(8, '2025-05-14', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(9, '2025-05-14', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Thu ngân'),
(10, '2025-05-14', 'EVENING', 'WORKING', '17:00:00', '21:00:00', 'Store 1', 'Thu ngân'),
(11, '2025-05-14', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Tiếp nhận khách'),
(12, '2025-05-14', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Tiếp nhận khách'),
(2, '2025-05-15', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(3, '2025-05-15', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(4, '2025-05-15', 'EVENING', 'WORKING', '17:00:00', '21:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(5, '2025-05-15', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(6, '2025-05-15', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(7, '2025-05-15', 'EVENING', 'WORKING', '17:00:00', '21:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(8, '2025-05-15', 'MORNING', 'OFF', NULL, NULL, 'Store 1', NULL),
(9, '2025-05-15', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Thu ngân'),
(10, '2025-05-15', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Thu ngân'),
(11, '2025-05-15', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Tiếp nhận khách'),
(12, '2025-05-15', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Tiếp nhận khách'),
(2, '2025-05-16', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(3, '2025-05-16', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(4, '2025-05-16', 'EVENING', 'WORKING', '17:00:00', '21:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(5, '2025-05-16', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(6, '2025-05-16', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(7, '2025-05-16', 'EVENING', 'WORKING', '17:00:00', '21:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(8, '2025-05-16', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(9, '2025-05-16', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Thu ngân'),
(10, '2025-05-16', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Thu ngân'),
(11, '2025-05-16', 'EVENING', 'WORKING', '17:00:00', '21:00:00', 'Store 1', 'Tiếp nhận khách'),
(12, '2025-05-16', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Tiếp nhận khách');

-- Insert into account_permission (gán quyền mẫu cho một số tài khoản)
INSERT INTO account_permission (account_id, permission_code) VALUES
-- ADMIN (account_id = 1)
(1, 'CREATE_BOOKING'),
(1, 'MANAGE_PAYMENT'),
(1, 'VIEW_CUSTOMER'),
(1, 'PRINT_RECEIPT'),
(1, 'APPLY_PROMOTION'),
(1, 'VIEW_INVOICE'),
(1, 'VIEW_BOOKING_ASSIGNED'),
(1, 'MARK_SERVICE_DONE'),
(1, 'UPDATE_PET_STATUS'),
(1, 'VIEW_SCHEDULE'),
(1, 'MANAGE_ACCOUNT'),
(1, 'ASSIGN_PERMISSION'),
(1, 'MANAGE_SCHEDULE'),
(1, 'ADD_EMPLOYEE'),
(1, 'MANAGE_SERVICE'),
(1, 'VIEW_FINANCE'),
(1, 'VIEW_DASHBOARD'),
(1, 'UPDATE_PROFILE'),
(1, 'APPROVE_LEAVE'),
(1, 'APPROVE_SHIFT_CHANGE'),
-- STAFF_CARE (account_id = 2, ví dụ)
(2, 'VIEW_BOOKING_ASSIGNED'),
(2, 'VIEW_SCHEDULE'),
(2, 'UPDATE_PROFILE'),
(2, 'REGISTER_SHIFT'),
(2, 'REQUEST_LEAVE'),
-- STAFF_CASHIER (account_id = 9, ví dụ)
(9, 'CREATE_BOOKING'),
(9, 'MANAGE_PAYMENT'),
(9, 'VIEW_INVOICE'),
(9, 'VIEW_SCHEDULE'),
(9, 'UPDATE_PROFILE'),
(9, 'VIEW_BOOKING_ASSIGNED'),
(9, 'REGISTER_SHIFT'),
(9, 'REQUEST_LEAVE'),
-- STAFF_RECEPTION (account_id = 11, ví dụ)
(11, 'CREATE_BOOKING'),
(11, 'VIEW_SCHEDULE'),
(11, 'UPDATE_PROFILE'),
(11, 'VIEW_BOOKING_ASSIGNED'),
(11, 'REGISTER_SHIFT'),
(11, 'REQUEST_LEAVE');

-- Update invoice to reset promotion fields for consistency
UPDATE invoice 
SET promotion_code = NULL, 
    discount_percent = 0, 
    discount_amount = 0, 
    total = subtotal 
WHERE promotion_code IS NOT NULL;

-- Assign permissions to roles
CALL assign_permission_by_role(1);
CALL assign_permission_by_role(2);
CALL assign_permission_by_role(3);
CALL assign_permission_by_role(4);
CALL assign_permission_by_role(5);

-- Assign specific permissions
CALL sp_assign_permission_to_role(2, 'REGISTER_SHIFT');
CALL sp_assign_permission_to_role(2, 'REQUEST_LEAVE');
CALL sp_assign_permission_to_role(3, 'REGISTER_SHIFT');
CALL sp_assign_permission_to_role(3, 'REQUEST_LEAVE');
CALL sp_assign_permission_to_role(4, 'REGISTER_SHIFT');
CALL sp_assign_permission_to_role(4, 'REQUEST_LEAVE');
CALL sp_assign_permission_to_role(1, 'APPROVE_LEAVE');
CALL sp_assign_permission_to_role(1, 'APPROVE_SHIFT_CHANGE');

SET SQL_SAFE_UPDATES = 0;

-- 1. UPDATE promotion: shift start & end date
UPDATE promotion
SET
start_date = DATE_ADD(start_date, INTERVAL 40 DAY),
end_date = DATE_ADD(end_date, INTERVAL 40 DAY);

-- 2. UPDATE booking_time (bắt đầu từ 21/06)
UPDATE booking
SET booking_time = DATE_ADD(booking_time, INTERVAL 40 DAY);

-- 3. UPDATE order_date (ghi nhận đơn hàng khách từ 21/06)
UPDATE `order`
SET order_date = DATE_ADD(order_date, INTERVAL 40 DAY);

-- 4. UPDATE invoice.payment_date
UPDATE invoice
SET payment_date = DATE_ADD(payment_date, INTERVAL 40 DAY);

-- 5. UPDATE work_schedule (ngày làm việc staff chỉnh từ 21/06)
UPDATE work_schedule
SET work_date = DATE_ADD(work_date, INTERVAL 40 DAY);


-- Kích hoạt lại nếu cần (tùy chọn)
SET SQL_SAFE_UPDATES = 1;