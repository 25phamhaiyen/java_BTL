DROP TABLE IF EXISTS account_permission;
DROP TABLE IF EXISTS booking_detail;
DROP TABLE IF EXISTS order_detail;
DROP TABLE IF EXISTS invoice;
DROP TABLE IF EXISTS booking;
DROP TABLE IF EXISTS pet;
DROP TABLE IF EXISTS `order`;
DROP TABLE IF EXISTS work_schedule;
DROP TABLE IF EXISTS staff;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS account;

-- Sau đó xóa các bảng không phụ thuộc hoặc bảng cha
DROP TABLE IF EXISTS pet_type;
DROP TABLE IF EXISTS service;
DROP TABLE IF EXISTS promotion;
DROP TABLE IF EXISTS permission;
DROP TABLE IF EXISTS role;
DROP TABLE IF EXISTS person;


INSERT INTO role (role_name) VALUES 
('ADMIN'), 
('STAFF_CARE'), 
('STAFF_CASHIER'), 
('STAFF_RECEPTION'),
('OUT');

INSERT INTO person (person_id, full_name, gender, phone, address, email)
VALUES
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
-- 10 khách hàng
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

INSERT INTO `account` (account_id, username, `password`, role_id)
VALUES
(1, 'admin01', '$2a$10$bxB8Fa1fAwS7BZoplZQCQOyc6CQEMDixqUWZ1e6O/6QJRUVW5mYlG', 1), -- pass: admin123
(2, 'care01', '$2a$10$Jg2R.bSJNMiMZPhxW6wIr.JZn5n0hW7QoqfKB3ehaK94SnSIgFDOq', 2), -- pass: 123
(3, 'care02', '$2a$10$Jg2R.bSJNMiMZPhxW6wIr.JZn5n0hW7QoqfKB3ehaK94SnSIgFDOq', 2), -- pass: 123
(4, 'care03', '$2a$10$Jg2R.bSJNMiMZPhxW6wIr.JZn5n0hW7QoqfKB3ehaK94SnSIgFDOq', 2), -- pass: 123
(5, 'care04', '$2a$10$Jg2R.bSJNMiMZPhxW6wIr.JZn5n0hW7QoqfKB3ehaK94SnSIgFDOq', 2), -- pass: 123
(6, 'care05', '$2a$10$Jg2R.bSJNMiMZPhxW6wIr.JZn5n0hW7QoqfKB3ehaK94SnSIgFDOq', 2), -- pass: 123
(7, 'care06', '$2a$10$Jg2R.bSJNMiMZPhxW6wIr.JZn5n0hW7QoqfKB3ehaK94SnSIgFDOq', 2), -- pass: 123
(8, 'care07', '$2a$10$Jg2R.bSJNMiMZPhxW6wIr.JZn5n0hW7QoqfKB3ehaK94SnSIgFDOq', 2), -- pass: 123
(9, 'cashier01', '$2a$10$Jg2R.bSJNMiMZPhxW6wIr.JZn5n0hW7QoqfKB3ehaK94SnSIgFDOq', 3), -- pass: 123
(10, 'cashier02', '$2a$10$Jg2R.bSJNMiMZPhxW6wIr.JZn5n0hW7QoqfKB3ehaK94SnSIgFDOq', 3), -- pass: 123
(11, 'reception01', '$2a$10$Jg2R.bSJNMiMZPhxW6wIr.JZn5n0hW7QoqfKB3ehaK94SnSIgFDOq', 4), -- pass: 123
(12, 'reception02', '$2a$10$Jg2R.bSJNMiMZPhxW6wIr.JZn5n0hW7QoqfKB3ehaK94SnSIgFDOq', 4), -- pass: 123
(13, 'reception03', '$2a$10$Jg2R.bSJNMiMZPhxW6wIr.JZn5n0hW7QoqfKB3ehaK94SnSIgFDOq', 4); -- pass: 123

INSERT INTO staff (staff_id, dob, salary, hire_date, account_id, role_id)
VALUES
(1, '1990-01-01', 20000000, '2020-01-01', 1, 1),
(2, '1991-02-01', 10000000, '2021-01-01', 2, 2),
(3, '1992-03-01', 10000000, '2021-01-01', 3, 2),
(4, '1993-04-01', 10000000, '2021-01-01', 4, 2),
(5, '1994-05-01', 10000000, '2021-01-01', 5, 2),
(6, '1995-06-01', 10000000, '2021-01-01', 6, 2),
(7, '1996-07-01', 10000000, '2021-01-01', 7, 2),
(8, '1997-08-01', 10000000, '2021-01-01', 8, 2),
(9, '1991-09-01', 8000000, '2022-01-01', 9, 3),
(10, '1992-10-01', 8000000, '2022-01-01', 10, 3),
(11, '1993-11-01', 9000000, '2022-01-01', 11, 4),
(12, '1994-12-01', 9000000, '2022-01-01', 12, 4);

