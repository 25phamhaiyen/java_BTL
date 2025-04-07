-- Thêm vai trò
-- Xóa toàn bộ dữ liệu cũ (nếu cần)
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS order_detail;
DROP TABLE IF EXISTS invoice;
DROP TABLE IF EXISTS `order`;

-- Sau đó xóa staff
DROP TABLE IF EXISTS staff;

-- Tiếp tục xóa các bảng khác...
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS account;
DROP TABLE IF EXISTS person;
DROP TABLE IF EXISTS pet;
DROP TABLE IF EXISTS service;
DROP TABLE IF EXISTS typeservice;
DROP TABLE IF EXISTS typepet;
DROP TABLE IF EXISTS paymentstatus;
DROP TABLE IF EXISTS happenstatus;
DROP TABLE IF EXISTS `role`;
DROP TABLE IF EXISTS available_times;
SET FOREIGN_KEY_CHECKS = 1;
ALTER TABLE account AUTO_INCREMENT = 1;
ALTER TABLE `role` AUTO_INCREMENT = 1;
ALTER TABLE person AUTO_INCREMENT = 1;
-- Xóa toàn bộ dữ liệu cũ (nếu cần)
INSERT INTO `role` (RoleName) VALUES
('Quản trị viên'),
('Quản lý'),
('Nhân viên'),
('Khách hàng');

<<<<<<< HEAD
-- Thêm loại thú cưng (không phụ thuộc bảng nào)
=======
INSERT INTO `account` (`UN_Username`, `Password`, `Email`, `Role_ID`) VALUES
('admin', '$2a$10$bxB8Fa1fAwS7BZoplZQCQOyc6CQEMDixqUWZ1e6O/6QJRUVW5mYlG', 'admin@example.com', 2),
('manager', '$2a$10$eJP9dNXOqp/xPLlyttPmcO7VnmrrZLzqqt666Z9eqnxH1torpo7lO', 'manager@example.com', 1),
('employee', '$2a$10$bdWRUaqXS09UMFZClbom2uO7VgcJzDBqwWeLfc6t0NQr6mQ6sDzCe', 'employee@example.com', 3)
;

INSERT INTO `person` (`lastName`, `firstName`, `phoneNumber`, `sex`, `citizenNumber`, `address`, `email`) VALUES
('Nguyen', 'Anh', '0123456789', 0, '123456789012', '123 Mai Dịch', ''),
('Tran', 'Hoa', '0987654321', 1, '098765432109', '456 Le Duan', ''),
('Phạm', 'Hải Yến', '0112233445', 1, '012345678912', '123 Lê Văn Việt', 'phy@gmail.com');

INSERT INTO `customer` (`PersonID`, `AccountID`, `registrationDate`, `loyaltyPoints`) VALUES
(1, 1, '2025-04-01', 100),
(2, 3, '2025-04-02', 50);

INSERT INTO `staff` (`PersonID`, `Role_ID`, `AccountID`, `startDate`, `salary`, `workShift`, `position`) VALUES
(1, 2, 1, '2025-04-01', 10000000, 'FullDay', 'Admin'),
(2, 3, 3, '2025-04-02', 7000000, 'FullDay', 'Cashier'),
(3, 1, 2, '2025-04-01', 15000000, 'FullDay', 'Manager');

INSERT INTO happenstatus (UN_StatusCode, StatusName) VALUES
(1, 'PENDING'),
(2, 'PROCESSING'),
(3, 'COMPLETED');

INSERT INTO paymentstatus (UN_StatusCode, StatusName) VALUES
(1, 'PENDING'),
(2, 'Paid'),
(3, 'Failed');

INSERT INTO typeservice (UN_TypeName) VALUES
('Tắm vệ sinh'),
('Cắt tỉa lông'),
('Dịch vụ y tế'),
('Huấn luyện thú cưng');

