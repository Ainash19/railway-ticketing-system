/**
 * Program Name: TrainDAO.java
 * Purpose: Database operations for the Trains table.
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
import com.railway.models.Train;

/**
 * 
 */
public class TrainDAO
{
	/**
	 * This method will: Retrieves all active trains from the database.
	 * @return Array list of active trains
	 */
	public static ArrayList<Train> getAllTrain() 
	{
		String sql = "SELECT train_id, train_number, train_name, source_station, "
				+ "destination_station, total_seats, base_fare, is_active "
				+ "FROM Trains "
				+ "WHERE is_active = 1 " 					// active trains only
				+ "ORDER BY train_number ASC";		// results by train number alphabetically
		ArrayList<Train> trains = new ArrayList<>();
		try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
       while (rs.next()) {
           trains.add(mapRow(rs));
       }
       return trains;
       
   } catch (SQLException e) {
       System.err.println(e);
       return null;
   }
		
	}
	/**
	 * 
	 * This method will: 1. Search active trains by source and/or destination station (case-insensitive, partial match).
	 * 									 2. Pass an empty string for either parameter to skip that filter.
	 * @param source
	 * @param destination
	 * @return Array lists of Trains objects matching the search criteria, or null if 
	 * 				a database error occurs
	 */
	public static ArrayList<Train> searchTrains(String source, String destination)
	{
		// SQL query with placeholders for dynamic filtering
		 String sql = "SELECT train_id, train_number, train_name, "
         + "       source_station, destination_station, total_seats, base_fare, is_active "
         + "FROM Trains "
         + "WHERE is_active = 1 "
         // make case - insensitive
         + "  AND (? = '' OR LOWER(source_station)      LIKE LOWER(?)) "
         + "  AND (? = '' OR LOWER(destination_station) LIKE LOWER(?)) "
         + "ORDER BY train_number ASC";
		 
		 ArrayList<Train> trains = new ArrayList<>();
		 try (Connection connection = DatabaseConnection.getConnection();
				 PreparedStatement ps = connection.prepareStatement(sql))
		 {
			 String srcPattern = source.isBlank() ? "" : "%" + source.trim() + "%";
			 String dstPattern = destination.isBlank() ? "" : "%" + destination.trim() + "%";
			 ps.setString(1, srcPattern);		// For the OR condition check
			 ps.setString(2, srcPattern);		// For the LIKE pattern
			 ps.setString(3, dstPattern);
			 ps.setString(4, dstPattern);
			 ResultSet rs = ps.executeQuery();
			 while (rs.next())
			 {
				 trains.add(mapRow(rs));
			 }
			 rs.close();
			 return trains;
			 
		 } catch (SQLException e) {
       System.err.println(e);
       return null;
		 }
	}
	/**
	 * 
	 * This method will: Fetch a single train by its train_id.
	 * @param trainId
	 * @return the Train, or null if not found
	 */
	public static Train getTrainById(int trainId)
	{
		String sql = "SELECT train_id, train_number, train_name, "
        + "source_station, destination_station, total_seats, base_fare, is_active "
        + "FROM Trains "
        + "WHERE train_id = ?";
		
		try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement ps = connection.prepareStatement(sql)) {
       ps.setInt(1, trainId);
       ResultSet rs = ps.executeQuery();
       if (rs.next()) 
       {
         Train t = mapRow(rs);
         rs.close();
         return t;
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
	 * This method will: Return all distinct source stations (for ComboBox population).
	 * @return Array list of Strings 
	 */
	public static ArrayList<String> getAllSourceStations()
	{
		String sql = "SELECT DISTINCT source_station FROM Trains "
        + "WHERE is_active = 1 ORDER BY source_station ASC";
		ArrayList<String> stations = new ArrayList<>();
		try (Connection connection = DatabaseConnection.getConnection();
				 PreparedStatement ps = connection.prepareStatement(sql);
				 ResultSet rs = ps.executeQuery()) {
			 while (rs.next()) 
			 {
			     stations.add(rs.getString("source_station"));
			 }
			 return stations;
		 
		} catch (SQLException e) {
		 System.err.println(e);
		 return null;
		}
	}
	
	/**
	 * 
	 * This method will: Returns all distinct destination stations (for ComboBox population).
	 * @return
	 */
	public static ArrayList<String> getAllDestinationStations() {
    String sql = "SELECT DISTINCT destination_station FROM Trains "
               + "WHERE is_active = 1 ORDER BY destination_station ASC";
    ArrayList<String> stations = new ArrayList<>();
    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement ps = connection.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            stations.add(rs.getString("destination_station"));
        }
        return stations;
    } catch (SQLException e) {
        System.err.println(e);
        return null;
    }
}

	// HELPER
	/**
	 * This method will: TODO
	 * @param rs
	 * @return
	 */
	private static Train mapRow(ResultSet rs) throws SQLException {
    return new Train(
        rs.getInt("train_id"),
        rs.getString("train_number"),
        rs.getString("train_name"),
        rs.getString("source_station"),
        rs.getString("destination_station"),
        rs.getInt("total_seats"),
        rs.getDouble("base_fare"),
        rs.getInt("is_active") == 1
    );
}
}