INSERT INTO customer (customer_id, `point`)
VALUES
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

INSERT INTO pet_type (type_id, species, breed) VALUES
(1, 'Dog', 'Poodle'),
(2, 'Dog', 'Shiba'),
(3, 'Cat', 'Mèo Anh Lông Ngắn'),
(4, 'Cat', 'Mèo Ba Tư'),
(5, 'Hamster', 'Hamster Bear');

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

INSERT INTO service (service_id, name, description, price, duration_minutes, active) VALUES
(1, 'Tắm thú cưng', 'Tắm, sấy khô và chải lông cho thú cưng', 100000, 30, true),
(2, 'Cắt tỉa lông', 'Cắt tỉa tạo kiểu theo yêu cầu', 150000, 45, true),
(3, 'Khám sức khỏe', 'Kiểm tra tổng quát sức khỏe thú AFF', 200000, 30, true),
(4, 'Tiêm phòng', 'Tiêm các loại vaccine cơ bản', 250000, 15, true),
(5, 'Gửi trông thú cưng', 'Dịch vụ giữ thú cưng theo giờ', 50000, 60, true),
(6, 'Huấn luyện cơ bản', 'Dạy ngồi, đứng, bắt tay,...', 300000, 60, false);

INSERT INTO promotion (promotion_id, code, description, discount_percent, start_date, end_date, active) VALUES
(1, 'KM1', 'Giảm 10% cho tất cả các dịch vụ', 10, '2025-05-04', '2025-05-15', true),
(2, 'KM2', 'Chỉ áp dụng cho khách hàng lần đầu sử dụng', 20, '2025-05-04', '2025-05-15', true),
(3, 'KM3', 'Giảm 15% khi sử dụng combo 2 dịch vụ', 15, '2025-05-04', '2025-05-15', false),
(4, 'KM4', 'Giảm 5% toàn bộ đơn hàng >300k', 5, '2025-05-04', '2025-05-15', true),
(5, 'KM5', 'Giảm 25% cho đơn hàng từ 500k trở lên', 25, '2025-05-05', '2025-05-19', true),
(6, 'KM6', 'Giảm 30% cho khách hàng VIP', 30, '2025-05-10', '2025-06-09', true),
(7, 'KM7', 'Giảm 10% khi đặt lịch trước 3 ngày', 10, '2025-05-15', '2025-06-14', true),
(8, 'KM8', 'Giảm 20% cho dịch vụ thứ 3 trong combo', 20, '2025-05-04', '2025-05-31', false),
(9, 'KM9', 'Giảm 15% cho đơn hàng cuối tuần', 15, '2025-06-01', '2025-06-30', true);

