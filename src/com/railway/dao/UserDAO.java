/**
 * Program Name: UserDAO.java
 * Purpose: This class works with data in the Users table of railway.db database
 * @author Ainash Zhumagulova 
 * Date Jun 26, 2026
 */
package com.railway.dao;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

import com.railway.config.DatabaseConnection;
import com.railway.models.User;
/**
 * Each method opens its own connection and closes it via try-with-resources.
 */
public class UserDAO
{
	
	// READ
	
	/**
	 * Authenticates a user by user name  + password.
	 * The password is SHA-256 hashed before comparing to the stored hash. (using SHA-256 hashing algorithm:)
	 * So the real password never stored in the database (security)
	 * 
	 * @return the matching User, or null if credentials are invalid
	 */
	public static User authenticate(String username, String plainPassword) 
	{
		String hash = sha256(plainPassword);

    // DEBUG 
    //System.out.println("[AUTH] username: " + username);
    //System.out.println("[AUTH] hash produced: " + hash);
    
    
    
		 String sql = "SELECT user_id, username, password_hash, email, role "
         				+ "FROM Users "
         				+ "WHERE username = ? AND password_hash = ?";	// ? - to specify a parameter for a prepared statement 
		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql))
		{
			ps.setString(1, username);
			ps.setString(2, hash);
			ResultSet rs = ps.executeQuery();
			// Move the cursor to the next row in the result set
			// (returns true if the next roe exists, otherwise false)
			if(rs.next())
			{
				User user = mapRow(rs);
				rs.close();
				return user;
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
	 * This method will: fetch a single user by username 
	 * @param username
	 * @return the User, or null if not found
	 */
	public static User getUserByUsername(String username) 
	{
		 String sql = "SELECT user_id, username, password_hash, email, role "
         				+ "FROM Users "
         				+ "WHERE username = ?";
		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql))
		{
			ps.setString(1, username);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
			{
				User user = mapRow(rs);
				rs.close();
				return user;
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
	 * This method will: fetch a single user by user_id
	 * @param userId
	 * @return the User, or null if not found 
	 */
	public static User getUserById(int userId)
	{
		 String sql = "SELECT user_id, username, password_hash, email, role "
         				+ "FROM Users "
         				+ "WHERE user_id = ?";
		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql))
		{
			ps.setInt(1, userId);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
			{
				User user = mapRow(rs);
				rs.close();
				return user;
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
 * This method will: check if the given username already exists in the User table
 * @param username
 * @return return true if the given username already exists 
 */
	public static boolean usernameExists(String username)
	{
		String sql = "SELECT 1 FROM Users WHERE username = ?";
		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql))
		{
			ps.setString(1, username);
			ResultSet rs = ps.executeQuery();
			boolean exists = rs.next();
			rs.close();
			return exists;
			
		} catch (SQLException e) {
			System.err.println(e);
			return false;
		}
		
	}
	/**
	 * 
	 * This method will: check if the given email already exists in the User table
	 * @param email
	 * @return return true if the given email already exists 
	 */
	public static boolean emailExists(String email)
	{
		String sql = "SELECT 1 FROM Users WHERE email = ?";
		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql))
		{
			ps.setString(1, email);
			ResultSet rs = ps.executeQuery();
			boolean exists = rs.next();
			rs.close();
			return exists;
			
		} catch (SQLException e) {
			System.err.println(e);
			return false;
		}
		
	}
	
	
	// CREATE 
	/**
	 * 
	 * This method will: TODO
	 * @param username
	 * @param plainPassword
	 * @param email
	 * @return
	 */
	public static int createUser(String username, String plainPassword, String email)
	{
		String hash = sha256(plainPassword);

    // DEBUG — remove after fixing
    // System.out.println("[REGISTER] username: " + username);
    // System.out.println("[REGISTER] hash stored: " + hash);
    
    
    
		String sql = "INSERT INTO Users (username, password_hash, email, role) "
        + "VALUES (?, ?, ?, 'passenger')";
		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
		{
			ps.setString(1, username);
			ps.setString(2, sha256(plainPassword));
			ps.setString(3, email);
			ps.executeUpdate();
			
			// Retrieve the auto-generated user_id
			ResultSet keys = ps.getGeneratedKeys();
			if(keys.next())
			{
				int id = keys.getInt(1);
				keys.close();
				return id;
			}
			keys.close();
			return -1;
			
		} catch (SQLException e) {
			System.err.println(e);
			return -1;
		}
	}
	
	
	
	// HELPER 
	
	/**
	 * This method will: 
	 * @param rs
	 * @return
	 */
	private static User mapRow(ResultSet rs) throws SQLException
	{
		
		return new User (
				rs.getInt("user_id"),
        rs.getString("username"),
        rs.getString("password_hash"),
        rs.getString("email"),
        rs.getString("role")
				);
	}

	/**
	 * This method will: hash passwords before storage and comparison
	 * @param plainPassword
	 * @return a lowercase hex SHA-256 digest of the input string
	 */
	private static String sha256(String input)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA-256");
		  // Use UTF-8 — never rely on platform default encoding
			byte[] hash = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for(byte b : hash)
			{
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e)
		{
			throw new RuntimeException ("SHA-256 not available", e);
		}
	
	}
	
}
