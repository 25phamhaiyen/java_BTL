INSERT INTO `role` (RoleName) VALUES
('Quản lý'),
('Quản trị viên'),
('Nhân viên'),
('Khách hàng');

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
('VIP'),
('Cơ bản');

INSERT INTO service (serviceName, CostPrice, TypeServiceID, MoTa) VALUES
('Tắm rửa cơ bản', 100000, 1, 'Dịch vụ tắm rửa cơ bản cho thú cưng'),
('Cắt tỉa lông chó nhỏ', 150000, 1, 'Cắt tỉa lông cho chó dưới 10kg'),
('Tiêm phòng dại', 200000, 1, 'Tiêm vaccine phòng bệnh dại'),
('Huấn luyện nghe lời', 500000, 2, 'Huấn luyện chó vâng lời chủ'),
('Dịch vụ spa', 250000, 2, 'Tắm, massage và dưỡng lông');

INSERT INTO typepet (UN_TypeName) VALUES
('Chó'),
('Mèo'),
('Chim'),
('Thỏ'),
('Bò sát');

INSERT INTO pet (PetName, age, Customer_ID, TypePetID) VALUES
('Buddy', 3, 1, 1),
('Milo', 2, 1, 2),
('Bella', 4, 2, 1),
('Coco', 1, 2, 3),
('Luna', 5, 1, 4);

INSERT INTO `order` (orderDate, appointmentDate, orderType, Total, Customer_ID, StaffID, HappenStatusID) VALUES
('2024-03-20 10:00:00', '2024-03-22 10:30:00', 'Appointment', 350000, 1, 1, 1),
('2024-03-21 15:00:00', NULL, 'AtStore', 150000, 2, 2, 2);

INSERT INTO order_detail (OrderID, ServiceID, Quantity, UnitPrice) VALUES
(1, 1, 1, 100000),
(1, 5, 1, 250000),
(2, 2, 1, 150000);

INSERT INTO invoice (OrderID, TotalAmount, PaymentStatusID) VALUES
(1, 350000, 1),
(2, 150000, 2);

