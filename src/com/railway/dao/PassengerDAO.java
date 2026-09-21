/**
 * Program Name: Passenger.java
 * Purpose: Database operations for the Passengers table.
 * @author Ainash Zhumagulova 
 * Date Jun 26, 2026
 */
package com.railway.dao; // Data Access Object

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.railway.config.DatabaseConnection;
import com.railway.models.Passenger;

/**
 * 
 */
public class PassengerDAO
{
	
	// READ
	/**
	 * 
	 * This method will: Fetch a passenger profile by the linked user_id.
	 * @param userId
	 * @return the Passenger, or null if not found
	 */
	 public static Passenger getPassengerByUserId(int userId) {
	   String sql = "SELECT passenger_id, user_id, full_name, phone, email "
	              + "FROM Passengers "
	              + "WHERE user_id = ?";
	   try (Connection connection = DatabaseConnection.getConnection();
	        PreparedStatement ps = connection.prepareStatement(sql))
	   {
	       ps.setInt(1, userId);
	       ResultSet rs = ps.executeQuery();
	       if (rs.next()) 
	       {
	           Passenger p = mapRow(rs);
	           rs.close();
	           return p;
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
	  * This method will: Fetch a passenger profile by passenger_id.
	  * @param passengerId
	  * @return the Passenger, or null if not found
	  */
	 public static Passenger getPassengerById(int passengerId) {
     String sql = "SELECT passenger_id, user_id, full_name, phone, email "
                + "FROM Passengers "
                + "WHERE passenger_id = ?";
     try (Connection connection = DatabaseConnection.getConnection();
          PreparedStatement ps = connection.prepareStatement(sql)) {
         ps.setInt(1, passengerId);
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
             Passenger p = mapRow(rs);
             rs.close();
             return p;
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
	  * This method will: TODO
	  * @return the total number of registered passengers
	  */
	  public static int getTotalCount() {
      String sql = "SELECT COUNT(*) FROM Passengers";
      try (Connection connection = DatabaseConnection.getConnection();
           PreparedStatement ps = connection.prepareStatement(sql)) {
          ResultSet rs = ps.executeQuery();
          int count = rs.next() ? rs.getInt(1) : 0;
          rs.close();
          return count;
      } catch (SQLException e) {
          System.err.println(e);
          return 0;
      }
  }
	  
	  // CREATE
	  /**
	   * 
	   * This method will: Insert a new passenger profile and returns the generated passenger_id.
	   * @param userId
	   * @param fullName
	   * @param phone
	   * @param email
	   * @return the new passenger_id, or -1 on failure
	   * 
	   * Note: 
	   * INSERT + need the generated ID back → prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
	   * INSERT + don't need the ID → prepareStatement(sql)
		 * SELECT / UPDATE / DELETE → prepareStatement(sql)
	   */
	  
	 
	  public static int createPassenger(int userId, String fullName, String phone, String email) {
      String sql = "INSERT INTO Passengers (user_id, full_name, phone, email) "
                 + "VALUES (?, ?, ?, ?)";
      try (Connection connection = DatabaseConnection.getConnection();
           PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
          ps.setInt(1, userId);
          ps.setString(2, fullName);
          ps.setString(3, phone);
          ps.setString(4, email);
          ps.executeUpdate();

          ResultSet keys = ps.getGeneratedKeys();
          if (keys.next()) {
              int id = keys.getInt(1);
              keys.close();
           
              return id;
          }
          keys.close();
        
          return -1;
      } 
      catch (SQLException e) 
      {
        //System.err.println(e);
      	// DEBUG — prints the actual SQL error
        System.err.println("[PASSENGER] createPassenger failed: " + e.getMessage());
        System.err.println("[PASSENGER] userId=" + userId + " fullName=" + fullName);
      	return -1;
      }
  }

	/**
	 * This method will: TODO
	 * @param rs
	 * @return
	 * @throws SQLException 
	 */
	private static Passenger mapRow(ResultSet rs) throws SQLException
	{
		return new Passenger(
        rs.getInt("passenger_id"),
        rs.getInt("user_id"),
        rs.getString("full_name"),
        rs.getString("phone"),
        rs.getString("email")
    );

	}
	
	
	
}
