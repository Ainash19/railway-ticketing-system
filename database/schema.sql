-- ============================================================
-- Railway Ticketing System - SQLite Schema
-- ============================================================

PRAGMA foreign_keys = ON;

DROP TABLE IF EXISTS FraudAlerts;
DROP TABLE IF EXISTS Bookings;
DROP TABLE IF EXISTS Schedules;
DROP TABLE IF EXISTS Trains;
DROP TABLE IF EXISTS Passengers;
DROP TABLE IF EXISTS Users;

-- Users Table
CREATE TABLE Users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    role TEXT NOT NULL DEFAULT 'passenger' CHECK (role IN ('passenger', 'admin'))
);

-- Passengers Table
CREATE TABLE Passengers (
    passenger_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER UNIQUE NOT NULL,
    full_name TEXT NOT NULL,
    phone TEXT,
    email TEXT,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- Trains Table
CREATE TABLE Trains (
    train_id INTEGER PRIMARY KEY AUTOINCREMENT,
    train_number TEXT UNIQUE NOT NULL,
    train_name TEXT NOT NULL,
    source_station TEXT NOT NULL,
    destination_station TEXT NOT NULL,
    total_seats INTEGER NOT NULL,
    base_fare REAL NOT NULL,
    is_active INTEGER DEFAULT 1
);

-- Schedules Table
CREATE TABLE Schedules (
    schedule_id INTEGER PRIMARY KEY AUTOINCREMENT,
    train_id INTEGER NOT NULL,
    departure_date TEXT NOT NULL,
    departure_time TEXT NOT NULL,
    arrival_date TEXT NOT NULL,
    arrival_time TEXT NOT NULL,
    available_seats INTEGER NOT NULL,
    FOREIGN KEY (train_id) REFERENCES Trains(train_id) ON DELETE CASCADE
);

-- Bookings Table
CREATE TABLE Bookings (
    booking_id INTEGER PRIMARY KEY AUTOINCREMENT,
    passenger_id INTEGER NOT NULL,
    schedule_id INTEGER NOT NULL,
    booking_time TEXT NOT NULL DEFAULT (datetime('now')),
    cancellation_time TEXT,
    seat_number TEXT NOT NULL,
    ticket_price REAL NOT NULL,
    status TEXT NOT NULL DEFAULT 'confirmed' CHECK (status IN ('confirmed', 'cancelled')),
    is_fraud_flagged INTEGER DEFAULT 0,
    FOREIGN KEY (passenger_id) REFERENCES Passengers(passenger_id),
    FOREIGN KEY (schedule_id) REFERENCES Schedules(schedule_id),
    UNIQUE (schedule_id, seat_number)
);

-- FraudAlerts Table
CREATE TABLE FraudAlerts (
    alert_id INTEGER PRIMARY KEY AUTOINCREMENT,
    passenger_id INTEGER NOT NULL,
    fraud_type TEXT NOT NULL,
    alert_description TEXT,
    severity TEXT NOT NULL DEFAULT 'medium' CHECK (severity IN ('low', 'medium', 'high')),
    alert_time TEXT NOT NULL DEFAULT (datetime('now')),
    is_reviewed INTEGER DEFAULT 0,
    FOREIGN KEY (passenger_id) REFERENCES Passengers(passenger_id)
);

-- Helpful indexes for the fraud-detection lookups
CREATE INDEX IF NOT EXISTS idx_bookings_passenger ON Bookings(passenger_id);
CREATE INDEX IF NOT EXISTS idx_bookings_schedule ON Bookings(schedule_id);
CREATE INDEX IF NOT EXISTS idx_fraudalerts_passenger ON FraudAlerts(passenger_id);
CREATE INDEX IF NOT EXISTS idx_schedules_train ON Schedules(train_id);
