/**
 * Program Name: BookingDAO.java
 * Purpose: Database operations for the Bookings table.
 * 					Includes the three fraud-detection count queries
 * @author Ainash Zhumagulova 
 * Date Jun 26, 2026
 */
package com.railway.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.railway.config.DatabaseConnection;
import com.railway.models.Booking;

/**
 * 
 */
public class BookingDAO
{
	// CREATE
	
	/**
	 * 
	 * This method will: Inserts a new confirmed booking. 
	 * 									Use JDBC to modify the data in a database
	 * 									Use prepared statements to avoid  build a SQL statement directly from user input,
	 * 									avoiding making the database susceptible to a security vulnerability (SQL injection attack)
	 * @param passengerId
	 * @param scheduleId
	 * @param seatNumber
	 * @param ticketPrice
	 * @return the generated booking_id, or -1 on failure
	 */
	public static int createBooking(int passengerId, int scheduleId, String seatNumber, double ticketPrice)
	{
		
		String sql = "INSERT INTO Bookings "
				+ " (passenger_id, schedule_id, seat_number, ticket_price, status, booking_time) "
				+ "VALUES (?, ?, ?, ?, 'confirmed', datetime('now'))";
		
		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
		{
			ps.setInt(1, passengerId);
			ps.setInt(2, scheduleId);
			ps.setString(3, seatNumber);
			ps.setDouble(4, ticketPrice);
			ps.executeUpdate();
			
			ResultSet keys = ps.getGeneratedKeys();
			if(keys.next())
			{
				int id = keys.getInt(1);
				keys.close();
				return id;
		}
		keys.close();
		return -1;
	} catch (SQLException e )
	{
		System.err.println(e);
		return -1;
	}
	}
	
	
	// READ
	/**
	 * 
	 * This method will:  Returns all bookings for a passenger, newest first.
	 * @param passengerId
	 * @return an ArrayyList object of Bookings objects
	 */
	public static ArrayList<Booking>getBookingsByPassenger(int passengerId)
	{
		String sql = "SELECT booking_id, passenger_id, schedule_id, booking_time, "
        + "       cancellation_time, seat_number, ticket_price, status, is_fraud_flagged "
        + "FROM Bookings "
        + "WHERE passenger_id = ? "
        + "ORDER BY booking_time DESC";
		
		ArrayList<Booking> bookings = new ArrayList<>();
		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql))
		{
			ps.setInt(1, passengerId);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
			{
				bookings.add(mapRow(rs));
				
			}
			rs.close();
			return bookings;
			
		} catch (SQLException e ) {
			System.err.println(e);
			return null;
		}
	}
	/**
	 * 
	 * This method will: Return only confirmed (active) bookings for a passenger.
	 * @param passengerId
	 * @return an ArrayyList object of Bookings objects
	 */
	public static ArrayList<Booking> getConfirmedBookingsByPassenger(int passengerId)
	{
	   String sql = "SELECT booking_id, passenger_id, schedule_id, booking_time, "
         + "       cancellation_time, seat_number, ticket_price, status, is_fraud_flagged "
         + "FROM Bookings "
         + "WHERE passenger_id = ? AND status = 'confirmed' "
         + "ORDER BY booking_time DESC";
	   
			ArrayList<Booking> bookings = new ArrayList<>();
			try (Connection connection = DatabaseConnection.getConnection();
			   PreparedStatement ps = connection.prepareStatement(sql)) {
			  ps.setInt(1, passengerId);
			  ResultSet rs = ps.executeQuery();
			  while (rs.next()) 
			  {
			      bookings.add(mapRow(rs));
			  }
			  rs.close();
			  return bookings;
			} catch (SQLException e) {
			  System.err.println(e);
			  return null;
			}
	}
	
	/**
	 * 
	 * This method will: Fetch a single booking by booking_id.
	 * @param bookingId
	 * @return the Booking, or null if not found
	 */
	public static Booking getBookingById(int bookingId)
	{
		
		 String sql = "SELECT booking_id, passenger_id, schedule_id, booking_time, "
         + " cancellation_time, seat_number, ticket_price, status, is_fraud_flagged "
         + "FROM Bookings "
         + "WHERE booking_id = ?";
		 
		  try (Connection connection = DatabaseConnection.getConnection();
          PreparedStatement ps = connection.prepareStatement(sql)) {
         ps.setInt(1, bookingId);
         ResultSet rs = ps.executeQuery();
         if (rs.next()) 
         {
             Booking b = mapRow(rs);
             rs.close();
             return b;
         }
         rs.close();
         return null;
     } catch (SQLException e) {
         System.err.println(e);
         return null;
     }
	}
	
	/**
	 * 
	 * This method will: Return all bookings in the system (admin view), newest first.
	 * @return
	 */
	public static ArrayList<Booking> getAllBookings()
	{
		String sql = "SELECT booking_id, passenger_id, schedule_id, booking_time, "
				+ "cancellation_time, seat_number, ticket_price, status, is_fraud_flagged "
				+ "FROM Bookings "
				+ "ORDER BY booking_time DESC";
		
		ArrayList<Booking> bookings = new ArrayList<>();
		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql);
				ResultSet rs = ps.executeQuery())
		{
			while (rs.next() ) 
			{
				bookings.add(mapRow(rs));
			}
			return bookings;
		} catch (SQLException e) {
			System.err.println(e);
			return null;
		}
		
	}
	
	/**
	 * 
	 * This method will: Return the total number of bookings across all passengers.
	 * @return int number of bookings
	 */
	public static int getTotalCount()
	{
		String sql = "SELECT COUNT(*) FROM Bookings";
		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql))
		{
			ResultSet rs = ps.executeQuery();
			int count = rs.next() ? rs.getInt(1) : 0;
			rs.close();
			return count;
		} catch (SQLException e) {
			System.err.println(e);
			return 0;
		}
	}

	// UPDATE 
	
	/**
	 * 
	 * This method will: Set a booking status to 'cancelled' and records the cancellation time.
   * 										Only affects bookings that are currently 'confirmed'.
	 * @param bookingId
	 * @return true if a row was updated
	 */
	public static boolean cancelBooking(int bookingId)
	{
		String sql = "UPDATE Bookings "
        + "SET status = 'cancelled', "
        + "cancellation_time = datetime('now') "
        + "WHERE booking_id = ? AND status = 'confirmed'";
	
		
		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql))
		{
			ps.setInt(1, bookingId);
			int rows = ps.executeUpdate();
			return rows > 0;
			
		} catch (SQLException e) {
			System.err.println(e);
			return false;
		}
		
	}
	
	/**
	 * 
	 * This method will: Mark a booking as fraud-flagged
	 * @param bookingId
	 * @return true if a row was updated
	 */
	public static boolean flagBookingAsFraud(int bookingId)
	{
		String sql = "UPDATE Bookings SET is_fraud_flagged = 1 WHERE booking_id = ?";
		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql))
		{
			ps.setInt(1, bookingId);
			int rows = ps.executeUpdate();
			return rows > 0;
			
		} catch (SQLException e ) {
			System.err.println(e);
			return false;
		}
	}
	
	// FRAUD DETECTION QUERIES 
	/**
	 * 
	 * This method will: [R01] Return true if the passenger already has a confirmed booking
   * 										on the same schedule within the last 1 hour. 
	 * @param passengerId
	 * @param scheduleId
	 * @return boolean true if has
	 */
	public static boolean hasDuplicateBooking(int passengerId, int scheduleId)
	{
		String sql = "SELECT COUNT(*) FROM Bookings "
        + "WHERE passenger_id = ? "
        + "  AND schedule_id = ? "
        + "  AND status = 'confirmed' "
        + "  AND booking_time >= datetime('now', '-1 hours')";
		try (Connection connection = DatabaseConnection.getConnection();
		  PreparedStatement ps = connection.prepareStatement(sql)) 
		{
		 ps.setInt(1, passengerId);
		 ps.setInt(2, scheduleId);
		 ResultSet rs = ps.executeQuery();
		 int count = rs.next() ? rs.getInt(1) : 0;
		 rs.close();
		 return count > 0;
		} catch (SQLException e) {
		 System.err.println(e);
		 return false;
		}
	}
	
	/**
	 * 
	 * This method will: [R02] Counts confirmed bookings made by a passenger in the last N minutes.
	 * @param passengerId the passenger to check
	 * @param minutes look-back window (e.g. 10)
	 * @return
	 */
	public static int countBookingsInLastMinutes(int passengerId, int minutes)
	{
		String sql = "SELECT COUNT(*) FROM Bookings "
				+ "WHERE passenger_id = ? "
				+ " AND status = 'confirmed' "
				+ " AND booking_time >= datetime('now', ? || ' minutes')";
		
		try(Connection connection = DatabaseConnection.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql))
		{
			ps.setInt(1, passengerId);
			ps.setString(2, "-" + minutes);
			ResultSet rs = ps.executeQuery();
			int count = rs.next() ? rs.getInt(1) : 0;
			rs.close();
			return count;
			
		} catch (SQLException e) {
			System.err.println(e);
			return 0;
		}
	}
	
	/**
	 * 
	 * This method will: [R03] Counts cancellations made by a passenger in the last 24 hours.
	 * @param passengerId the passenger to check
	 * @return int number of bookings
	 */
	public static int countCancellationsInLast24Hours(int passengerId)
	{
		String sql = "SELECT COUNT(*) FROM Bookings "
				+ "WHERE passenger_id = ? "
				+ " AND status = 'cancelled' "
				+ " AND cancellation_time >= datetime('now', '-24 hours')";
		
		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql))
		{
			ps.setInt(1, passengerId);
			ResultSet rs = ps.executeQuery();
			int count = rs.next() ? rs.getInt(1) : 0;
			rs.close();
			return count;
		} catch (SQLException e) {
			System.err.println(e);
			return 0;
		}
	}
	
	

	/**
	 * This method will: TODO
	 * @param rs
	 * @return
	 * @throws SQLException 
	 */
	private static Booking mapRow(ResultSet rs) throws SQLException
	{
		return new Booking(
        rs.getInt("booking_id"),
        rs.getInt("passenger_id"),
        rs.getInt("schedule_id"),
        rs.getString("booking_time"),
        rs.getString("cancellation_time"),
        rs.getString("seat_number"),
        rs.getDouble("ticket_price"),
        rs.getString("status"),
        rs.getInt("is_fraud_flagged") == 1
    );
	}
}
