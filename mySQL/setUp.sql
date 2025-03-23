-- tạo db
DROP DATABASE IF EXISTS bestpets;
CREATE DATABASE bestpets
CHARACTER SET utf8mb4 COLLATE UTF8MB4_UNICODE_CI;

-- Sử dụng DB
USE bestpets;

-- Cài đặt
SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

-- 				Tạo bảng Khách hàng
CREATE TABLE `customer` (
	`customer_ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
	`lastName` VARCHAR(50) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`firstName` VARCHAR(20) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`phoneNumber` VARCHAR(10) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`sex` BIT(1) NOT NULL,
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

-- 				Tạo bảng Nhân viên
CREATE TABLE `staff` (
	`StaffID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
	`lastName` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`firstName` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`Sex` BIT(1) NOT NULL,
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
	`Mô tả` TEXT NULL DEFAULT NULL COLLATE 'utf8mb4_unicode_ci',
	PRIMARY KEY (`serviceID`) USING BTREE,
	INDEX `FkService_TypeServiceID` (`TypeServiceID`) USING BTREE,
	CONSTRAINT `FkService_TypeServiceID` FOREIGN KEY (`TypeServiceID`) REFERENCES `typeservice` (`TypeServiceID`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COLLATE='utf8mb4_unicode_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1
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

-- 				Tạo bảng Đơn hàng
CREATE TABLE `order` (
	`orderID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
	`orderDate` DATE NOT NULL,
	`Total` DOUBLE NOT NULL DEFAULT '0',
	`Customer_ID` INT UNSIGNED NOT NULL,
	`StaffID` INT UNSIGNED NOT NULL,
	PRIMARY KEY (`orderID`) USING BTREE,
	INDEX `Fkorder_Customer_ID` (`Customer_ID`) USING BTREE,
	INDEX `Fkorder_StaffID` (`StaffID`) USING BTREE,
	CONSTRAINT `Fkorder_Customer_ID` FOREIGN KEY (`Customer_ID`) REFERENCES `customer` (`customer_ID`) ON UPDATE NO ACTION ON DELETE NO ACTION,
	CONSTRAINT `Fkorder_StaffID` FOREIGN KEY (`StaffID`) REFERENCES `staff` (`StaffID`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COLLATE='utf8mb4_unicode_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1
;


-- 				Tạo bảng Order
-- CREATE TABLE `Order`(
-- 	PartyID INT UNSIGNED,
--    DishID INT UNSIGNED,
--    Price DOUBLE NOT NULL,
--    CONSTRAINT PkOrder_PartyID_DishID PRIMARY KEY (PartyID, DishID),
--    CONSTRAINT FkOrder_PartyID FOREIGN KEY (PartyID) REFERENCES Party(PartyID),
--    CONSTRAINT FkOrder_DishID FOREIGN KEY (DishID) REFERENCES Dish(DishID)
-- );

-- 				Tạo bảng DetailInvoice
-- CREATE TABLE DetailInvoice(
--	DetailInvoiceID INT UNSIGNED AUTO_INCREMENT,
--    DishName VARCHAR(255) NOT NULL,
--    Unit_Price DOUBLE NOT NULL,
--    `Number` TINYINT UNSIGNED,
--    Amount DOUBLE NOT NULL,
--    InvoiceID INT UNSIGNED,
--    CONSTRAINT PkDetailInvoice_DetailInvoiceID PRIMARY KEY (DetailInvoiceID),
--    CONSTRAINT FkDetailInvoice_InvoiceID FOREIGN KEY (InvoiceID) REFERENCES Invoice(InvoiceID)
-- );customer
-- 				Tạo bảng Work
-- CREATE TABLE work(
--	PartyID INT UNSIGNED,customer
--    StaffID INT UNSIGNED,
--    Salary DOUBLE DEFAULT 0,
--    CONSTRAINT PkWork_PartyID_StaffID PRIMARY KEY (PartyID,StaffID),
--    CONSTRAINT FkWork_DishID FOREIGN KEY (PartyID) REFERENCES Party(PartyID),
--    CONSTRAINT FkWork_InvoiceID FOREIGN KEY (StaffID) REFERENCES Staff(StaffID)
-- );

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