INSERT INTO booking (booking_id, customer_id, pet_id, staff_id, booking_time, status, note) VALUES
-- Dữ liệu từ 1/4/2025 đến 10/4/2025
(1, 21, 1, 2, '2025-04-01 10:00:00', 'PENDING', NULL),
(2, 22, 2, 3, '2025-04-02 14:00:00', 'CONFIRMED', NULL),
(3, 13, 3, 4, '2025-04-03 09:30:00', 'PENDING', NULL),
(4, 14, 4, 5, '2025-04-04 16:00:00', 'COMPLETED', NULL),
(5, 15, 5, 6, '2025-04-05 11:00:00', 'CONFIRMED', NULL),
(6, 16, 6, 7, '2025-04-06 15:00:00', 'CANCELLED', NULL),
(7, 17, 7, 8, '2025-04-07 13:00:00', 'PENDING', NULL),
(8, 18, 8, 9, '2025-04-08 08:30:00', 'CONFIRMED', NULL),
(9, 19, 9, 10, '2025-04-09 17:30:00', 'COMPLETED', NULL),
(10, 20, 10, 11, '2025-04-10 10:45:00', 'PENDING', NULL),
-- Dữ liệu ngày 1/5/2025
(11, 13, 5, 2, '2025-05-01 08:30:00', 'PENDING', 'Tắm sạch lông'),
(12, 14, 7, 2, '2025-05-01 09:00:00', 'CONFIRMED', 'Cắt lông gọn gàng'),
(13, 15, 9, 5, '2025-05-01 10:00:00', 'COMPLETED', NULL),
(14, 16, 11, 5, '2025-05-01 11:00:00', 'PENDING', 'Khám kỹ'),
(15, 17, 13, 6, '2025-05-01 13:30:00', 'CONFIRMED', NULL),
(16, 18, 15, 6, '2025-05-01 14:00:00', 'CANCELLED', 'Khách hủy'),
(17, 19, 17, 3, '2025-05-01 15:00:00', 'PENDING', NULL),
(18, 20, 19, 3, '2025-05-01 16:00:00', 'COMPLETED', 'Tiêm vaccine'),
(19, 21, 1, 7, '2025-05-01 17:30:00', 'CONFIRMED', NULL),
(20, 22, 2, 7, '2025-05-01 18:00:00', 'PENDING', 'Giữ lâu dài'),
(21, 13, 6, 4, '2025-05-01 19:00:00', 'COMPLETED', NULL),
(22, 14, 8, 4, '2025-05-01 20:00:00', 'CONFIRMED', NULL),
-- Ngày 2/5/2025
(23, 15, 10, 3, '2025-05-02 08:30:00', 'PENDING', NULL),
(24, 16, 12, 3, '2025-05-02 09:00:00', 'CONFIRMED', 'Tắm nhẹ nhàng'),
(25, 17, 14, 5, '2025-05-02 10:00:00', 'COMPLETED', NULL),
(26, 18, 16, 5, '2025-05-02 11:00:00', 'PENDING', 'Cắt lông đẹp'),
(27, 19, 18, 2, '2025-05-02 13:30:00', 'CONFIRMED', NULL),
(28, 20, 20, 2, '2025-05-02 14:00:00', 'CANCELLED', 'Khách bận'),
(29, 21, 3, 6, '2025-05-02 15:00:00', 'PENDING', NULL),
(30, 22, 4, 6, '2025-05-02 16:00:00', 'COMPLETED', NULL),
(31, 13, 5, 7, '2025-05-02 17:30:00', 'CONFIRMED', 'Khám sức khỏe'),
(32, 14, 7, 7, '2025-05-02 18:00:00', 'PENDING', NULL),
(33, 15, 9, 11, '2025-05-02 19:00:00', 'COMPLETED', NULL),
(34, 16, 11, 11, '2025-05-02 20:00:00', 'CONFIRMED', NULL),
-- Ngày 3/5/2025
(35, 17, 13, 6, '2025-05-03 08:30:00', 'PENDING', 'Tắm sạch'),
(36, 18, 15, 6, '2025-05-03 09:00:00', 'CONFIRMED', NULL),
(37, 19, 17, 2, '2025-05-03 10:00:00', 'COMPLETED', NULL),
(38, 20, 19, 2, '2025-05-03 11:00:00', 'PENDING', 'Cắt tỉa lông'),
(39, 21, 1, 7, '2025-05-03 13:30:00', 'CONFIRMED', NULL),
(40, 22, 2, 7, '2025-05-03 14:00:00', 'CANCELLED', NULL),
(41, 13, 6, 4, '2025-05-03 15:00:00', 'PENDING', NULL),
(42, 14, 8, 4, '2025-05-03 16:00:00', 'COMPLETED', 'Tiêm phòng'),
(43, 15, 10, 5, '2025-05-03 17:30:00', 'CONFIRMED', NULL),
(44, 16, 12, 5, '2025-05-03 18:00:00', 'PENDING', NULL),
(45, 17, 14, 3, '2025-05-03 19:00:00', 'COMPLETED', NULL),
(46, 18, 16, 3, '2025-05-03 20:00:00', 'CONFIRMED', 'Giữ thú cưng'),
-- Ngày 4/5/2025
(47, 19, 18, 2, '2025-05-04 08:30:00', 'PENDING', NULL),
(48, 20, 20, 2, '2025-05-04 09:00:00', 'CONFIRMED', 'Tắm sạch'),
(49, 21, 3, 5, '2025-05-04 10:00:00', 'COMPLETED', NULL),
(50, 22, 4, 5, '2025-05-04 11:00:00', 'PENDING', NULL),
(51, 13, 5, 3, '2025-05-04 13:30:00', 'CONFIRMED', 'Cắt lông'),
(52, 14, 7, 3, '2025-05-04 14:00:00', 'CANCELLED', 'Khách hủy'),
(53, 15, 9, 6, '2025-05-04 15:00:00', 'PENDING', NULL),
(54, 16, 11, 6, '2025-05-04 16:00:00', 'COMPLETED', NULL),
(55, 17, 13, 7, '2025-05-04 17:30:00', 'CONFIRMED', 'Khám sức khỏe'),
(56, 18, 15, 7, '2025-05-04 18:00:00', 'PENDING', NULL),
(57, 19, 17, 4, '2025-05-04 19:00:00', 'COMPLETED', NULL),
(58, 20, 19, 4, '2025-05-04 20:00:00', 'CONFIRMED', NULL),
-- Ngày 5/5/2025
(59, 21, 1, 5, '2025-05-05 08:30:00', 'PENDING', 'Tắm sạch'),
(60, 22, 2, 5, '2025-05-05 09:00:00', 'CONFIRMED', NULL),
(61, 13, 6, 6, '2025-05-05 10:00:00', 'COMPLETED', NULL),
(62, 14, 8, 6, '2025-05-05 11:00:00', 'PENDING', 'Cắt tỉa lông'),
(63, 15, 10, 2, '2025-05-05 13:30:00', 'CONFIRMED', NULL),
(64, 16, 12, 2, '2025-05-05 14:00:00', 'CANCELLED', NULL),
(65, 17, 14, 3, '2025-05-05 15:00:00', 'PENDING', NULL),
(66, 18, 16, 3, '2025-05-05 16:00:00', 'COMPLETED', 'Tiêm phòng'),
(67, 19, 18, 7, '2025-05-05 17:30:00', 'CONFIRMED', NULL),
(68, 20, 20, 7, '2025-05-05 18:00:00', 'PENDING', NULL),
(69, 21, 3, 4, '2025-05-05 19:00:00', 'COMPLETED', NULL),
(70, 22, 4, 4, '2025-05-05 20:00:00', 'CONFIRMED', 'Giữ thú cưng');

