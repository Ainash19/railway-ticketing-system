/**
 * Program Name: ScheduleDAO.java
 * Purpose: Database operations for the Schedules table.
 * @author Ainash Zhumagulova 
 * Date Jun 26, 2026
 */
package com.railway.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.railway.config.DatabaseConnection;
import com.railway.models.Schedule;

/**
 * 
 */
public class ScheduleDAO
{
	// READ
	
	/**
	 * 
	 * This method will: Return all schedules for a specific train, ordered by departure date and time.
	 * @param trainId
	 * @return an ArryList object that contains Schedules objects, stored in Schedule table of railway.db
	 * 					and sorted in ascending order by the departure_date and departure_time columns
	 */
	public  static ArrayList<Schedule>getSchedulesByTrain(int trainId)
	{
		String sql = "SELECT schedule_id, train_id, departure_date, departure_time, "
        + "       arrival_date, arrival_time, available_seats "
        + "FROM Schedules "
        + "WHERE train_id = ? "
        + "ORDER BY departure_date ASC, departure_time ASC";
		
		// creates an ArrayList object for storing Schedule objects 
		ArrayList<Schedule> schedules = new ArrayList<>();
    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement ps = connection.prepareStatement(sql)) 
    		{
        ps.setInt(1, trainId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            schedules.add(mapRow(rs));
        }
        rs.close();
        return schedules;
    } catch (SQLException e) {
        System.err.println(e);
        return null;
    }
	}
	
	/**
	 * 
	 * This method will: Returns schedules for a specific train on a specific departure date,
   * 										with at least one available seat.
	 * @param trainId
	 * @param date
	 * @return 
	 */
	public static ArrayList<Schedule>getSchedulesByTrainAndDate(int trainId, String date)
	{
		 String sql = "SELECT schedule_id, train_id, departure_date, departure_time, "
         + "       arrival_date, arrival_time, available_seats "
         + "FROM Schedules "
         + "WHERE train_id = ? AND departure_date = ? AND available_seats > 0 "
         + "ORDER BY departure_time ASC";
		 
		ArrayList<Schedule> schedules = new ArrayList<>();
		try (Connection connection = DatabaseConnection.getConnection();
		   PreparedStatement ps = connection.prepareStatement(sql)) 
			{
		  ps.setInt(1, trainId);
		  ps.setString(2, date);
		  ResultSet rs = ps.executeQuery();
		  while (rs.next()) {
		      schedules.add(mapRow(rs));
		  }
		  rs.close();
		  return schedules;
		} catch (SQLException e) {
		  System.err.println(e);
		  return null;
		}
	}
	
	/**
	 * 
	 * This method will: Fetch a single schedule by schedule_id.
	 * @param scheduleId
	 * @return  the Schedule, or null if not found
	 */
	public static Schedule getScheduleById(int scheduleId)
	{
		String sql = "SELECT schedule_id, train_id, departure_date, departure_time, "
				+ " arrival_date, arrival_time, available_seats "
				+ "FROM Schedules "
				+ "WHERE schedule_id = ?";
		
		try(Connection connection = DatabaseConnection.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql))
		{
			ps.setInt(1, scheduleId);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
			{
				Schedule s = mapRow(rs);
				rs.close();
				return s;
			}
			rs.close();
			return null;
		} catch (SQLException e ) {
			System.err.println(e);
			return null;
		}
	}
	
	/**
	 * 
	 * This method will: Returns all seat numbers already confirmed-booked for a given schedule.
   * 										Used by the seat map to mark occupied seats.
	 * @param scheduleId
	 * @return
	 */
	public static ArrayList<String>getBookedSeatNumbers(int scheduleId)
	{
		String sql = "SELECT seat_number FROM Bookings "
        + "WHERE schedule_id = ? AND status = 'confirmed'";
		
			ArrayList<String> seats = new ArrayList<>();
			try (Connection connection = DatabaseConnection.getConnection();
			  PreparedStatement ps = connection.prepareStatement(sql)) {
			 ps.setInt(1, scheduleId);
			 ResultSet rs = ps.executeQuery();
			 while (rs.next()) 
			 {
			     seats.add(rs.getString("seat_number"));
			 }
			 rs.close();
			 return seats;
			} catch (SQLException e) {
			 System.err.println(e);
			 return null;
			}
	}
	
	// UPDATE
	
	/**
	 * 
	 * This method will: Decrements available_seats by 1 when a booking is confirmed.
   *										 Will not go below 0 (guarded by WHERE clause).
	 * @param ScheduleId
	 * @return true if the update succeeded
	 */
	public static boolean decrementAvailableSeats(int ScheduleId)
	{
		String sql = "UPDATE Schedules "
				+ "SET available_seats = available_seats - 1 "
				+ "WHERE schedule_id = ? AND available_seats > 0";
		
		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql))
		{
			ps.setInt(1, ScheduleId);
			int rows = ps.executeUpdate();
			return rows > 0;
		} catch (SQLException  e ) {
			System.err.println(e);
			return false;
		}
	}
	
	/**
	 * 
	 * This method will: Increments available_seats by 1 when a booking is cancelled.
	 * @param scheduleId
	 * @return true if the update succeeded
	 */
	public static boolean incrementAvailableSeats(int scheduleId)
	{
		String sql = "UPDATE Schedules "
				+ "SET available_seats = available_seats + 1 "
				+ "WHERE schedule_id = ?";
		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql))
		{
			ps.setInt(1, scheduleId);
			int rows = ps.executeUpdate();
			return rows > 0;
			
		} catch (SQLException e) {
			System.err.println(e);
			return false;
		}
	}
	
	
	// HELPER

	/**
	 * This method will: TODO
	 * @param rs
	 * @return
	 * @throws SQLException 
	 */
	private static Schedule mapRow(ResultSet rs) throws SQLException
	{
		return new Schedule(
        rs.getInt("schedule_id"),
        rs.getInt("train_id"),
        rs.getString("departure_date"),
        rs.getString("departure_time"),
        rs.getString("arrival_date"),
        rs.getString("arrival_time"),
        rs.getInt("available_seats")
    );
	}
}
