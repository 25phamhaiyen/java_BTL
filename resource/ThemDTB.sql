INSERT INTO role (RoleName) VALUES
('Admin'),
('Nhân viên'),
('Khách hàng');
INSERT INTO account (UN_Username, Password, Email, Role_ID) VALUES
('admin01', 'adminpass', 'admin@example.com', 1),
('staff01', 'staffpass', 'staff01@example.com', 2),
('staff02', 'staffpass', 'staff02@example.com', 2),
('customer01', 'custpass', 'cust01@example.com', 3),
('customer02', 'custpass', 'cust02@example.com', 3);
INSERT INTO customer (lastName, firstName, phoneNumber, sex, citizenNumber, address, AccountID) VALUES
('Nguyễn', 'Minh Anh', '0912345678', 1, '123456789012', 'Hà Nội', 4),
('Trần', 'Hoàng Nam', '0923456789', 1, '234567890123', 'Hồ Chí Minh', 5);
INSERT INTO staff (lastName, firstName, Sex, phoneNumber, CitizenNumber, Address, Role_ID, AccountID) VALUES
('Phạm', 'Thanh Hùng', 1, '0934567890', '345678901234', 'Hà Nội', 2, 2),
('Lê', 'Thị Mai', 0, '0945678901', '456789012345', 'Đà Nẵng', 2, 3);
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

