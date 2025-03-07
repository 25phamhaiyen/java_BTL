-- tạo db
DROP DATABASE IF EXISTS bestpets;
CREATE DATABASE bestpets
CHARACTER SET utf8mb4 COLLATE UTF8MB4_UNICODE_CI;

-- Sử dụng DB
USE bestpets;

-- Cài đặt
SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

-- 				Tạo bảng Khách hàng
CREATE TABLE IF NOT EXISTS Customer(
	Customer_ID INT UNSIGNED AUTO_INCREMENT,
   `lastName` VARCHAR(50) NOT NULL,
   `firstName` VARCHAR(20) NOT NULL,
   phoneNumber VARCHAR(10) NOT NULL,
   Sex BIT NOT NULL,
   CitizenNumber VARCHAR(12) NOT NULL,
   Address TEXT NOT NULL,
   CONSTRAINT PkCustomer_CustomerID PRIMARY KEY (Customer_ID),
   CONSTRAINT Customer_PhoneNumber UNIQUE (phoneNumber),
   CONSTRAINT Customer_CitizenNumber UNIQUE (CitizenNumber),
   CONSTRAINT CkCustomer_phoneNumber CHECK (LENGTH(phoneNumber) = 10),
   CONSTRAINT CkCustomer_CitizenNumber CHECK (LENGTH(CitizenNumber) = 12)
);

-- 				Tạo bảng Trạng thái thanh toán
CREATE TABLE IF NOT EXISTS PaymentStatus(
	PaymentStatusID INT UNSIGNED AUTO_INCREMENT,
    UN_StatusCode INT NOT NULL,
    StatusName VARCHAR(100) NOT NULL,
    CONSTRAINT PkPaymentStatus_PaymentStatusID PRIMARY KEY (PaymentStatusID),
    CONSTRAINT UnPaymentStatus_UN_StatusCode UNIQUE (UN_StatusCode)
);

-- 				Tạo bảng Trạng thái xảy ra
CREATE TABLE IF NOT EXISTS HappenStatus(
	HappenStatusID INT UNSIGNED AUTO_INCREMENT,
    UN_StatusCode INT NOT NULL,
    StatusName VARCHAR(100) NOT NULL,
    CONSTRAINT PkHappenStatus_HappenStatusID PRIMARY KEY (HappenStatusID),
    CONSTRAINT UnHappenStatus_UN_StatusCode UNIQUE (UN_StatusCode)
);


--				Tạo bảng Vai trò
CREATE TABLE IF NOT EXISTS Role(
	Role_ID INT UNSIGNED AUTO_INCREMENT,
    RoleName VARCHAR(255) NOT NULL,
    CONSTRAINT PkRole_RoleID PRIMARY KEY (Role_ID)
);

-- 				Tạo bảng Nhân viên
CREATE TABLE IF NOT EXISTS Staff(
	StaffID INT UNSIGNED AUTO_INCREMENT,
	 `lastName` VARCHAR(255) NOT NULL,
    `firstName` VARCHAR(255) NOT NULL,
    Sex BIT NOT NULL,
    phoneNumber VARCHAR(10) NOT NULL,
    CitizenNumber VARCHAR(12) NOT NULL,
    Address VARCHAR(300) NOT NULL,
    Role_ID INT UNSIGNED NOT NULL,
    CONSTRAINT PkStaff_StaffID PRIMARY KEY (StaffID),
    CONSTRAINT UnStaff_UN_PhoneNumber UNIQUE (phoneNumber),
    CONSTRAINT UnStaff_UN_CitizenNumber UNIQUE (CitizenNumber),
	 CONSTRAINT CkStaff_UN_PhoneNumber CHECK (LENGTH(phoneNumber) = 10),
    CONSTRAINT CkStaff_UN_CitizenNumber CHECK (LENGTH(CitizenNumber) = 12),
    CONSTRAINT FkStaff_RoleID FOREIGN KEY (Role_ID) REFERENCES Role(Role_ID)
);

--				Tạo bảng loại dịch vụ
CREATE TABLE IF NOT EXISTS TypeService(
	TypeServiceID INT UNSIGNED AUTO_INCREMENT,
	UN_TypeName VARCHAR(200) NOT NULL,
    CONSTRAINT PkTypeService_TypeServiceID PRIMARY KEY (TypeServiceID),
    CONSTRAINT UnTypeService_UN_TypeName UNIQUE (UN_TypeName)
);
-- 				Tạo bảng Dịch vụ
CREATE TABLE IF NOT EXISTS service(
	serviceID INT UNSIGNED AUTO_INCREMENT,
    serviceName VARCHAR(255) NOT NULL,
    CostPrice DOUBLE DEFAULT 0,
    TypeServiceID INT UNSIGNED,
   -- describe TEXT NOT NULL,
    CONSTRAINT PkService_ServiceID PRIMARY KEY (serviceID),
    CONSTRAINT FkService_TypeServiceID FOREIGN KEY (TypeServiceID) REFERENCES TypeService(TypeServiceID)
);

-- 				Tạo bảng loại thú cưng
CREATE TABLE IF NOT EXISTS TypePet(
	TypePetID INT UNSIGNED AUTO_INCREMENT,
    UN_TypeName VARCHAR(100) NOT NULL,
    CONSTRAINT PkTypePet_TypePetID PRIMARY KEY (TypePetID) ,
    CONSTRAINT UnTypePet_UN_TypeName UNIQUE (UN_TypeName)
);

-- 				Tạo bảng thú cưng
CREATE TABLE IF NOT EXISTS Pet(
	PetID INT UNSIGNED AUTO_INCREMENT,
    PetName TEXT NOT NULL,
    age TINYINT UNSIGNED NOT NULL,
    Customer_ID INT UNSIGNED,
    TypePetID INT UNSIGNED,
    CONSTRAINT PkPet_PetID PRIMARY KEY (PetID),
    CONSTRAINT FkPet_CustomerID FOREIGN KEY (Customer_ID) REFERENCES Customer(Customer_ID),
    CONSTRAINT FkParty_TypePetcustomerID FOREIGN KEY (TypePetID) REFERENCES TypePet(TypePetID)
);

-- 				Tạo bảng Đơn hàng
CREATE TABLE `Order`(
	orderID INT UNSIGNED AUTO_INCREMENT,
   orderDate DATE NOT NULL,
    Total DOUBLE DEFAULT 0,
   Customer_ID INT UNSIGNED,
   StaffID INT UNSIGNED,
    CONSTRAINT Pkorder_orderID PRIMARY KEY (orderID),
    CONSTRAINT Fkorder_Customer_ID FOREIGN KEY (Customer_ID) REFERENCES Customer(Customer_ID),
    CONSTRAINT Fkorder_StaffID FOREIGN KEY (StaffID) REFERENCES Staff(StaffID)
);

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
CREATE TABLE Account(
	AccountID INT UNSIGNED AUTO_INCREMENT,
    UN_Username VARCHAR(255) NOT NULL,
    `Password` TEXT NOT NULL,
	Email VARCHAR(255),
    StaffID INT UNSIGNED,
    CONSTRAINT PkAccount_AccountID PRIMARY KEY (AccountID),
    CONSTRAINT UnAccount_UN_Username UNIQUE (UN_Username),
    CONSTRAINT FkAccount_StaffID FOREIGN KEY (StaffID) REFERENCES Staff(StaffID)
);