INSERT INTO service (serviceName, CostPrice, TypeServiceID, MoTa) VALUES
('Tắm rửa cơ bản', 100000, 1, 'Dịch vụ tắm rửa cơ bản cho thú cưng'),
('Cắt tỉa lông chó nhỏ', 150000, 2, 'Cắt tỉa lông cho chó dưới 10kg'),
('Tiêm phòng dại', 200000, 3, 'Tiêm vaccine phòng bệnh dại'),
('Huấn luyện nghe lời', 500000, 4, 'Huấn luyện chó vâng lời chủ'),
('Dịch vụ spa', 250000, 1, 'Tắm, massage và dưỡng lông');

>>>>>>> 7825721005ffadda6888e0b4b26ca916c967d630
INSERT INTO typepet (UN_TypeName) VALUES
('Chó'),
('Mèo'),
('Chim'),
('Thỏ'),
('Bò sát');

-- Thêm trạng thái đơn hàng (không phụ thuộc)
INSERT INTO happenstatus (UN_StatusCode, StatusName) VALUES
(1, 'Đang chờ'),
(2, 'Đang xử lý'),
(3, 'Đã hoàn thành'),
(5, 'Đang tiến hành'),
(6, 'Chờ xác nhận');

-- Thêm trạng thái thanh toán (không phụ thuộc)
TRUNCATE TABLE paymentstatus;
INSERT INTO paymentstatus (PaymentStatusID, UN_StatusCode, StatusName) VALUES
(1, 1, 'Chưa thanh toán'),
(2, 2, 'Đã thanh toán'),
(3, 3, 'Thanh toán thất bại'),
(4, 4, 'Chờ thanh toán'),
(5, 5, 'Hoàn tiền');


-- Thêm loại dịch vụ (phải có trước khi thêm service)
INSERT INTO typeservice (UN_TypeName) VALUES
('Tắm vệ sinh'),
('Cắt tỉa lông'),
('Dịch vụ y tế'),
('Huấn luyện thú cưng');

