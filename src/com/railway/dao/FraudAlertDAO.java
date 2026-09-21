/**
 * Program Name: FraudAlertDAO.java
 * Purpose: Database operations for the FraudAlerts table.
 * @author Ainash Zhumagulova 
 * Date Jun 28, 2026
 */
package com.railway.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.railway.config.DatabaseConnection;
import com.railway.models.FraudAlert;

/**
 * 
 */
public class FraudAlertDAO
{
	//-------------------------------------------------------------------
  // CREATE
  // ------------------------------------------------------------------
	
	/**
	 * 
	 * This method will: Inserts a new fraud alert record.
	 * @param passengerId					the flagged passenger
	 * @param fraudType						rule code, e.g. "R01-DUPLICATE_BOOKING"
	 * @param alertDescription		human-readable explanation
	 * @param severity						"low", "medium", or "high"
	 * @return int								the generated alert_id, or -1 on failure
	 */
	public static int createFraudAlert(int passengerId, String fraudType, String alertDescription, String severity)
	{
		String sql = "INSERT INTO FraudAlerts "
        + " (passenger_id, fraud_type, alert_description, severity, alert_time, is_reviewed) "
        + "VALUES (?, ?, ?, ?, datetime('now'), 0)";
		
		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement  ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS))
		{
			ps.setInt(1, passengerId);
			ps.setString(2, fraudType);
			ps.setString(3, alertDescription);
			ps.setString(4, severity);
			
			ps.executeUpdate();
			
			ResultSet keys = ps.getGeneratedKeys();
			if (keys.next())
			{
				int id = keys.getInt(1);
				keys.close();
				return id;
			}
			keys.close();
			return -1;
		} catch (SQLException e ) {
			System.err.println(e);
			return -1;
		}
	}
	
	// ------------------------------------------------------------------
  // READ
  // ------------------------------------------------------------------

	/**
	 * 
	 * This method will: Return all fraud alerts that have not yet been reviewed, newest first.
	 * @return ArrayList object of FraudAlert objects
	 */
	public static ArrayList<FraudAlert> getUnreviewedAlerts() 
	{
    String sql = "SELECT alert_id, passenger_id, fraud_type, alert_description, "
               + 					" severity, alert_time, is_reviewed "
               + "FROM FraudAlerts "
               + "WHERE is_reviewed = 0 "
               + "ORDER BY alert_time DESC";
    
    ArrayList<FraudAlert> alerts = new ArrayList<>();
    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement ps = connection.prepareStatement(sql);
    		
         ResultSet rs = ps.executeQuery()) 
    {
        while (rs.next()) 
        {
            alerts.add(mapRow(rs));
        }
        return alerts;
    } catch (SQLException e) {
        System.err.println(e);
        return null;
    }
	}
	
	/**
	 * 
	 * This method will: Return ALL fraud alerts (reviewed and unreviewed), newest first.
	 * @return ArrayList object of FraudAlert objects
	 */
	public static ArrayList<FraudAlert> getAllAlerts()
	{
		String sql = "SELECT alert_id, passenger_id, fraud_type, alert_description, "
				+ "severity, alert_time, is_reviewed "
				+ "FROM FraudAlerts "
				+ "ORDER BY alert_time DESC";
		
		ArrayList<FraudAlert>alerts = new ArrayList<>();
		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql);
				ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				alerts.add(mapRow(rs));
			}
			return alerts;
		} catch (SQLException e) {
			System.err.println(e);
			return null;
		}
	}
	
	/**
	 * 
	 * This method will: Return all fraud alerts for a specific passenger, newest first.
	 * @param passengerId
	 * @return ArrayList object of FraudAlert objects
	 */
	public static ArrayList<FraudAlert> getAlertsByPassenger (int passengerId)
	{
		String sql = "SELECT alert_id, passenger_id, fraud_type, alert_description, "
				+ "severity, alert_time, is_reviewed "
				+ "FROM FraudAlerts "
				+ "WHERE passenger_id = ? "
				+ "ORDER BY alert_time DESC";
		
		ArrayList<FraudAlert>alerts = new ArrayList<>();
		try(Connection connection = DatabaseConnection.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql))
		{
			ps.setInt(1, passengerId);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				alerts.add(mapRow(rs));
			}
			rs.close();
			return alerts;
		} catch (SQLException e ) {
			System.err.println(e);
			return null;
		}
	}
	
	/**
	 * 
	 * This method will: Return the total number of fraud alerts ever created.
	 * @return int number of unreviewed alerts
	 */
	public static int getUnreviewedCount()
	{
		 String sql = "SELECT COUNT(*) FROM FraudAlerts WHERE is_reviewed = 0";
		 
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
	
	/**
	 * 
	 * This method will: Return total number of fraud alerts ever created.
	 * @return
	 */
	public static int getTotalCount()
	{
		String sql = "SELECT COUNT(*) FROM FraudAlerts";
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
	/**
	 * 
	 * This method will: Mark a single alert as reviewed by an admin.
	 * @param alertId
	 * @return true if a row was updated
	 */
	public static boolean markAsReviewed(int alertId)
	{
		String sql = "UPDATE FraudAlerts SET is_reviewed = 1 WHERE alert_id = ?";
		
    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement ps = connection.prepareStatement(sql)) 
    {
        ps.setInt(1, alertId);
        int rows = ps.executeUpdate();
        return rows > 0;
    } catch (SQLException e) {
        System.err.println(e);
        return false;
    }
	}
	
	/**
	 * 
	 * This method will: Mark all unreviewed alerts for a passenger as reviewed (bulk action).
	 * @param passengerId
	 * @return int 	the number of rows updated
	 */
	public static int markAllReviewedForPassenger(int passengerId)
	{
		 String sql = "UPDATE FraudAlerts "
         + "SET is_reviewed = 1 "
         + "WHERE passenger_id = ? AND is_reviewed = 0";
		 
			try (Connection connection = DatabaseConnection.getConnection();
			   PreparedStatement ps = connection.prepareStatement(sql)) 
			
			{
			  ps.setInt(1, passengerId);
			  return ps.executeUpdate();
			} catch (SQLException e) {
			  System.err.println(e);
			  return 0;
				}			
		}
	
	// Helper

	/**
	 * This method will: TODO
	 * @param rs
	 * @return
	 * @throws SQLException 
	 */
	private static FraudAlert mapRow(ResultSet rs) throws SQLException
	{
		return new FraudAlert(
        rs.getInt("alert_id"),
        rs.getInt("passenger_id"),
        rs.getString("fraud_type"),
        rs.getString("alert_description"),
        rs.getString("severity"),
        rs.getString("alert_time"),
        rs.getInt("is_reviewed") == 1
        );
	}
}
