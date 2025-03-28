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
-- 				Tạo bảng Khách hàng
CREATE TABLE `customer` (
	`customer_ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
	`lastName` VARCHAR(50) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`firstName` VARCHAR(20) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`phoneNumber` VARCHAR(10) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`sex` TINYINT NOT NULL,
	`citizenNumber` VARCHAR(12) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`address` TEXT NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`AccountID` INT UNSIGNED NOT NULL,
	PRIMARY KEY (`customer_ID`) USING BTREE,
	UNIQUE INDEX `Customer_PhoneNumber` (`phoneNumber`) USING BTREE,
	UNIQUE INDEX `Customer_CitizenNumber` (`citizenNumber`) USING BTREE,
	INDEX `AccountID` (`AccountID`) USING BTREE,
	CONSTRAINT `FK_customer_account` FOREIGN KEY (`AccountID`) REFERENCES `account` (`AccountID`) ON UPDATE NO ACTION ON DELETE NO ACTION,
	CONSTRAINT `CkCustomer_CitizenNumber` CHECK ((length(`citizenNumber`) = 12)),
	CONSTRAINT `CkCustomer_phoneNumber` CHECK ((length(`phoneNumber`) = 10))
)
COLLATE='utf8mb4_unicode_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1
;

-- 				Tạo bảng Nhân viên
CREATE TABLE `staff` (
	`StaffID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
	`lastName` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`firstName` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`Sex` TINYINT NOT NULL,
	`phoneNumber` VARCHAR(10) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`CitizenNumber` VARCHAR(12) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`Address` VARCHAR(300) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`Role_ID` INT UNSIGNED NOT NULL,
	`AccountID` INT UNSIGNED NOT NULL,
	PRIMARY KEY (`StaffID`) USING BTREE,
	UNIQUE INDEX `UnStaff_UN_PhoneNumber` (`phoneNumber`) USING BTREE,
	UNIQUE INDEX `UnStaff_UN_CitizenNumber` (`CitizenNumber`) USING BTREE,
	INDEX `FkStaff_RoleID` (`Role_ID`) USING BTREE,
	INDEX `AccountID` (`AccountID`) USING BTREE,
	CONSTRAINT `FkStaff_RoleID` FOREIGN KEY (`Role_ID`) REFERENCES `role` (`Role_ID`) ON UPDATE NO ACTION ON DELETE NO ACTION,
	CONSTRAINT `FK_staff_account` FOREIGN KEY (`AccountID`) REFERENCES `account` (`AccountID`) ON UPDATE NO ACTION ON DELETE NO ACTION,
	CONSTRAINT `CkStaff_UN_CitizenNumber` CHECK ((length(`CitizenNumber`) = 12)),
	CONSTRAINT `CkStaff_UN_PhoneNumber` CHECK ((length(`phoneNumber`) = 10))
)
COLLATE='utf8mb4_unicode_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1
;

-- 				Tạo bảng Đơn hàng
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
	CONSTRAINT `Fkorder_Customer_ID` FOREIGN KEY (`Customer_ID`) REFERENCES `customer` (`customer_ID`) ON UPDATE NO ACTION ON DELETE CASCADE,
	CONSTRAINT `Fkorder_StaffID` FOREIGN KEY (`StaffID`) REFERENCES `staff` (`StaffID`) ON UPDATE NO ACTION ON DELETE CASCADE,
	CONSTRAINT `FK_order_happenstatus` FOREIGN KEY (`HappenStatusID`) REFERENCES `happenstatus` (`HappenStatusID`) ON UPDATE NO ACTION ON DELETE CASCADE
)
COLLATE='utf8mb4_unicode_ci'
ENGINE=InnoDB
;

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
CREATE TABLE `pet` (
	`PetID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
	`PetName` TEXT NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`age` TINYINT UNSIGNED NOT NULL,
	`Customer_ID` INT UNSIGNED NOT NULL,
	`TypePetID` INT UNSIGNED NOT NULL,
	PRIMARY KEY (`PetID`) USING BTREE,
	INDEX `FkPet_CustomerID` (`Customer_ID`) USING BTREE,
	INDEX `FkParty_TypePetcustomerID` (`TypePetID`) USING BTREE,
	CONSTRAINT `FkParty_TypePetcustomerID` FOREIGN KEY (`TypePetID`) REFERENCES `typepet` (`TypePetID`) ON UPDATE NO ACTION ON DELETE NO ACTION,
	CONSTRAINT `FkPet_CustomerID` FOREIGN KEY (`Customer_ID`) REFERENCES `customer` (`customer_ID`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COLLATE='utf8mb4_unicode_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1
;

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