-- Thêm tài khoản mới (không trùng lặp)
INSERT INTO `account` (`AccountID`, `UN_Username`, `Password`, `Email`, `Role_ID`) VALUES 
(1, 'admin_pet', 'abcd', 'admin.pet@example.com', 1),
(2, 'manager_pet', 'abcd', 'manager.pet@example.com', 2),
(3, 'staff_pet1', 'abcd', 'staff1.pet@example.com', 3),
(4, 'customer_pet1', 'abcd', 'customer1.pet@example.com', 4),
(5, 'customer_pet2', 'abcd', 'customer2.pet@example.com', 4),
(6, 'customer_pet3', 'abcd', 'customer3.pet@example.com', 4),
(7, 'customer_pet4', 'abcd', 'customer4.pet@example.com', 4),
(8, 'customer_pet5', 'abcd', 'customer5.pet@example.com', 4),
(9, 'customer_pet6', 'abcd', 'customer6.pet@example.com', 4),
(10, 'customer_pet7', 'abcd', 'customer7.pet@example.com', 4),
(11, 'customer_pet8', 'abcd', 'customer8.pet@example.com', 4),
(12, 'customer_pet9', 'abcd', 'customer9.pet@example.com', 4),
(13, 'customer_pet10', 'abcd', 'customer10.pet@example.com', 4),
(14, 'customer_pet11', 'abcd', 'customer11.pet@example.com', 4),
(15, 'customer_pet12', 'abcd', 'customer12.pet@example.com', 4),
(16, 'customer_pet13', 'abcd', 'customer13.pet@example.com', 4),
(17, 'customer_pet14', 'abcd', 'customer14.pet@example.com', 4),
(18, 'customer_pet15', 'abcd', 'customer15.pet@example.com', 4);
-- Thêm thông tin cá nhân (phải có trước customer và staff)
INSERT INTO `person` (`PersonID`, `lastName`, `firstName`, `phoneNumber`, `sex`, `citizenNumber`, `address`) VALUES
(1, 'Nguyễn', 'Quản Trị', '0987654321', 1, '123456789012', '123 Đường ABC, Q.1, TP.HCM'),
(2, 'Lê', 'Nhân Viên', '0923456789', 2, '345678901234', '789 Đường DEF, Q.3, TP.HCM'),
(3, 'Phạm', 'Khách Hàng A', '0934567890', 1, '456789012345', '321 Đường GHI, Q.4, TP.HCM'),
(4, 'Hoàng', 'Khách Hàng B', '0945678901', 2, '567890123456', '654 Đường KLM, Q.5, TP.HCM'),
(5, 'Lý', 'Khách Hàng C', '0956789012', 1, '678901234567', '123 Đường NOP, Q.6, TP.HCM'),
(6, 'Trịnh', 'Khách Hàng D', '0967890123', 2, '789012345678', '456 Đường QRS, Q.7, TP.HCM'),
(7, 'Võ', 'Khách Hàng E', '0978901234', 1, '890123456789', '789 Đường TUV, Q.8, TP.HCM'),
(8, 'Đặng', 'Khách Hàng F', '0989012345', 2, '901234567890', '321 Đường WXY, Q.9, TP.HCM'),
(9, 'Ngô', 'Văn An', '0901234567', 1, '123451234567', '123 Đường Lê Lợi, Q.1, TP.HCM'),
(10, 'Đinh', 'Thị Bình', '0912345679', 2, '234562345678', '456 Đường Nguyễn Huệ, Q.1, TP.HCM'),
(11, 'Lâm', 'Văn Chính', '0923456799', 1, '345673456789', '789 Đường Lê Lai, Q.3, TP.HCM'),
(12, 'Dương', 'Thị Diệu', '0934567899', 2, '456784567890', '101 Đường Cách Mạng Tháng 8, Q.3, TP.HCM'),
(13, 'Mai', 'Văn Em', '0945678902', 1, '567895678901', '202 Đường Nguyễn Trãi, Q.5, TP.HCM'),
(14, 'Hồ', 'Thị Phúc', '0956789013', 2, '678906789012', '303 Đường 3/2, Q.10, TP.HCM'),
(15, 'Vũ', 'Văn Giang', '0967890124', 1, '789017890123', '404 Đường Lý Thường Kiệt, Q.11, TP.HCM'),
(16, 'Phan', 'Thị Hảo', '0978901235', 2, '890128901234', '505 Đường Quang Trung, Gò Vấp, TP.HCM'),
(17, 'Nguyễn', 'Văn Mười Bảy', '0978901236', 1, '890128901235', '506 Đường Quang Trung, Gò Vấp, TP.HCM'),
(18, 'Trần', 'Thị Mười Tám', '0978901237', 2, '890128901236', '507 Đường Quang Trung, Gò Vấp, TP.HCM');
TRUNCATE TABLE person;
-- Thêm khách hàng (phụ thuộc person và account)
INSERT INTO `customer` (`PersonID`, `AccountID`, `registrationDate`, `loyaltyPoints`) VALUES
(3, 3, '2024-01-01', 100),
(4, 4, '2024-01-01', 100),
(5, 5, '2024-01-15', 50),
(6, 6, '2024-02-01', 75),
(7, 7, '2024-02-15', 120),
(8, 8, '2024-03-01', 200),
(9, 9, '2024-03-15', 50),
(10, 10, '2024-04-01', 150),
(11, 11, '2024-01-10', 90),
(12, 12, '2024-01-20', 130),
(13, 13, '2024-02-05', 70),
(14, 14, '2024-02-20', 220),
(15, 15, '2024-03-05', 180),
(16, 16, '2024-03-20', 60),
(17, 17, '2024-04-05', 95),
(18, 18, '2024-04-20', 150);

-- Thêm nhân viên (phụ thuộc person, role và account)
INSERT INTO `staff` (`PersonID`, `Role_ID`, `AccountID`, `startDate`, `salary`, `workShift`, `position`) VALUES
(1, 1, 1, '2023-01-01', 15000000, 'Morning', 'Quản trị hệ thống'),
(2, 2, 2, '2023-02-01', 12000000, 'Afternoon', 'Quản lý cửa hàng'),
(3, 3, 3, '2023-03-01', 8000000, 'Evening', 'Nhân viên chăm sóc');

