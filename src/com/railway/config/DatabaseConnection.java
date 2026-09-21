/**
 * Program Name: DatabaseConnection.java
 * Purpose: This class will work with data in the railway.db 
 * 
 * @author Ainash Zhumagulova 
 * Date Jun 26, 2026
 */
package com.railway.config;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * The database file railway.db  already exist (created via DB Browser for SQLite)
 * and contain all required tables before the app runs.
 * No tables are created here — this class only opens connections.
 */
public class DatabaseConnection
{
	// Path to the pre-created SQLite database file.
  // If railway.db is in the Eclipse project root, this path works as-is.
  // If it is elsewhere, use an absolute path, e.g.: "jdbc:sqlite:C:/.../railway.db"
	//private static final String DB_URL = "jdbc:sqlite:railway.db";
	
	private static final String DB_PATH = "database/railway.sqlite";
  private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;
	
	
	/**
	 * 
	 * A static method getConnection() of the DriverManager class that connects to a SQLite database in the working directory
	 * Opens and returns a new JDBC connection to railway.db.
   * The caller is responsible for closing it (use try-with-resources).
   *
   * @throws a Connection object 
   * 					SQLException if the driver is missing or the file cannot be opened
	 */
	public static Connection getConnection() throws SQLException
	{
			try 
			{
		    Class.forName("org.sqlite.JDBC");
			} catch (ClassNotFoundException e) {
		    throw new SQLException(
		        "SQLite JDBC driver not found. " +
		        "Add sqlite-jdbc-3.46.1.3.jar to the project build path.", e);
			}
			
		   // Debug: Check if file exists
      File dbFile = new File(DB_PATH);
      System.out.println("Looking for database at: " + dbFile.getAbsolutePath());
      System.out.println("Database file exists: " + dbFile.exists());
      
      
			return DriverManager.getConnection(DB_URL);
		
	}
}
