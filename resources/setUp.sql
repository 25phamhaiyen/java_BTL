-- tạo db
DROP DATABASE IF EXISTS bestpets;
CREATE DATABASE bestpets
CHARACTER SET utf8mb4 COLLATE UTF8MB4_UNICODE_CI;

-- Sử dụng DB
USE bestpets;

-- Cài đặt
SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";




-- 				Tạo bảng Trạng thái thanh toán
CREATE TABLE `paymentstatus` (
	`PaymentStatusID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
	`UN_StatusCode` INT NOT NULL,
	`StatusName` VARCHAR(100) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	PRIMARY KEY (`PaymentStatusID`) USING BTREE,
	UNIQUE INDEX `UnPaymentStatus_UN_StatusCode` (`UN_StatusCode`) USING BTREE
)
COLLATE='utf8mb4_unicode_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1
;

-- 				Tạo bảng Trạng thái xảy ra
CREATE TABLE `happenstatus` (
	`HappenStatusID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
	`UN_StatusCode` INT UNSIGNED NOT NULL,
	`StatusName` VARCHAR(100) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	PRIMARY KEY (`HappenStatusID`) USING BTREE,
	UNIQUE INDEX `UnHappenStatus_UN_StatusCode` (`UN_StatusCode`) USING BTREE
)
COLLATE='utf8mb4_unicode_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1
;


--				Tạo bảng Vai trò
CREATE TABLE `role` (
	`Role_ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
	`RoleName` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	PRIMARY KEY (`Role_ID`) USING BTREE
)
COLLATE='utf8mb4_unicode_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1
;


--				Tạo bảng loại dịch vụ
CREATE TABLE `typeservice` (
	`TypeServiceID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
	`UN_TypeName` VARCHAR(200) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	PRIMARY KEY (`TypeServiceID`) USING BTREE,
	UNIQUE INDEX `UnTypeService_UN_TypeName` (`UN_TypeName`) USING BTREE
)
COLLATE='utf8mb4_unicode_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1
;

