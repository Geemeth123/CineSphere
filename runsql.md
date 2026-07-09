# CineSphere Database Setup Script

> Run this entire script in MySQL Workbench to set up the CineSphere database.

---

## 1. Create Database

```sql
CREATE DATABASE IF NOT EXISTS cinesphere;
USE cinesphere;
```

---

## 2. Users Table

```sql
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'TICKET_STAFF') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

---

## 3. Movies Table

```sql
CREATE TABLE movies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    poster_path VARCHAR(500),
    rating DECIMAL(3,1) DEFAULT 0.0,
    release_date DATE,
    duration_minutes INT NOT NULL DEFAULT 120,
    genre VARCHAR(100),
    adult_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    kids_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    tmdb_id INT UNIQUE,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

---

## 4. Halls Table

```sql
CREATE TABLE halls (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    total_seats INT NOT NULL,
    seat_rows INT NOT NULL,
    seat_columns INT NOT NULL,
    status ENUM('ACTIVE', 'MAINTENANCE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 5. Seats Table

```sql
CREATE TABLE seats (
    id INT AUTO_INCREMENT PRIMARY KEY,
    hall_id INT NOT NULL,
    row_label CHAR(1) NOT NULL,
    seat_number INT NOT NULL,
    seat_type ENUM('REGULAR', 'PREMIUM', 'VIP') NOT NULL DEFAULT 'REGULAR',
    FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE,
    UNIQUE KEY unique_seat (hall_id, row_label, seat_number)
);
```

---

## 6. Shows Table

```sql
CREATE TABLE shows (
    id INT AUTO_INCREMENT PRIMARY KEY,
    movie_id INT NOT NULL,
    hall_id INT NOT NULL,
    show_date DATE NOT NULL,
    show_time TIME NOT NULL,
    status ENUM('SCHEDULED', 'CANCELLED', 'COMPLETED') NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE,
    FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE
);
```

---

## 7. Bookings Table

```sql
CREATE TABLE bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    show_id INT NOT NULL,
    booked_by INT NOT NULL,
    adult_count INT NOT NULL DEFAULT 0,
    kids_count INT NOT NULL DEFAULT 0,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    booking_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('CONFIRMED', 'CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
    qr_code TEXT,
    FOREIGN KEY (show_id) REFERENCES shows(id),
    FOREIGN KEY (booked_by) REFERENCES users(id)
);
```

---

## 8. Booking Seats Table

```sql
CREATE TABLE booking_seats (
    id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    seat_id INT NOT NULL,
    ticket_type ENUM('ADULT', 'KID') NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    FOREIGN KEY (seat_id) REFERENCES seats(id),
    UNIQUE KEY unique_booking_seat (booking_id, seat_id)
);
```

---

## 9. Stored Procedure: Auto-Generate Seats

```sql
DELIMITER //
CREATE PROCEDURE generate_hall_seats(IN p_hall_id INT, IN p_rows INT, IN p_cols INT)
BEGIN
    DECLARE r INT DEFAULT 0;
    DECLARE c INT DEFAULT 0;
    DECLARE row_char CHAR(1);

    WHILE r < p_rows DO
        SET row_char = CHAR(65 + r);
        SET c = 1;
        WHILE c <= p_cols DO
            INSERT INTO seats (hall_id, row_label, seat_number, seat_type)
            VALUES (p_hall_id, row_char, c, 'REGULAR');
            SET c = c + 1;
        END WHILE;
        SET r = r + 1;
    END WHILE;
END //
DELIMITER ;
```

---

## 10. Seed Data

### Users

```sql
INSERT INTO users (username, password, full_name, role) VALUES
('admin', 'admin123', 'System Administrator', 'ADMIN'),
('ticket', 'ticket123', 'Counter Staff', 'TICKET_STAFF');
```

### Halls

```sql
INSERT INTO halls (name, total_seats, seat_rows, seat_columns) VALUES
('Hall A', 80, 8, 10),
('Hall B', 60, 6, 10),
('Hall C', 100, 10, 10);
```

### Generate Seats for All Halls

```sql
CALL generate_hall_seats(1, 8, 10);
CALL generate_hall_seats(2, 6, 10);
CALL generate_hall_seats(3, 10, 10);
```

### Movies

```sql
INSERT INTO movies (title, description, duration_minutes, genre, adult_price, kids_price, rating, release_date, status) VALUES
('The Dark Knight', 'When the menace known as the Joker wreaks havoc on Gotham, Batman must accept one of the greatest psychological tests of his ability to fight injustice.', 152, 'Action', 350.00, 200.00, 9.0, '2008-07-18', 'ACTIVE'),
('Inception', 'A thief who steals corporate secrets through dream-sharing technology is given the task of planting an idea into the mind of a CEO.', 148, 'Sci-Fi', 400.00, 250.00, 8.8, '2010-07-16', 'ACTIVE');
```

### Sample Shows (Today)

```sql
INSERT INTO shows (movie_id, hall_id, show_date, show_time, status) VALUES
(1, 1, CURDATE(), '14:00:00', 'SCHEDULED'),
(2, 2, CURDATE(), '18:00:00', 'SCHEDULED');
```
