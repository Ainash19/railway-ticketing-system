-- ============================================================
-- Railway Ticketing System - Seed Data
-- Run AFTER schema.sql to populate demo data.
--
-- This reproduces the exact scenario shown in SCREENSHOTS.md:
-- two demo accounts, five fictional Canadian train routes, and
-- one passenger (John Snow) whose booking history triggers all
-- three fraud rules (R01, R02, R03).
--
-- Demo password for every account below: password123
-- (SHA-256 hash: ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f)
-- ============================================================

PRAGMA foreign_keys = ON;

-- ============================================================
-- USERS
-- ============================================================
INSERT INTO Users (user_id, username, password_hash, email, role) VALUES
  (1, 'john_snow',  'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'john@email.com',  'passenger'),
  (2, 'jane_smith', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'jane@email.com',  'passenger'),
  (3, 'admin1',     'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'admin@railway.com', 'admin');

-- ============================================================
-- PASSENGERS
-- ============================================================
INSERT INTO Passengers (passenger_id, user_id, full_name, phone, email) VALUES
  (1, 1, 'John Snow',  '416-555-0123', 'john@email.com'),
  (2, 2, 'Jane Smith', '604-555-0456', 'jane@email.com');

-- ============================================================
-- TRAINS
-- ============================================================
INSERT INTO Trains (train_id, train_number, train_name, source_station, destination_station, total_seats, base_fare, is_active) VALUES
  (1, 'VIA001', 'The Canadian',          'Toronto',   'Vancouver', 500, 688.00, 1),
  (2, 'VIA002', 'Maple Corridor',        'Toronto',   'Montreal',  400, 142.00, 1),
  (3, 'VIA003', 'Rocky Mountain Service','Vancouver', 'Jasper',    300, 242.00, 1),
  (4, 'VIA004', 'Capital Corridor',      'Ottawa',    'Toronto',   350, 129.99, 1),
  (5, 'VIA005', 'Ocean Service',         'Montreal',  'Halifax',   450, 273.00, 1);

-- ============================================================
-- SCHEDULES
-- ============================================================
INSERT INTO Schedules (schedule_id, train_id, departure_date, departure_time, arrival_date, arrival_time, available_seats) VALUES
  (1,  1, '2026-07-15', '09:55:00', '2026-07-19', '08:00:00', 498),
  (2,  2, '2026-07-15', '09:30:00', '2026-07-15', '14:30:00', 400),
  (3,  3, '2026-07-15', '15:00:00', '2026-07-16', '11:00:00', 300),
  (4,  4, '2026-07-15', '10:00:00', '2026-07-15', '14:00:00', 350),
  (5,  5, '2026-07-15', '18:30:00', '2026-07-16', '18:36:00', 450),
  (6,  1, '2026-07-12', '09:55:00', '2026-07-16', '08:00:00', 500),
  (7,  2, '2026-07-12', '09:30:00', '2026-07-12', '14:30:00', 400),
  (8,  3, '2026-07-12', '15:00:00', '2026-07-13', '11:00:00', 300),
  (9,  4, '2026-07-12', '10:00:00', '2026-07-12', '14:00:00', 350),
  (10, 5, '2026-07-12', '18:30:00', '2026-07-13', '18:36:00', 450);

-- ============================================================
-- BOOKINGS  (all under John Snow — passenger_id 1)
-- ============================================================
INSERT INTO Bookings (booking_id, passenger_id, schedule_id, booking_time, cancellation_time, seat_number, ticket_price, status, is_fraud_flagged) VALUES
  (1, 1, 1, '2026-07-12 17:48:34', NULL,                  '9B', 688.00, 'confirmed', 0),
  (2, 1, 2, '2026-07-12 17:52:52', '2026-08-14 16:17:28', '5D', 142.00, 'cancelled', 0),
  (3, 1, 1, '2026-09-14 16:22:10', '2026-09-14 16:23:28', '4C', 688.00, 'cancelled', 0),
  (4, 1, 1, '2026-09-14 16:26:41', NULL,                  '1A', 688.00, 'confirmed', 0),
  (5, 1, 2, '2026-09-14 16:30:27', '2026-09-14 16:35:40', '3D', 142.00, 'cancelled', 0),
  (6, 1, 7, '2026-09-14 16:31:22', '2026-09-14 16:35:30', '3C', 142.00, 'cancelled', 0),
  (7, 1, 9, '2026-09-14 16:31:54', '2026-09-14 16:35:26', '3D', 129.99, 'cancelled', 0);

-- ============================================================
-- FRAUD ALERTS  (all under John Snow - R01, R02, and R03 all triggered)
-- ============================================================
INSERT INTO FraudAlerts (alert_id, passenger_id, fraud_type, alert_description, severity, alert_time, is_reviewed) VALUES
  (1, 1, 'R01-DUPLICATE_BOOKING',      'Passenger 1 attempted duplicate booking on schedule 1 within 1 hour.',        'high',   '2026-07-12 17:49:28', 1),
  (2, 1, 'R01-DUPLICATE_BOOKING',      'Passenger 1 attempted duplicate booking on schedule 7 within 1 hour.',        'high',   '2026-09-14 16:31:32', 0),
  (3, 1, 'R02-RAPID_BOOKING',          'Passenger 1 made 4 bookings in the last 10 minutes (limit: 3).',              'high',   '2026-09-14 16:31:59', 0),
  (4, 1, 'R03-EXCESSIVE_CANCELLATIONS','Passenger 1 has cancelled 3 bookings in the last 24 hours (limit: 2).',       'medium', '2026-09-14 16:35:30', 0),
  (5, 1, 'R03-EXCESSIVE_CANCELLATIONS','Passenger 1 has cancelled 4 bookings in the last 24 hours (limit: 2).',       'medium', '2026-09-14 16:35:40', 1);