-- Thêm dịch vụ (phụ thuộc typeservice)
INSERT INTO service (serviceName, CostPrice, TypeServiceID, MoTa) VALUES
('Tắm rửa cơ bản', 100000, 1, 'Dịch vụ tắm rửa cơ bản cho thú cưng'),
('Tắm spa cao cấp', 250000, 1, 'Tắm, massage và dưỡng lông'),
('Cắt tỉa lông chó nhỏ', 150000, 2, 'Cắt tỉa lông cho chó dưới 10kg'),
('Cắt tỉa lông mèo', 180000, 2, 'Cắt tỉa lông cho mèo'),
('Tiêm phòng dại', 200000, 3, 'Tiêm vaccine phòng bệnh dại'),
('Khám tổng quát', 300000, 3, 'Khám sức khỏe định kỳ'),
('Huấn luyện cơ bản', 500000, 4, 'Huấn luyện chó vâng lời chủ');

-- Thêm thú cưng (phụ thuộc customer và typepet)
INSERT INTO pet (PetName, age, Customer_ID, TypePetID) VALUES
('Buddy', 3, 4, 1),
('Milo', 2, 4, 2),
('Bella', 4, 5, 1),
('Coco', 1, 5, 3),
('Luna', 5, 4, 4),
('Rocky', 2, 6, 1),
('Kitty', 3, 6, 2),
('Max', 5, 7, 1),
('Charlie', 1, 8, 3),
('Lucy', 4, 8, 2),
('Loki', 2, 9, 4),
('Thor', 3, 9, 1),
('Peanut', 1, 10, 2),
('Shadow', 6, 10, 5),
('Bolt', 2, 11, 1),
('Whiskers', 3, 11, 2),
('Rex', 4, 12, 1),
('Snowball', 1, 12, 2),
('Tweety', 2, 13, 3),
('Bugs', 3, 13, 4),
('Lizzy', 2, 14, 5),
('King', 5, 14, 1),
('Queen', 4, 15, 2),
('Goldie', 1, 15, 3),
('Spike', 3, 16, 1),
('Cotton', 2, 16, 4),
('Scales', 4, 17, 5),
('Hunter', 2, 17, 1),
('Fluffy', 1, 18, 2),
('Blue', 2, 18, 3);