INSERT INTO booking_detail (booking_id, service_id, quantity, price) VALUES
-- Dữ liệu từ 1/4/2025 đến 10/4/2025
(1, 1, 1, 100000), (1, 2, 1, 150000), -- booking_id 1: Tắm + Cắt lông
(2, 1, 2, 100000), -- booking_id 2: Tắm (2 lần)
(3, 3, 1, 200000), -- booking_id 3: Khám sức khỏe
(4, 2, 1, 150000), (4, 4, 1, 250000), -- booking_id 4: Cắt lông + Tiêm phòng
(5, 1, 1, 100000), -- booking_id 5: Tắm
(6, 2, 2, 150000), -- booking_id 6: Cắt lông (2 lần)
(7, 5, 1, 50000), -- booking_id 7: Gửi trông
(8, 3, 1, 200000), -- booking_id 8: Khám sức khỏe
(9, 4, 1, 250000), (9, 1, 1, 100000), -- booking_id 9: Tiêm phòng + Tắm
(10, 1, 1, 100000), -- booking_id 10: Tắm
-- Ngày 1/5/2025
(11, 1, 1, 100000), (11, 2, 1, 150000), -- booking_id 11: Tắm + Cắt lông
(12, 2, 1, 150000), -- booking_id 12: Cắt lông
(13, 3, 1, 200000), (13, 1, 1, 100000), -- booking_id 13: Khám sức khỏe + Tắm
(14, 3, 1, 200000), -- booking_id 14: Khám sức khỏe
(15, 5, 2, 50000), -- booking_id 15: Gửi trông (2 lần)
(16, 1, 1, 100000), -- booking_id 16: Tắm
(17, 2, 1, 150000), (17, 3, 1, 200000), -- booking_id 17: Cắt lông + Khám sức khỏe
(18, 4, 1, 250000), -- booking_id 18: Tiêm phòng
(19, 1, 1, 100000), -- booking_id 19: Tắm
(20, 5, 3, 50000), -- booking_id 20: Gửi trông (3 lần)
(21, 2, 1, 150000), (21, 4, 1, 250000), -- booking_id 21: Cắt lông + Tiêm phòng
(22, 3, 1, 200000), -- booking_id 22: Khám sức khỏe
-- Ngày 2/5/2025
(23, 1, 1, 100000), -- booking_id 23: Tắm
(24, 2, 1, 150000), (24, 1, 1, 100000), -- booking_id 24: Cắt lông + Tắm
(25, 3, 1, 200000), -- booking_id 25: Khám sức khỏe
(26, 2, 2, 150000), -- booking_id 26: Cắt lông (2 lần)
(27, 5, 1, 50000), -- booking_id 27: Gửi trông
(28, 1, 1, 100000), -- booking_id 28: Tắm
(29, 3, 1, 200000), (29, 4, 1, 250000), -- booking_id 29: Khám sức khỏe + Tiêm phòng
(30, 2, 1, 150000), -- booking_id 30: Cắt lông
(31, 3, 1, 200000), -- booking_id 31: Khám sức khỏe
(32, 5, 2, 50000), -- booking_id 32: Gửi trông (2 lần)
(33, 1, 1, 100000), (33, 2, 1, 150000), -- booking_id 33: Tắm + Cắt lông
(34, 4, 1, 250000), -- booking_id 34: Tiêm phòng
-- Ngày 3/5/2025
(35, 1, 1, 100000), -- booking_id 35: Tắm
(36, 2, 1, 150000), -- booking_id 36: Cắt lông
(37, 3, 1, 200000), (37, 1, 1, 100000), -- booking_id 37: Khám sức khỏe + Tắm
(38, 2, 1, 150000), -- booking_id 38: Cắt lông
(39, 5, 2, 50000), -- booking_id 39: Gửi trông (2 lần)
(40, 1, 1, 100000), -- booking_id 40: Tắm
(41, 3, 1, 200000), -- booking_id 41: Khám sức khỏe
(42, 4, 1, 250000), (42, 2, 1, 150000), -- booking_id 42: Tiêm phòng + Cắt lông
(43, 1, 1, 100000), -- booking_id 43: Tắm
(44, 5, 1, 50000), -- booking_id 44: Gửi trông
(45, 2, 1, 150000), (45, 3, 1, 200000), -- booking_id 45: Cắt lông + Khám sức khỏe
(46, 5, 3, 50000), -- booking_id 46: Gửi trông (3 lần)
-- Ngày 4/5/2025
(47, 1, 1, 100000), -- booking_id 47: Tắm
(48, 2, 1, 150000), -- booking_id 48: Cắt lông
(49, 3, 1, 200000), -- booking_id 49: Khám sức khỏe
(50, 2, 1, 150000), (50, 1, 1, 100000), -- booking_id 50: Cắt lông + Tắm
(51, 5, 2, 50000), -- booking_id 51: Gửi trông (2 lần)
(52, 1, 1, 100000), -- booking_id 52: Tắm
(53, 3, 1, 200000), -- booking_id 53: Khám sức khỏe
(54, 4, 1, 250000), -- booking_id 54: Tiêm phòng
(55, 1, 1, 100000), (55, 2, 1, 150000), -- booking_id 55: Tắm + Cắt lông
(56, 5, 1, 50000), -- booking_id 56: Gửi trông
(57, 2, 1, 150000), (57, 3, 1, 200000), -- booking_id 57: Cắt lông + Khám sức khỏe
(58, 4, 1, 250000), -- booking_id 58: Tiêm phòng
-- Ngày 5/5/2025
(59, 1, 1, 100000), -- booking_id 59: Tắm
(60, 2, 1, 150000), -- booking_id 60: Cắt lông
(61, 3, 1, 200000), (61, 1, 1, 100000), -- booking_id 61: Khám sức khỏe + Tắm
(62, 2, 1, 150000), -- booking_id 62: Cắt lông
(63, 5, 2, 50000), -- booking_id 63: Gửi trông (2 lần)
(64, 1, 1, 100000), -- booking_id 64: Tắm
(65, 3, 1, 200000), -- booking_id 65: Khám sức khỏe
(66, 4, 1, 250000), (66, 2, 1, 150000), -- booking_id 66: Tiêm phòng + Cắt lông
(67, 1, 1, 100000), -- booking_id 67: Tắm
(68, 5, 1, 50000), -- booking_id 68: Gửi trông
(69, 2, 1, 150000), (69, 3, 1, 200000), -- booking_id 69: Cắt lông + Khám sức khỏe
(70, 5, 3, 50000); -- booking_id 70: Gửi trông (3 lần)