-- 				Tạo bảng Dịch vụ
CREATE TABLE `service` (
	`serviceID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
	`serviceName` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`CostPrice` DOUBLE NOT NULL DEFAULT '0',
	`TypeServiceID` INT UNSIGNED NOT NULL,
	`MoTa` TEXT NULL DEFAULT NULL COLLATE 'utf8mb4_unicode_ci',
	PRIMARY KEY (`serviceID`) USING BTREE,
	INDEX `FkService_TypeServiceID` (`TypeServiceID`) USING BTREE,
	CONSTRAINT `FkService_TypeServiceID` FOREIGN KEY (`TypeServiceID`) REFERENCES `typeservice` (`TypeServiceID`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COLLATE='utf8mb4_unicode_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1
;

-- 				Tạo bảng Tài khoản
CREATE TABLE `account` (
	`AccountID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
	`UN_Username` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`Password` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`Email` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8mb4_unicode_ci',
	`Role_ID` INT UNSIGNED NOT NULL,
	PRIMARY KEY (`AccountID`) USING BTREE,
	UNIQUE INDEX `UnAccount_UN_Username` (`UN_Username`) USING BTREE,
	INDEX `Role_ID` (`Role_ID`) USING BTREE,
	CONSTRAINT `FK_account_role` FOREIGN KEY (`Role_ID`) REFERENCES `role` (`Role_ID`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COLLATE='utf8mb4_unicode_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1
;

-- 				Tạo bảng Con người
CREATE TABLE `person` (
	`PersonID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
	`lastName` VARCHAR(50) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`firstName` VARCHAR(20) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`phoneNumber` VARCHAR(10) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`sex` TINYINT NOT NULL,
	`citizenNumber` VARCHAR(12) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`address` TEXT NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`email` TEXT NOT NULL COLLATE 'utf8mb4_unicode_ci',
	PRIMARY KEY (`PersonID`) USING BTREE,
	UNIQUE INDEX `Person_PhoneNumber` (`phoneNumber`) USING BTREE,
	UNIQUE INDEX `Person_CitizenNumber` (`citizenNumber`) USING BTREE,
	CONSTRAINT `CkPerson_CitizenNumber` CHECK ((length(`citizenNumber`) = 12)),
	CONSTRAINT `CkPerson_phoneNumber` CHECK ((length(`phoneNumber`) = 10))
)
COLLATE='utf8mb4_unicode_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1
;


-- 				Tạo bảng Khách hàng
CREATE TABLE `customer` (
    `PersonID` INT UNSIGNED NOT NULL,
    `AccountID` INT UNSIGNED NOT NULL,
    `registrationDate` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `loyaltyPoints` INT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (`PersonID`),
    INDEX `AccountID` (`AccountID`),
    CONSTRAINT `FK_customer_person` FOREIGN KEY (`PersonID`) REFERENCES `person` (`PersonID`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `FK_customer_account` FOREIGN KEY (`AccountID`) REFERENCES `account` (`AccountID`) ON DELETE NO ACTION ON UPDATE NO ACTION
)
COLLATE='utf8mb4_unicode_ci'
ENGINE=InnoDB;
-- 				Tạo bảng Nhân viên
CREATE TABLE `staff` (
    `PersonID` INT UNSIGNED NOT NULL,
    `Role_ID` INT UNSIGNED NOT NULL,
    `AccountID` INT UNSIGNED NOT NULL,
    `startDate` DATE DEFAULT NULL,
    `endDate` DATE DEFAULT NULL,
    `salary` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `workShift` VARCHAR(50) NOT NULL COLLATE 'utf8mb4_unicode_ci',
    `position` VARCHAR(100) NOT NULL COLLATE 'utf8mb4_unicode_ci',
    PRIMARY KEY (`PersonID`),
    INDEX `FkStaff_Role_ID` (`Role_ID`) USING BTREE,
    INDEX `AccountID` (`AccountID`) USING BTREE,
    CONSTRAINT `FK_staff_person` FOREIGN KEY (`PersonID`) REFERENCES `person` (`PersonID`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `FkStaff_Role_ID` FOREIGN KEY (`Role_ID`) REFERENCES `role` (`Role_ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT `FK_staff_account` FOREIGN KEY (`AccountID`) REFERENCES `account` (`AccountID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) 
COLLATE='utf8mb4_unicode_ci'
ENGINE=InnoDB;

-- 				Tạo bảng Đơn hàng
-- Tạo bảng Đơn hàng (order)
CREATE TABLE `order` (
	`orderID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
	`orderDate` DATETIME NOT NULL DEFAULT (CURRENT_TIMESTAMP),
	`appointmentDate` DATETIME NULL DEFAULT NULL,
	`orderType` ENUM('AtStore','Appointment') NOT NULL DEFAULT 'AtStore' COLLATE 'utf8mb4_unicode_ci',
	`Total` DOUBLE NOT NULL DEFAULT '0',
	`Customer_ID` INT UNSIGNED NOT NULL,  
	`StaffID` INT UNSIGNED NULL DEFAULT NULL,
	`HappenStatusID` INT UNSIGNED NOT NULL,
	PRIMARY KEY (`orderID`) USING BTREE,
	INDEX `Fkorder_Customer_ID` (`Customer_ID`) USING BTREE,
	INDEX `Fkorder_StaffID` (`StaffID`) USING BTREE,
	INDEX `Fkorder_PaymentStatusID` (`HappenStatusID`) USING BTREE,
	CONSTRAINT `Fkorder_Customer_ID` FOREIGN KEY (`Customer_ID`) REFERENCES `customer` (`PersonID`) ON UPDATE NO ACTION ON DELETE CASCADE,  -- Tham chiếu đến PersonID
	CONSTRAINT `Fkorder_StaffID` FOREIGN KEY (`StaffID`) REFERENCES `staff` (`PersonID`) ON UPDATE NO ACTION ON DELETE CASCADE,
	CONSTRAINT `FK_order_happenstatus` FOREIGN KEY (`HappenStatusID`) REFERENCES `happenstatus` (`HappenStatusID`) ON UPDATE NO ACTION ON DELETE CASCADE
)
COLLATE='utf8mb4_unicode_ci'
ENGINE=InnoDB;


-- Tạo bảng chi tiết đơn hàng (order_detail)
CREATE TABLE `order_detail` (
	`OrderDetailID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
	`OrderID` INT UNSIGNED NOT NULL,
	`ServiceID` INT UNSIGNED NOT NULL,
	`Quantity` INT UNSIGNED NOT NULL DEFAULT '1',
	`UnitPrice` DECIMAL(10,2) NOT NULL,
	`TotalPrice` DECIMAL(10,2) AS ((`Quantity` * `UnitPrice`)) stored,
	PRIMARY KEY (`OrderDetailID`) USING BTREE,
	INDEX `FkOrderDetail_OrderID` (`OrderID`) USING BTREE,
	INDEX `FkOrderDetail_ServiceID` (`ServiceID`) USING BTREE,
	CONSTRAINT `FkOrderDetail_OrderID` FOREIGN KEY (`OrderID`) REFERENCES `order` (`orderID`) ON UPDATE NO ACTION ON DELETE CASCADE,
	CONSTRAINT `FkOrderDetail_ServiceID` FOREIGN KEY (`ServiceID`) REFERENCES `service` (`serviceID`) ON UPDATE NO ACTION ON DELETE CASCADE,
	CONSTRAINT `order_detail_chk_1` CHECK ((`Quantity` > 0)),
	CONSTRAINT `order_detail_chk_2` CHECK ((`UnitPrice` >= 0))
)
COLLATE='utf8mb4_unicode_ci'
ENGINE=InnoDB
;

-- Tạo bảng hóa đơn (invoice)
CREATE TABLE `invoice` (
	`InvoiceID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
	`OrderID` INT UNSIGNED NOT NULL,
	`TotalAmount` DOUBLE NOT NULL DEFAULT '0',
	`CreatedAt` TIMESTAMP NULL DEFAULT (CURRENT_TIMESTAMP),
	`PaymentStatusID` INT UNSIGNED NOT NULL,
	PRIMARY KEY (`InvoiceID`) USING BTREE,
	UNIQUE INDEX `UnInvoice_OrderID` (`OrderID`) USING BTREE,
	INDEX `FkInvoice_OrderID` (`OrderID`) USING BTREE,
	INDEX `PaymentStatusID` (`PaymentStatusID`) USING BTREE,
	CONSTRAINT `FkInvoice_OrderID` FOREIGN KEY (`OrderID`) REFERENCES `order` (`orderID`) ON UPDATE NO ACTION ON DELETE CASCADE,
	CONSTRAINT `FK_invoice_paymentstatus` FOREIGN KEY (`PaymentStatusID`) REFERENCES `paymentstatus` (`PaymentStatusID`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COLLATE='utf8mb4_unicode_ci'
ENGINE=InnoDB
;

-- 				Tạo bảng loại thú cưng
CREATE TABLE `typepet` (
	`TypePetID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
	`UN_TypeName` VARCHAR(100) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	PRIMARY KEY (`TypePetID`) USING BTREE,
	UNIQUE INDEX `UnTypePet_UN_TypeName` (`UN_TypeName`) USING BTREE
)
COLLATE='utf8mb4_unicode_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1
;

-- 				Tạo bảng thú cưng
-- Tạo bảng thú cưng (pet)
CREATE TABLE `pet` (
	`PetID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
	`PetName` TEXT NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`age` TINYINT UNSIGNED NOT NULL,
	`Customer_ID` INT UNSIGNED NOT NULL,  -- Tham chiếu đến PersonID trong bảng customer
	`TypePetID` INT UNSIGNED NOT NULL,
	PRIMARY KEY (`PetID`) USING BTREE,
	INDEX `FkPet_CustomerID` (`Customer_ID`) USING BTREE,  -- Đảm bảo tên cột trùng với cột tham chiếu
	INDEX `FkParty_TypePetcustomerID` (`TypePetID`) USING BTREE,
	CONSTRAINT `FkParty_TypePetcustomerID` FOREIGN KEY (`TypePetID`) REFERENCES `typepet` (`TypePetID`) ON UPDATE NO ACTION ON DELETE NO ACTION,
	CONSTRAINT `FkPet_CustomerID` FOREIGN KEY (`Customer_ID`) REFERENCES `customer` (`PersonID`) ON UPDATE NO ACTION ON DELETE NO ACTION  -- Tham chiếu đến PersonID trong customer
)
COLLATE='utf8mb4_unicode_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1;

-- Bảng work_schedule – lịch làm việc nhân viên
CREATE TABLE work_schedule (
    scheduleID INT AUTO_INCREMENT PRIMARY KEY,
    staffID INT UNSIGNED NOT NULL,
    workDate DATE NOT NULL,
    shift ENUM('Morning', 'Afternoon', 'Evening') NOT NULL,
    note VARCHAR(255),
    FOREIGN KEY (staffID) REFERENCES staff(PersonID)  
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- Bảng promotion – khuyến mãi theo điểm tích lũy
CREATE TABLE promotion (
    promotionID INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `description` TEXT,
    requiredPoints INT NOT NULL,
    discountPercent DOUBLE NOT NULL CHECK (discountPercent >= 0 AND discountPercent <= 100),
    startDate DATE NOT NULL,
    endDate DATE NOT NULL,
    CONSTRAINT chk_date_range CHECK (endDate > startDate)
);

-- View dashboard_summary_by_month – tổng hợp số liệu theo tháng
CREATE VIEW dashboard_summary_by_month AS
SELECT 
    DATE_FORMAT(i.CreatedAt, '%Y-%m') AS month,
    COUNT(DISTINCT i.invoiceID) AS total_orders,
    COUNT(DISTINCT CASE 
        WHEN MONTH(c.registrationDate) = MONTH(i.CreatedAt) 
             AND YEAR(c.registrationDate) = YEAR(i.CreatedAt) 
        THEN c.PersonID
    END) AS new_customers,
    SUM(i.TotalAmount) AS total_revenue
FROM invoice i
JOIN `order` o ON i.orderID = o.orderID
JOIN customer c ON o.Customer_ID = c.PersonID
GROUP BY DATE_FORMAT(i.CreatedAt, '%Y-%m')
ORDER BY month;

-- áp dụng khuyến mãi vào hóa đơn
CREATE TABLE invoice_promotion (
    invoiceID INT UNSIGNED,
    promotionID INT UNSIGNED,
    discountApplied DOUBLE DEFAULT 0,
    PRIMARY KEY (invoiceID, promotionID),
    FOREIGN KEY (invoiceID) REFERENCES invoice(InvoiceID) ON DELETE CASCADE,
    FOREIGN KEY (promotionID) REFERENCES promotion(promotionID) ON DELETE CASCADE
);


DELIMITER //

CREATE TRIGGER after_order_detail_delete
AFTER DELETE ON order_detail
FOR EACH ROW
BEGIN
    UPDATE `order`
    SET Total = (SELECT COALESCE(SUM(TotalPrice), 0) FROM order_detail WHERE OrderID = OLD.OrderID)
    WHERE OrderID = OLD.OrderID;
END;

//

DELIMITER ;

DELIMITER $$

CREATE TRIGGER update_loyaltyPoints_after_invoice_insert
AFTER INSERT ON `invoice`
FOR EACH ROW
BEGIN
    DECLARE total_spent DOUBLE;

    -- Tính tổng tiền chi tiêu của khách hàng từ hóa đơn
    SET total_spent = NEW.TotalAmount;

    -- Cập nhật điểm tích lũy cho khách hàng
    UPDATE `customer`
    SET `loyaltyPoints` = `loyaltyPoints` + FLOOR(total_spent / 100) -- Giả sử mỗi 100 đơn vị tiền tệ = 1 điểm
    WHERE `PersonID` = (SELECT `Customer_ID` FROM `order` WHERE `orderID` = NEW.OrderID);
END $$

DELIMITER ;

DELIMITER $$

CREATE TRIGGER update_loyaltyPoints_after_invoice_update
AFTER UPDATE ON `invoice`
FOR EACH ROW
BEGIN
    DECLARE old_total_spent DOUBLE;
    DECLARE new_total_spent DOUBLE;

    -- Lấy tổng tiền chi tiêu cũ và mới
    SET old_total_spent = OLD.TotalAmount;
    SET new_total_spent = NEW.TotalAmount;

    -- Cập nhật điểm tích lũy cho khách hàng
    UPDATE `customer`
    SET `loyaltyPoints` = `loyaltyPoints` - FLOOR(old_total_spent / 100) + FLOOR(new_total_spent / 100)
    WHERE `PersonID` = (SELECT `Customer_ID` FROM `order` WHERE `orderID` = NEW.OrderID);
END $$

DELIMITER ;

DELIMITER $$

CREATE TRIGGER update_loyaltyPoints_after_invoice_delete
AFTER DELETE ON `invoice`
FOR EACH ROW
BEGIN
    DECLARE total_spent DOUBLE;

    -- Lấy tổng tiền chi tiêu của hóa đơn đã xóa
    SET total_spent = OLD.TotalAmount;

    -- Cập nhật điểm tích lũy cho khách hàng
    UPDATE `customer`
    SET `loyaltyPoints` = `loyaltyPoints` - FLOOR(total_spent / 100)
    WHERE `PersonID` = (SELECT `Customer_ID` FROM `order` WHERE `orderID` = OLD.OrderID);
END $$

DELIMITER ;

-- Trigger kiểm tra appointmentDate >= CURRENT_DATE() cho đơn hẹn lịch
DELIMITER $$

CREATE TRIGGER check_appointment_date_before_insert
BEFORE INSERT ON `order`
FOR EACH ROW
BEGIN
    IF NEW.orderType = 'Appointment' AND NEW.appointmentDate < CURRENT_DATE() THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Appointment date must be today or in the future.';
    END IF;
END $$

DELIMITER ;

-- Ràng buộc endDate > startDate trong bảng staff
DELIMITER $$

CREATE TRIGGER check_staff_date_before_insert
BEFORE INSERT ON staff
FOR EACH ROW
BEGIN
    IF NEW.endDate <= NEW.startDate THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'End date must be after start date.';
    END IF;
END $$

CREATE TRIGGER check_staff_date_before_update
BEFORE UPDATE ON staff
FOR EACH ROW
BEGIN
    IF NEW.endDate <= NEW.startDate THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'End date must be after start date.';
    END IF;
END $$

DELIMITER ;

DELIMITER $$

CREATE TRIGGER after_order_detail_insert
AFTER INSERT ON order_detail
FOR EACH ROW
BEGIN
    UPDATE `order`
    SET Total = (SELECT COALESCE(SUM(TotalPrice), 0) FROM order_detail WHERE OrderID = NEW.OrderID)
    WHERE OrderID = NEW.OrderID;
END $$

DELIMITER ;

DELIMITER $$

CREATE TRIGGER after_order_detail_update
AFTER UPDATE ON order_detail
FOR EACH ROW
BEGIN
    UPDATE `order`
    SET Total = (SELECT COALESCE(SUM(TotalPrice), 0) FROM order_detail WHERE OrderID = NEW.OrderID)
    WHERE OrderID = NEW.OrderID;
END $$

DELIMITER ;

DELIMITER $$

CREATE TRIGGER after_order_detail_delete
AFTER DELETE ON order_detail
FOR EACH ROW
BEGIN
    UPDATE `order`
    SET Total = (SELECT COALESCE(SUM(TotalPrice), 0) FROM order_detail WHERE OrderID = OLD.OrderID)
    WHERE OrderID = OLD.OrderID;
END $$

DELIMITER ;


