DELETE FROM account_permission;
DELETE FROM booking_detail;
DELETE FROM order_detail;
DELETE FROM invoice;
DELETE FROM booking;
DELETE FROM pet;
DELETE FROM `order`;
DELETE FROM work_schedule;
DELETE FROM staff;
DELETE FROM customer;
DELETE FROM account;

-- Xóa dữ liệu từ các bảng không có phụ thuộc hoặc bảng cha
DELETE FROM pet_type;
DELETE FROM service;
DELETE FROM promotion;
DELETE FROM permission;
DELETE FROM role;


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
(12, 'reception02', '$2a$10$Jg2R.bSJNMiMZPhxW6wIr.JZn5n0hW7QoqfKB3ehaK94SnSIgFDOq', 4); -- pass: 123
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
(13, 0),
(14, 0),
(15, 0),
(16, 0),
(17, 0),
(18, 0),
(19, 0),
(20, 0),
(21, 0),
(22, 0);
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
(3, 'Khám sức khỏe', 'Kiểm tra tổng quát sức khỏe thú cưng', 200000, 30, true),
(4, 'Tiêm phòng', 'Tiêm các loại vaccine cơ bản', 250000, 15, true),
(5, 'Gửi trông thú cưng', 'Dịch vụ giữ thú cưng theo giờ', 50000, 60, true),
(6, 'Huấn luyện cơ bản', 'Dạy ngồi, đứng, bắt tay,...', 300000, 60, false);
INSERT INTO promotion (promotion_id, code, description, discount_percent, start_date, end_date, active) VALUES
(1, 'KM1', 'Giảm 10% cho tất cả các dịch vụ', 10, '2025-04-01', '2025-04-15', true),
(2, 'KM2', 'Chỉ áp dụng cho khách hàng lần đầu sử dụng', 20, '2025-04-01', '2025-04-30', true),
(3, 'KM3', 'Giảm 15% khi sử dụng combo 2 dịch vụ', 15, '2025-03-15', '2025-04-10', false),
(4, 'KM4', 'Giảm 5% toàn bộ đơn hàng >300k', 5, '2025-06-01', '2025-06-30', true);
INSERT INTO booking (customer_id, pet_id, staff_id, booking_time, status, note) VALUES
(13, 1, 11, '2024-04-01 14:27:51', 'COMPLETED', NULL),
(14, 2, 11, '2024-04-01 15:00:00', 'COMPLETED', NULL),
(15, 3, 12, '2025-04-03 09:30:00', 'COMPLETED', NULL),
(16, 4, 11, '2025-04-04 16:00:00', 'COMPLETED', NULL),
(17, 5, 11, '2025-04-05 11:00:00', 'COMPLETED', NULL),
(18, 6, 12, '2025-04-06 15:00:00', 'CANCELLED', NULL),
(19, 7, 12, '2025-04-07 13:00:00', 'COMPLETED', NULL),
(20, 8, 12, '2025-04-08 08:30:00', 'COMPLETED', NULL),
(21, 9, 12, '2025-04-09 17:30:00', 'COMPLETED', NULL),
(22, 10, 11, '2025-04-10 10:45:00', 'COMPLETED', NULL);

CALL add_booking_detail(1, 1, 1, NULL);
CALL add_booking_detail(2, 2, 2, NULL);
CALL add_booking_detail(3, 3, 3, NULL);
CALL add_booking_detail(4, 4, 2, NULL);
CALL add_booking_detail(5, 5, 1, NULL);
CALL add_booking_detail(6, 6, 2, NULL);
CALL add_booking_detail(7, 1, 3, NULL);
CALL add_booking_detail(8, 2, 1, NULL);
CALL add_booking_detail(9, 3, 1, NULL);
CALL add_booking_detail(10, 4, 1, NULL);

CALL create_order_from_booking(1, 11);
CALL create_order_from_booking(2, 11);
CALL create_order_from_booking(3, 12);
CALL create_order_from_booking(4, 11);
CALL create_order_from_booking(5, 11);
CALL create_order_from_booking(6, 12);
CALL create_order_from_booking(7, 12);
CALL create_order_from_booking(8, 12);
CALL create_order_from_booking(9, 12);
CALL create_order_from_booking(10, 11);

INSERT INTO work_schedule (schedule_id, staff_id, work_date, shift, note) VALUES
(1, 2, '2025-04-08', 'MORNING', 'WORKING'),
(2, 3, '2025-04-08', 'AFTERNOON', 'WORKING'),
(3, 4, '2025-04-08', 'MORNING', 'ABSENT'),
(4, 5, '2025-04-08', 'EVENING', 'WORKING'),
(5, 6, '2025-04-09', 'MORNING', 'WORKING'),
(6, 7, '2025-04-09', 'AFTERNOON', 'WORKING'),
(7, 8, '2025-04-09', 'EVENING', 'WORKING'),
(8, 9, '2025-04-09', 'MORNING', 'OFF'),
(9, 10, '2025-04-09', 'MORNING', 'WORKING'),
(10, 11, '2025-04-09', 'AFTERNOON', 'WORKING');
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