INSERT INTO `order` (order_id, customer_id, order_date, total_amount, status, voucher_code) VALUES
(1, 21, '2025-04-01', 200000, 'PENDING', NULL),
(2, 22, '2025-04-02', 150000, 'COMPLETED', 'KM3'),
(3, 13, '2025-04-03', 300000, 'CANCELLED', NULL),
(4, 14, '2025-04-04', 100000, 'PENDING', NULL),
(5, 15, '2025-04-05', 180000, 'COMPLETED', NULL),

-- Ngày 1/5/2025
(6, 15, '2025-05-01 10:00:00', 300000, 'COMPLETED', NULL),
(7, 20, '2025-05-01 16:00:00', 250000, 'COMPLETED', 'KM1'),
(8, 13, '2025-05-01 19:00:00', 400000, 'COMPLETED', NULL),
-- Ngày 2/5/2025
(9, 17, '2025-05-02 10:00:00', 200000, 'COMPLETED', NULL),
(10, 22, '2025-05-02 16:00:00', 150000, 'COMPLETED', 'KM2'),
(11, 13, '2025-05-02 19:00:00', 250000, 'COMPLETED', NULL),
-- Ngày 3/5/2025
(12, 19, '2025-05-03 10:00:00', 300000, 'COMPLETED', NULL),
(13, 14, '2025-05-03 16:00:00', 400000, 'COMPLETED', 'KM4'),
(14, 15, '2025-05-03 19:00:00', 350000, 'COMPLETED', NULL),
-- Ngày 4/5/2025
(15, 21, '2025-05-04 10:00:00', 200000, 'COMPLETED', NULL),
(16, 16, '2025-05-04 16:00:00', 250000, 'COMPLETED', NULL),
(17, 19, '2025-05-04 19:00:00', 350000, 'COMPLETED', 'KM1'),
-- Ngày 5/5/2025
(18, 13, '2025-05-05 10:00:00', 300000, 'COMPLETED', NULL),
(19, 18, '2025-05-05 16:00:00', 400000, 'COMPLETED', 'KM2'),
(20, 21, '2025-05-05 19:00:00', 350000, 'COMPLETED', NULL),
(21, 15, '2025-05-05 13:30:00', 100000, 'PENDING', NULL);
INSERT INTO order_detail (order_detail_id, order_id, service_id, quantity, price) VALUES
-- Dữ liệu ban đầu (đã sửa giá cho đúng với bảng service)
(1, 1, 1, 1, 100000),
(2, 1, 2, 1, 150000),
(3, 2, 1, 1, 100000),
(4, 3, 3, 1, 200000),
(5, 4, 4, 1, 250000),
(6, 5, 5, 2, 50000),
-- Ngày 1/5/2025 (đã sửa số cột và giá)
(7, 6, 3, 1, 200000),
(8, 6, 1, 1, 100000),
(9, 7, 4, 1, 250000),
(10, 8, 2, 1, 150000),
(11, 8, 4, 1, 250000),
-- Ngày 2/5/2025
(12, 9, 3, 1, 200000),
(13, 10, 2, 1, 150000),
(14, 11, 1, 1, 100000),
(15, 11, 2, 1, 150000),
-- Ngày 3/5/2025
(16, 12, 3, 1, 200000),
(17, 12, 1, 1, 100000),
(18, 13, 4, 1, 250000),
(19, 13, 2, 1, 150000),
(20, 14, 2, 1, 150000),
(21, 14, 3, 1, 200000),
-- Ngày 4/5/2025
(22, 15, 3, 1, 200000),
(23, 16, 4, 1, 250000),
(24, 17, 2, 1, 150000),
(25, 17, 3, 1, 200000),
-- Ngày 5/5/2025
(26, 18, 3, 1, 200000),
(27, 18, 1, 1, 100000),
(28, 19, 4, 1, 250000),
(29, 19, 2, 1, 150000),
(30, 20, 2, 1, 150000),
(31, 20, 3, 1, 200000),
(32, 21, 5, 2, 50000);
INSERT INTO invoice (order_id, payment_date, subtotal, discount_percent, discount_amount, points_used, promotion_code, total, amount_paid, payment_method, status, staff_id, note) VALUES
-- Dữ liệu từ 1/4/2025 đến 5/4/2025 (duplicates removed)
(1, '2025-04-01 10:15:00', 250000, 0, 0, 0, NULL, 250000, 0, 'CASH', 'PENDING', 9, NULL),
(2, '2025-04-02 14:15:00', 100000, 0, 0, 0, NULL, 100000, 100000, 'CARD', 'COMPLETED', 10, 'KM3 không hiệu lực'),
(3, '2025-04-03 09:45:00', 200000, 0, 0, 0, NULL, 200000, 0, 'CASH', 'CANCELLED', 9, NULL),
(4, '2025-04-04 16:15:00', 250000, 0, 0, 0, NULL, 250000, 0, 'MOMO', 'PENDING', 10, NULL),
(5, '2025-04-05 11:15:00', 100000, 0, 0, 0, NULL, 100000, 100000, 'BANKING', 'COMPLETED', 9, NULL),
-- Ngày 1/5/2025 (status set to PENDING)
(6, '2025-05-01 10:15:00', 300000, 0, 0, 0, NULL, 300000, 300000, 'CASH', 'PENDING', 10, NULL),
(7, '2025-05-01 16:15:00', 250000, 10, 25000, 0, 'KM1', 225000, 225000, 'CARD', 'PENDING', 9, NULL),
(8, '2025-05-01 19:15:00', 400000, 0, 0, 0, NULL, 400000, 400000, 'MOMO', 'PENDING', 10, NULL),
-- Ngày 2/5/2025 (status set to PENDING)
(9, '2025-05-02 10:15:00', 200000, 0, 0, 0, NULL, 200000, 200000, 'BANKING', 'PENDING', 9, NULL),
(10, '2025-05-02 16:15:00', 150000, 20, 30000, 0, 'KM2', 120000, 120000, 'CASH', 'PENDING', 10, NULL),
(11, '2025-05-02 19:15:00', 250000, 0, 0, 0, NULL, 250000, 250000, 'CARD', 'PENDING', 9, NULL),
-- Ngày 3/5/2025 (status set to PENDING)
(12, '2025-05-03 10:15:00', 300000, 0, 0, 0, NULL, 300000, 300000, 'MOMO', 'PENDING', 10, NULL),
(13, '2025-05-03 16:15:00', 400000, 0, 0, 0, NULL, 400000, 400000, 'BANKING', 'PENDING', 9, 'KM4 không hiệu lực'),
(14, '2025-05-03 19:15:00', 350000, 0, 0, 0, NULL, 350000, 350000, 'CASH', 'PENDING', 10, NULL),
-- Ngày 4/5/2025 (status set to PENDING)
(15, '2025-05-04 10:15:00', 200000, 0, 0, 0, NULL, 200000, 200000, 'CARD', 'PENDING', 9, NULL),
(16, '2025-05-04 16:15:00', 250000, 0, 0, 0, NULL, 250000, 250000, 'MOMO', 'PENDING', 10, NULL),
(17, '2025-05-04 19:15:00', 350000, 10, 35000, 0, 'KM1', 315000, 315000, 'BANKING', 'PENDING', 9, NULL),
-- Ngày 5/5/2025 (status set to PENDING)
(18, '2025-05-05 10:15:00', 300000, 0, 0, 0, NULL, 300000, 300000, 'CASH', 'PENDING', 10, NULL),
(19, '2025-05-05 16:15:00', 400000, 20, 80000, 0, 'KM2', 320000, 320000, 'CARD', 'PENDING', 9, NULL),
(20, '2025-05-05 19:15:00', 350000, 0, 0, 0, NULL, 350000, 350000, 'MOMO', 'PENDING', 10, NULL),
(21, '2025-05-05 13:45:00', 100000, 0, 0, 0, NULL, 100000, 0, 'BANKING', 'PENDING', 9, NULL);
-- Trong phần INSERT INTO work_schedule
INSERT INTO work_schedule (staff_id, work_date, shift, note, start_time, end_time, location, task) VALUES
-- Dữ liệu hiện có
(2, '2025-04-08', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(3, '2025-04-08', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(4, '2025-04-08', 'MORNING', 'ABSENT', '08:00:00', '12:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(5, '2025-04-08', 'EVENING', 'WORKING', '17:00:00', '21:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(6, '2025-04-09', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(7, '2025-04-09', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(8, '2025-04-09', 'EVENING', 'WORKING', '17:00:00', '21:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(9, '2025-04-09', 'MORNING', 'OFF', NULL, NULL, 'Store 1', NULL),
(10, '2025-04-09', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Thu ngân'),
(11, '2025-04-09', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Tiếp nhận khách'),
(1, '2025-05-10', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Quản lý hệ thống'),
(2, '2025-05-10', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(3, '2025-05-10', 'EVENING', 'WORKING', '17:00:00', '21:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(4, '2025-05-11', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(5, '2025-05-11', 'AFTERNOON', 'ABSENT', '13:00:00', '17:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(6, '2025-05-11', 'EVENING', 'WORKING', '17:00:00', '21:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(7, '2025-05-12', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(8, '2025-05-12', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(9, '2025-05-12', 'EVENING', 'WORKING', '17:00:00', '21:00:00', 'Store 1', 'Thu ngân'),
(12, '2025-05-12', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Tiếp nhận khách'),
-- Thêm lịch làm việc cho staff_id = 11 vào ngày 10/05/2025
(11, '2025-05-10', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Tiếp nhận khách'),
-- Ngày 1/5/2025
(2, '2025-05-01', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(3, '2025-05-01', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(4, '2025-05-01', 'EVENING', 'WORKING', '17:00:00', '21:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(5, '2025-05-01', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(6, '2025-05-01', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(7, '2025-05-01', 'EVENING', 'WORKING', '17:00:00', '21:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(9, '2025-05-01', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Thu ngân'),
(10, '2025-05-01', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Thu ngân'),
(11, '2025-05-01', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Tiếp nhận khách'),
(12, '2025-05-01', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Tiếp nhận khách'),
-- Ngày 2/5/2025
(2, '2025-05-02', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(3, '2025-05-02', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(4, '2025-05-02', 'EVENING', 'ABSENT', '17:00:00', '21:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(5, '2025-05-02', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(6, '2025-05-02', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(7, '2025-05-02', 'EVENING', 'WORKING', '17:00:00', '21:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(9, '2025-05-02', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Thu ngân'),
(10, '2025-05-02', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Thu ngân'),
(11, '2025-05-02', 'EVENING', 'WORKING', '17:00:00', '21:00:00', 'Store 1', 'Tiếp nhận khách'),
(12, '2025-05-02', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Tiếp nhận khách'),
-- Ngày 3/5/2025
(2, '2025-05-03', 'EVENING', 'WORKING', '17:00:00', '21:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(3, '2025-05-03', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(4, '2025-05-03', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(5, '2025-05-03', 'EVENING', 'WORKING', '17:00:00', '21:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(6, '2025-05-03', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(7, '2025-05-03', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(9, '2025-05-03', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Thu ngân'),
(10, '2025-05-03', 'EVENING', 'WORKING', '17:00:00', '21:00:00', 'Store 1', 'Thu ngân'),
(11, '2025-05-03', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Tiếp nhận khách'),
(12, '2025-05-03', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Tiếp nhận khách'),
-- Ngày 4/5/2025
(2, '2025-05-04', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(3, '2025-05-04', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(4, '2025-05-04', 'EVENING', 'WORKING', '17:00:00', '21:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(5, '2025-05-04', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(6, '2025-05-04', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(7, '2025-05-04', 'EVENING', 'WORKING', '17:00:00', '21:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(9, '2025-05-04', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Thu ngân'),
(10, '2025-05-04', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Thu ngân'),
(11, '2025-05-04', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Tiếp nhận khách'),
(12, '2025-05-04', 'EVENING', 'WORKING', '17:00:00', '21:00:00', 'Store 1', 'Tiếp nhận khách'),
-- Ngày 5/5/2025
(2, '2025-05-05', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(3, '2025-05-05', 'EVENING', 'WORKING', '17:00:00', '21:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(4, '2025-05-05', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(5, '2025-05-05', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(6, '2025-05-05', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(7, '2025-05-05', 'EVENING', 'WORKING', '17:00:00', '21:00:00', 'Store 1', 'Chăm sóc thú cưng'),
(9, '2025-05-05', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Thu ngân'),
(10, '2025-05-05', 'AFTERNOON', 'WORKING', '13:00:00', '17:00:00', 'Store 1', 'Thu ngân'),
(11, '2025-05-05', 'EVENING', 'WORKING', '17:00:00', '21:00:00', 'Store 1', 'Tiếp nhận khách'),
(12, '2025-05-05', 'MORNING', 'WORKING', '08:00:00', '12:00:00', 'Store 1', 'Tiếp nhận khách');
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
('UPDATE_PROFILE', 'Cập nhật thông tin tài khoản cá nhân');


UPDATE invoice 
SET promotion_code = NULL, 
    discount_percent = 0, 
    discount_amount = 0, 
    total = subtotal 
WHERE promotion_code IS NOT NULL;

CALL assign_permission_by_role(1);
CALL assign_permission_by_role(2);
CALL assign_permission_by_role(3);
CALL assign_permission_by_role(4);
CALL assign_permission_by_role(5);
CALL assign_permission_by_role(6);
CALL assign_permission_by_role(7);
CALL assign_permission_by_role(8);
CALL assign_permission_by_role(9);
CALL assign_permission_by_role(10);
CALL assign_permission_by_role(11);
CALL assign_permission_by_role(12);