-- Thêm đơn hàng (phụ thuộc customer, staff và happenstatus)
TRUNCATE TABLE `order`;
INSERT INTO `order` (orderID, orderDate, appointmentDate, orderType, Total, Customer_ID, StaffID, HappenStatusID, Note) VALUES 
(1, NOW(), DATE_ADD(NOW(), INTERVAL 2 DAY), 'Appointment', 350000, 4, 3, 1, NULL), 
(2, NOW(), NULL, 'AtStore', 150000, 5, 3, 2, NULL), 
(3, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 'Appointment', 500000, 4, 2, 3, NULL), 
(4, NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), 'Appointment', 250000, 6, 3, 1, 'Khách yêu cầu nhẹ tay với chó'), 
(5, NOW(), NULL, 'AtStore', 180000, 7, 3, 2, NULL), 
(6, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 'Appointment', 300000, 8, 2, 3, 'Dị ứng với nước ấm'), 
(7, DATE_SUB(NOW(), INTERVAL 2 DAY), NULL, 'AtStore', 500000, 9, 3, 3, NULL), 
(8, NOW(), DATE_ADD(NOW(), INTERVAL 3 DAY), 'Appointment', 350000, 10, 2, 1, 'Cần giữ mèo tránh xa các thú khác'),
(9, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), 'Appointment', 250000, 11, 3, 3, 'Khách yêu cầu phục vụ nhanh'), 
(10, DATE_SUB(NOW(), INTERVAL 6 DAY), NULL, 'AtStore', 180000, 12, 3, 3, NULL), 
(11, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), 'Appointment', 350000, 13, 2, 3, 'Chó hay cắn người lạ'), 
(12, DATE_SUB(NOW(), INTERVAL 4 DAY), NULL, 'AtStore', 300000, 14, 3, 3, NULL), 
(13, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 'Appointment', 450000, 15, 2, 3, 'Mèo nhút nhát, cần nhẹ nhàng'), 
(14, DATE_SUB(NOW(), INTERVAL 2 DAY), NULL, 'AtStore', 500000, 16, 3, 2, NULL), 
(15, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY), 'Appointment', 280000, 17, 2, 1, 'Cần giữ vệ sinh sạch sẽ'), 
(16, NOW(), NULL, 'AtStore', 150000, 18, 3, 1, NULL), 
(17, NOW(), DATE_ADD(NOW(), INTERVAL 2 DAY), 'Appointment', 350000, 11, 2, 1, 'Thú cưng cần được giữ ấm'), 
(18, NOW(), DATE_ADD(NOW(), INTERVAL 3 DAY), 'Appointment', 400000, 12, 3, 1, 'Chó dễ bị kích động, cần cẩn thận'), 
(19, NOW(), DATE_ADD(NOW(), INTERVAL 4 DAY), 'Appointment', 300000, 13, 2, 1, 'Đã tiêm vaccine đầy đủ'), 
(20, NOW(), DATE_ADD(NOW(), INTERVAL 5 DAY), 'Appointment', 250000, 14, 3, 1, 'Thú cưng bị dị ứng thức ăn'), 
(21, NOW(), DATE_ADD(NOW(), INTERVAL 6 DAY), 'Appointment', 420000, 15, 2, 1, 'Cần chất lượng cao nhất'), 
(22, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 'Appointment', 380000, 16, 3, 1, 'Đã sử dụng dịch vụ nhiều lần'),
(23, DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, 'AtStore', 200000, 4, 3, 2, 'Khách hàng VIP, ưu tiên phục vụ'),
(24, DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, 'AtStore', 350000, 5, 3, 2, 'Cần tư vấn thêm về dinh dưỡng'),
(25, DATE_SUB(NOW(), INTERVAL 2 DAY), NULL, 'AtStore', 450000, 6, 2, 3, 'Thú cưng cần được tiêm vắc-xin đầy đủ'),
(26, DATE_SUB(NOW(), INTERVAL 2 DAY), NULL, 'AtStore', 280000, 7, 2, 3, NULL),
(27, DATE_SUB(NOW(), INTERVAL 3 DAY), NULL, 'AtStore', 950000, 8, 3, 3, 'Gói chăm sóc toàn diện'),
(28, DATE_SUB(NOW(), INTERVAL 3 DAY), NULL, 'AtStore', 1200000, 9, 3, 3, 'Phẫu thuật thẩm mỹ cho thú cưng'),
(29, DATE_SUB(NOW(), INTERVAL 4 DAY), NULL, 'AtStore', 820000, 10, 2, 3, 'Điều trị bệnh da');
ALTER TABLE `order` 
ADD COLUMN IF NOT EXISTS `PetID` INT UNSIGNED NULL DEFAULT NULL,
ADD CONSTRAINT `FK_order_pet` FOREIGN KEY (`PetID`) REFERENCES `pet` (`PetID`) 
ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `order` 
CHANGE COLUMN `Total` `totalAmount` DOUBLE NOT NULL DEFAULT 0;

