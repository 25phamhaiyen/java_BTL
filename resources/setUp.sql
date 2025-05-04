DROP DATABASE IF EXISTS bestpets;
CREATE DATABASE IF NOT EXISTS bestpets;
USE bestpets;

-- Vai trò trong hệ thống
CREATE TABLE role (
    role_id INT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL UNIQUE COMMENT 'Tên vai trò: MANAGER, STAFF,...'
);

-- Tài khoản đăng nhập
CREATE TABLE `account` (
    account_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    role_id INT,
    `active` BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES role(role_id) ON DELETE SET NULL
);

-- Con người
CREATE TABLE `person` (
    `person_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER') DEFAULT 'OTHER',
    `phone` VARCHAR(10) NOT NULL COLLATE 'utf8mb4_unicode_ci',
    `address` TEXT NOT NULL COLLATE 'utf8mb4_unicode_ci',
    `email` TEXT NOT NULL COLLATE 'utf8mb4_unicode_ci',
    PRIMARY KEY (`person_id`) USING BTREE,
    UNIQUE INDEX `Person_Phone` (`phone`) USING BTREE,
    CONSTRAINT `CkPerson_phone` CHECK ((length(`phone`) = 10))
);

-- Nhân viên
CREATE TABLE staff (
    staff_id INT UNSIGNED NOT NULL,
    dob DATE,
    salary DECIMAL(12, 2) DEFAULT 0.0,
    hire_date DATE,
    account_id INT UNIQUE,
    role_id INT,
    PRIMARY KEY (staff_id),
    FOREIGN KEY (staff_id) REFERENCES `person`(person_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role(role_id) ON DELETE SET NULL,
    FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE SET NULL
);

-- Khách hàng
CREATE TABLE customer (
    customer_id INT UNSIGNED NOT NULL,
    `point` INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (customer_id),
    FOREIGN KEY (customer_id) REFERENCES person(person_id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- Chuẩn hóa dữ liệu giống loài
CREATE TABLE pet_type (
    type_id INT PRIMARY KEY AUTO_INCREMENT,
    species VARCHAR(50) NOT NULL,
    breed VARCHAR(100) NOT NULL
);

-- Thú cưng
CREATE TABLE pet (
    pet_id INT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    type_id INT,
    pet_gender ENUM('MALE', 'FEMALE', 'UNKNOWN') DEFAULT 'UNKNOWN',
    dob DATE,
    weight DECIMAL(5,2),
    note TEXT,
    customer_id INT UNSIGNED NOT NULL,
    FOREIGN KEY (type_id) REFERENCES pet_type(type_id),
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE
);

-- Chương trình khuyến mãi
CREATE TABLE promotion (
    promotion_id INT PRIMARY KEY AUTO_INCREMENT,
    `code` VARCHAR(50) NOT NULL UNIQUE,
    `description` TEXT,
    discount_percent INT CHECK (discount_percent >= 0 AND discount_percent <= 100),
    start_date DATE,
    end_date DATE,
    CHECK (start_date <= end_date),
    active BOOLEAN DEFAULT TRUE
);

-- Dịch vụ chăm sóc thú cưng
CREATE TABLE service (
    service_id INT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `description` TEXT,
    price DECIMAL(10, 2) NOT NULL,
    duration_minutes INT,
    `active` BOOLEAN DEFAULT TRUE
);

-- Đặt lịch chăm sóc
CREATE TABLE booking (
    booking_id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    customer_id INT UNSIGNED NOT NULL,
    pet_id INT NOT NULL,
    staff_id INT UNSIGNED,
    booking_time DATETIME NOT NULL,
    `status` ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED') DEFAULT 'PENDING',
    note TEXT,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    FOREIGN KEY (pet_id) REFERENCES pet(pet_id),
    FOREIGN KEY (staff_id) REFERENCES staff(staff_id)
);

-- 1 booking có thể chứa nhiều dịch vụ
CREATE TABLE booking_detail (
    booking_detail_id INT PRIMARY KEY AUTO_INCREMENT,
    booking_id INT UNSIGNED NOT NULL,
    service_id INT NOT NULL,
    quantity INT DEFAULT 1,
    price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES booking(booking_id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES service(service_id)
);

-- Đơn hàng
CREATE TABLE `order` (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT UNSIGNED NOT NULL,
    staff_id INT UNSIGNED,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    voucher_code VARCHAR(50),
    booking_id INT UNSIGNED NULL,
    total_amount DECIMAL(12,2) DEFAULT 0.0,
    `status` ENUM('PENDING', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    FOREIGN KEY (staff_id) REFERENCES staff(staff_id),
    FOREIGN KEY (voucher_code) REFERENCES promotion(CODE),
    FOREIGN KEY (booking_id) REFERENCES booking(booking_id) ON DELETE SET NULL
);

-- Hóa đơn
CREATE TABLE invoice (
    invoice_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL UNIQUE,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    subtotal DECIMAL(15,2) DEFAULT 0.00,
    discount_percent DECIMAL(5,2) DEFAULT 0.00,
    discount_amount DECIMAL(15,2) DEFAULT 0.00,
    points_used INT DEFAULT 0,
    promotion_code VARCHAR(50),
    total DECIMAL(12,2),
    amount_paid DECIMAL(15,2) DEFAULT 0.00,
    payment_method ENUM('CASH', 'CARD', 'MOMO', 'BANKING') DEFAULT 'CASH',
    `status` ENUM('COMPLETED', 'PENDING', 'CANCELLED', 'FAILED') DEFAULT 'COMPLETED',
    staff_id INT UNSIGNED,
    note TEXT,
    FOREIGN KEY (order_id) REFERENCES `order`(order_id),
    FOREIGN KEY (staff_id) REFERENCES staff(staff_id),
    FOREIGN KEY (promotion_code) REFERENCES promotion(code) ON DELETE SET NULL
);

-- Chi tiết đơn hàng
CREATE TABLE order_detail (
    order_detail_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    service_id INT NOT NULL,
    quantity INT DEFAULT 1,
    price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES `order`(order_id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES service(service_id)
);

-- Lịch làm việc của nhân viên
CREATE TABLE work_schedule (
    schedule_id INT PRIMARY KEY AUTO_INCREMENT,
    staff_id INT UNSIGNED NOT NULL,
    work_date DATE NOT NULL,
    shift ENUM('MORNING', 'AFTERNOON', 'EVENING'),
    start_time TIME,
    end_time TIME,
    location VARCHAR(100),
    task VARCHAR(255),
    note TEXT,
    FOREIGN KEY (staff_id) REFERENCES staff(staff_id)
);

-- Danh sách quyền trong hệ thống
CREATE TABLE permission (
    permission_code VARCHAR(100) PRIMARY KEY,
    `description` TEXT NOT NULL
);

-- Phân quyền chi tiết cho tài khoản
CREATE TABLE account_permission (
    permission_account_id INT PRIMARY KEY AUTO_INCREMENT,
    account_id INT NOT NULL,
    permission_code VARCHAR(100) NOT NULL,
    FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE,
    FOREIGN KEY (permission_code) REFERENCES permission(permission_code) ON DELETE CASCADE
);

-- View tổng quan dashboard
CREATE VIEW dashboard_summary AS
SELECT
    (SELECT COUNT(*) FROM customer) AS total_customers,
    (SELECT COUNT(*) FROM booking) AS total_bookings,
    (SELECT COUNT(*) FROM invoice) AS total_invoices,
    (SELECT SUM(total) FROM invoice WHERE status = 'COMPLETED') AS total_revenue;

-- Procedure tạo đơn hàng từ booking
DELIMITER //
CREATE PROCEDURE create_order_from_booking (
    IN p_booking_id INT UNSIGNED,
    IN p_staff_id INT UNSIGNED
)
BEGIN
    DECLARE new_order_id INT;

    -- Tạo đơn hàng mới
    INSERT INTO `order` (customer_id, staff_id, order_date, status, booking_id)
    SELECT b.customer_id, p_staff_id, NOW(), 'PENDING', b.booking_id
    FROM booking b
    WHERE b.booking_id = p_booking_id;

    SET new_order_id = LAST_INSERT_ID();

    -- Sao chép chi tiết booking sang chi tiết đơn hàng, LẤY GIÁ TỪ booking_detail
    INSERT INTO order_detail (order_id, service_id, quantity, price)
    SELECT new_order_id, bd.service_id, bd.quantity, bd.price
    FROM booking_detail bd
    WHERE bd.booking_id = p_booking_id;

    -- Cập nhật tổng tiền cho đơn hàng (có thể gọi trigger hoặc cập nhật trực tiếp)
    UPDATE `order`
    SET total_amount = (SELECT SUM(price * quantity) FROM order_detail WHERE order_id = new_order_id)
    WHERE order_id = new_order_id;

    SELECT new_order_id AS created_order_id;

END;
//
DELIMITER ;

-- Trigger tự động cập nhật total_amount cho order
DELIMITER //
CREATE TRIGGER trg_update_order_total
AFTER INSERT ON order_detail
FOR EACH ROW
BEGIN
    UPDATE `order`
    SET total_amount = (
        SELECT SUM(price * quantity) FROM order_detail WHERE order_id = NEW.order_id
    )
    WHERE order_id = NEW.order_id;
END;
// 
DELIMITER ;

-- Procedure tạo đơn hàng mới và chi tiết đơn hàng
DELIMITER //
CREATE PROCEDURE create_order_with_details (
    IN p_customer_id INT,
    IN p_staff_id INT,
    IN p_service_id INT,
    IN p_quantity INT
)
BEGIN
    DECLARE new_order_id INT;
    DECLARE service_price DECIMAL(10, 2);

    -- Lấy giá dịch vụ
    SELECT price INTO service_price
    FROM service
    WHERE service_id = p_service_id;

    IF service_price IS NOT NULL THEN
        INSERT INTO `order` (customer_id, staff_id, order_date, status)
        VALUES (p_customer_id, p_staff_id, NOW(), 'PENDING');

        SET new_order_id = LAST_INSERT_ID();

        INSERT INTO order_detail (order_id, service_id, quantity, price)
        VALUES (new_order_id, p_service_id, p_quantity, service_price);

        SELECT new_order_id AS created_order_id;
    ELSE
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không tìm thấy dịch vụ với service_id đã cung cấp.';
    END IF;
END;
//
DELIMITER ;

-- Procedure thêm chi tiết đặt lịch
DELIMITER //
CREATE PROCEDURE add_booking_detail (
    IN p_booking_id INT,
    IN p_service_id INT,
    IN p_quantity INT,
    IN p_price DECIMAL(10, 2) -- Cho phép truyền giá, nếu không sẽ tự động lấy
)
BEGIN
    DECLARE service_price DECIMAL(10, 2);

    IF p_price IS NULL THEN
        -- Lấy giá dịch vụ từ bảng service nếu không được cung cấp
        SELECT price INTO service_price
        FROM service
        WHERE service_id = p_service_id;

        IF service_price IS NOT NULL THEN
            INSERT INTO booking_detail (booking_id, service_id, quantity, price)
            VALUES (p_booking_id, p_service_id, p_quantity, service_price);
        ELSE
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Không tìm thấy dịch vụ với service_id đã cung cấp.';
        END IF;
    ELSE
        -- Sử dụng giá được cung cấp
        INSERT INTO booking_detail (booking_id, service_id, quantity, price)
        VALUES (p_booking_id, p_service_id, p_quantity, p_price);
    END IF;
END;
//
DELIMITER ;



-- tạo hóa đơn để xử lý việc áp dụng mã giảm giá
DELIMITER //
CREATE PROCEDURE create_invoice (
    IN p_order_id INT,
    IN p_promotion_code VARCHAR(50),
    IN p_amount_paid DECIMAL(15, 2),
    IN p_payment_method ENUM('CASH', 'CARD', 'MOMO', 'BANKING'),
    IN p_staff_id INT UNSIGNED,
    IN p_note TEXT
)
BEGIN
    DECLARE v_subtotal DECIMAL(15, 2);
    DECLARE v_discount_percent INT DEFAULT 0;
    DECLARE v_discount_amount DECIMAL(15, 2) DEFAULT 0.00;
    DECLARE v_total DECIMAL(12, 2);

    -- Lấy subtotal từ đơn hàng
    SELECT total_amount INTO v_subtotal
    FROM `order`
    WHERE order_id = p_order_id;

    IF v_subtotal IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không tìm thấy đơn hàng với order_id đã cung cấp.';
    END IF;

    -- Kiểm tra và áp dụng mã giảm giá nếu có
    IF p_promotion_code IS NOT NULL AND p_promotion_code <> '' THEN
        SELECT discount_percent INTO v_discount_percent
        FROM promotion
        WHERE code = p_promotion_code AND active = TRUE AND start_date <= CURDATE() AND end_date >= CURDATE();

        IF v_discount_percent IS NOT NULL THEN
            SET v_discount_amount = v_subtotal * (v_discount_percent / 100);
        ELSE
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Mã khuyến mãi không hợp lệ hoặc đã hết hạn.';
        END IF;
    END IF;

    -- Tính toán tổng tiền sau giảm giá
    SET v_total = v_subtotal - v_discount_amount;

    -- Tạo hóa đơn mới
    INSERT INTO invoice (order_id, payment_date, subtotal, discount_percent, discount_amount, promotion_code, total, amount_paid, payment_method, staff_id, note)
    VALUES (p_order_id, NOW(), v_subtotal, v_discount_percent, v_discount_amount, p_promotion_code, v_total, p_amount_paid, p_payment_method, p_staff_id, p_note);

END;
//
DELIMITER ;

-- tự động gọi procedure create_invoice sau khi một đơn hàng (order) được đánh dấu là đã hoàn thành (status = 'COMPLETED')
DELIMITER //
CREATE TRIGGER trg_auto_create_invoice_after_order_complete
AFTER UPDATE ON `order`
FOR EACH ROW
BEGIN
    IF NEW.status = 'COMPLETED' AND OLD.status != 'COMPLETED' THEN
        -- Gọi procedure create_invoice với các giá trị mặc định hoặc logic xác định
        CALL create_invoice(
            NEW.order_id,        -- order_id của đơn hàng vừa hoàn thành
            NULL,                -- promotion_code (có thể để NULL nếu không tự động áp dụng)
            NEW.total_amount,    -- amount_paid (có thể đặt bằng total_amount ban đầu)
            'CASH',              -- payment_method mặc định (có thể cần logic khác)
            NEW.staff_id,        -- staff_id của nhân viên đã xử lý đơn hàng
            'Tự động tạo hóa đơn' -- note mặc định
        );
    END IF;
END;
//
DELIMITER ;

-- Trigger cập nhật trạng thái đơn hàng khi hóa đơn thanh toán xong
DELIMITER //
CREATE TRIGGER trg_invoice_paid_update_order
AFTER UPDATE ON invoice
FOR EACH ROW
BEGIN
    IF NEW.status = 'COMPLETED' THEN
        UPDATE `order`
        SET status = 'COMPLETED'
        WHERE order_id = NEW.order_id;
    END IF;
END;
//
DELIMITER ;

-- Trigger cập nhật total_amount nếu sửa order_detail
DELIMITER //
CREATE TRIGGER trg_update_order_total_after_update
AFTER UPDATE ON order_detail
FOR EACH ROW
BEGIN
    UPDATE `order`
    SET total_amount = (
        SELECT SUM(price * quantity) FROM order_detail WHERE order_id = NEW.order_id
    )
    WHERE order_id = NEW.order_id;
END;
//
DELIMITER ;s

-- Procedure gán quyền theo vai trò
DELIMITER $$
CREATE PROCEDURE assign_permission_by_role(IN acc_id INT)
BEGIN
    DECLARE role_name VARCHAR(50);

    SELECT r.role_name INTO role_name
    FROM staff s
    JOIN role r ON s.role_id = r.role_id
    WHERE s.account_id = acc_id;

    DELETE FROM account_permission WHERE account_id = acc_id;

    IF role_name = 'ADMIN' THEN
        INSERT INTO account_permission(account_id, permission_code)
        SELECT acc_id, permission_code FROM permission, (SELECT acc_id AS acc_id) AS temp;

    ELSEIF role_name = 'STAFF_CARE' THEN
        INSERT INTO account_permission(account_id, permission_code)
        SELECT acc_id, permission_code
        FROM permission, (SELECT acc_id AS acc_id) AS temp
        WHERE permission_code IN ('VIEW_BOOKING_ASSIGNED', 'VIEW_CUSTOMER', 'UPDATE_PROFILE');

    ELSEIF role_name = 'STAFF_CASHIER' THEN
        INSERT INTO account_permission(account_id, permission_code)
        SELECT acc_id, permission_code
        FROM permission, (SELECT acc_id AS acc_id) AS temp
        WHERE permission_code IN ('PRINT_RECEIPT', 'VIEW_CUSTOMER', 'UPDATE_PROFILE');

    ELSEIF role_name = 'STAFF_RECEPTION' THEN
        INSERT INTO account_permission(account_id, permission_code)
        SELECT acc_id, permission_code
        FROM permission, (SELECT acc_id AS acc_id) AS temp
        WHERE permission_code IN ('APPLY_PROMOTION', 'VIEW_CUSTOMER', 'UPDATE_PROFILE');
    END IF;
END$$
DELIMITER ;

-- Trigger tự động gán quyền sau khi thêm nhân viên
DELIMITER $$
CREATE TRIGGER trg_assign_permission_after_staff_insert
AFTER INSERT ON staff
FOR EACH ROW
BEGIN
    CALL assign_permission_by_role(NEW.account_id);
END$$
DELIMITER ;

-- Procedure gán quyền cho tài khoản
DELIMITER $$
CREATE PROCEDURE grant_permission(
    IN p_account_id INT,
    IN p_permission_code VARCHAR(100)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM account_permission
        WHERE account_id = p_account_id AND permission_code = p_permission_code
    ) THEN
        INSERT INTO account_permission(account_id, permission_code)
        VALUES (p_account_id, p_permission_code);
    END IF;
END$$
DELIMITER ;

-- Procedure xóa quyền của tài khoản
DELIMITER $$
CREATE PROCEDURE revoke_permission(
    IN p_account_id INT,
    IN p_permission_code VARCHAR(100)
)
BEGIN
    DELETE FROM account_permission
    WHERE account_id = p_account_id AND permission_code = p_permission_code;
END$$
DELIMITER ;

-- Trigger cập nhật điểm thưởng của khách hàng khi thanh toán hóa đơn
DELIMITER $$
CREATE TRIGGER trg_update_point_after_invoice
AFTER UPDATE ON invoice
FOR EACH ROW
BEGIN
    DECLARE v_customer_id INT;

    IF NEW.status = 'COMPLETED' AND OLD.status != 'COMPLETED' THEN
        SELECT customer_id INTO v_customer_id
        FROM `order`
        WHERE order_id = NEW.order_id;

        UPDATE customer
        SET point = point + FLOOR(NEW.total / 1000)
        WHERE customer_id = v_customer_id;
    END IF;
END$$
DELIMITER ;
-- Tổng doanh thu
DELIMITER $$
CREATE PROCEDURE sp_get_total_revenue_week()
BEGIN
    SELECT SUM(od.quantity * od.price) AS total_revenue
    FROM order_detail od
    JOIN invoice i ON i.order_id = od.order_id
    WHERE YEARWEEK(i.payment_date, 1) = YEARWEEK(CURDATE(), 1);
END$$

CREATE PROCEDURE sp_get_total_revenue_month()
BEGIN
    SELECT SUM(od.quantity * od.price) AS total_revenue
    FROM order_detail od
    JOIN invoice i ON i.order_id = od.order_id
    WHERE MONTH(i.payment_date) = MONTH(CURDATE()) AND YEAR(i.payment_date) = YEAR(CURDATE());
END$$

CREATE PROCEDURE sp_get_total_revenue_year()
BEGIN
    SELECT SUM(od.quantity * od.price) AS total_revenue
    FROM order_detail od
    JOIN invoice i ON i.order_id = od.order_id
    WHERE YEAR(i.payment_date) = YEAR(CURDATE());
END$$
DELIMITER $$

-- Tổng số đơn đặt lịch
DELIMITER $$

CREATE PROCEDURE sp_get_total_bookings_week()
BEGIN
    SELECT COUNT(*) 
    FROM booking
    WHERE YEARWEEK(booking_time, 1) = YEARWEEK(CURDATE(), 1);
END$$

CREATE PROCEDURE sp_get_total_bookings_month()
BEGIN
    SELECT COUNT(*) 
    FROM booking
    WHERE MONTH(booking_time) = MONTH(CURDATE())
      AND YEAR(booking_time) = YEAR(CURDATE());
END$$

CREATE PROCEDURE sp_get_total_bookings_year()
BEGIN
    SELECT COUNT(*) 
    FROM booking
    WHERE YEAR(booking_time) = YEAR(CURDATE());
END$$

DELIMITER ;

-- Tổng số khách hàng mới
DELIMITER $$

CREATE PROCEDURE sp_get_total_new_customers_week()
BEGIN
    SELECT COUNT(*) 
    FROM customer
    WHERE YEARWEEK(created_at, 1) = YEARWEEK(CURDATE(), 1);
END$$

CREATE PROCEDURE sp_get_total_new_customers_month()
BEGIN
    SELECT COUNT(*) 
    FROM customer
    WHERE MONTH(created_at) = MONTH(CURDATE())
      AND YEAR(created_at) = YEAR(CURDATE());
END$$

CREATE PROCEDURE sp_get_total_new_customers_year()
BEGIN
    SELECT COUNT(*) 
    FROM customer
    WHERE YEAR(created_at) = YEAR(CURDATE());
END$$

DELIMITER ;