-- Thêm chi tiết đơn hàng (phụ thuộc order và service)
TRUNCATE TABLE order_detail;
INSERT INTO order_detail (OrderID, ServiceID, Quantity, UnitPrice) VALUES
(1, 1, 1, 100000),
(1, 2, 1, 250000),
(2, 3, 1, 150000),
(3, 5, 1, 200000),
(3, 7, 1, 300000),
(4, 2, 1, 250000),
(5, 4, 1, 180000),
(6, 6, 1, 300000),
(7, 7, 1, 500000),
(8, 1, 1, 100000),
(8, 2, 1, 250000),
(9, 1, 1, 100000),
(9, 3, 1, 150000),
(10, 2, 1, 180000),
(11, 5, 1, 200000),
(11, 3, 1, 150000),
(12, 6, 1, 300000),
(13, 7, 1, 500000),
(14, 4, 1, 180000),
(15, 1, 1, 100000),
(15, 2, 1, 250000),
(16, 5, 1, 200000),
(16, 6, 1, 100000),
(17, 3, 1, 150000),
(17, 1, 1, 100000),
(18, 4, 1, 180000),
(19, 2, 1, 250000),
(19, 6, 1, 100000),
(20, 7, 1, 300000),
(20, 1, 1, 100000),
(21, 3, 1, 150000),
(21, 5, 1, 150000),
(22, 2, 1, 250000),
(23, 2, 1, 150000),
(23, 5, 1, 50000),
(24, 3, 1, 150000),
(24, 2, 1, 200000),
(25, 5, 1, 200000),
(25, 6, 1, 250000),
(26, 4, 1, 280000),
(27, 7, 1, 500000),
(27, 6, 1, 300000),
(27, 2, 1, 150000),
(28, 7, 2, 500000),
(28, 5, 1, 200000),
(29, 6, 2, 300000),
(29, 5, 1, 220000);
-- Thêm hóa đơn (phụ thuộc order và paymentstatus)
INSERT INTO invoice (InvoiceID, OrderID, Total, PaymentStatusID, PaymentMethod) VALUES 
(1, 1, 350000, 2, 'Tiền mặt'), 
(2, 2, 150000, 2, 'Tiền mặt'), 
(3, 3, 500000, 2, 'Chuyển khoản'), 
(4, 4, 250000, 2, 'Tiền mặt'), 
(5, 5, 180000, 2, 'Chuyển khoản'), 
(6, 6, 300000, 2, 'Tiền mặt'),
(7, 7, 500000, 2, 'Chuyển khoản'),
(8, 8, 350000, 2, 'Tiền mặt'),
(9, 9, 250000, 2, 'Tiền mặt'),
(10, 10, 180000, 2, 'Chuyển khoản'),
(11, 11, 350000, 3, 'Chuyển khoản'),
(12, 12, 300000, 3, 'Chuyển khoản'),
(13, 13, 450000, 4,'Chuyển khoản' ),
(14, 14, 500000, 4, 'Chuyển khoản'),
(15, 15, 280000, 5, 'Tiền mặt'),
(16, 16, 150000, 5, 'Chuyển khoản'),
-- Hóa đơn chưa thanh toán (PENDING)
(17, 17, 350000, 1, 'Chuyển khoản'),
(18, 18, 400000, 1, 'Chuyển khoản'),
(19, 19, 300000, 1, 'Chuyển khoản'),
(20, 20, 250000, 1, 'Chuyển khoản'),
(21, 21, 420000, 1, 'Chuyển khoản'),
(22, 22, 380000, 1, 'Chuyển khoản'),
(23, 23, 200000, 1, 'Tiền mặt'),
(24, 24, 350000, 1, 'Tiền mặt'),
(25, 25, 450000, 4, 'Tiền mặt'),
(26, 26, 280000, 4, 'Tiền mặt'),
(27, 27, 950000, 1, 'Tiền mặt'),
(28, 28, 1200000, 2, 'Chuyển khoản'),
(29, 29, 820000, 1, 'Tiền mặt');
INSERT INTO available_times (TimeSlot) VALUES
('08:00'), ('09:00'), ('10:00'), ('11:00'),
('13:00'), ('14:00'), ('15:00'), ('16:00'), ('17:00');
SET FOREIGN_KEY_CHECKS = 1;
ALTER TABLE `invoice` ADD COLUMN `PaymentMethod` VARCHAR(50) NULL;
UPDATE invoice SET PaymentStatusID = 3 WHERE InvoiceID = 6;
UPDATE `order` SET HappenStatusID = 6 WHERE orderID = 17;
UPDATE `order` SET HappenStatusID = 5 WHERE orderID = 14;
SELECT * FROM invoice WHERE InvoiceID = 11;
