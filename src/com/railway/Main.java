/**
 * Program Name: Main.java
 * Purpose: Application entry point.
 * 
 * 					This class serves as the bootstrap for the JavaFX application, handling:
 * - Application initialization and window configuration
 * - Database connection verification
 * - Error handling for missing database resources
 * - Navigation to the login screen upon successful startup
 * 
 * The database file (railway.db) must exist in the project root directory,
 * created via DB Browser for SQLite with the provided schema.sql script.
 * @author Ainash Zhumagulova #1330403
 * Date Jun 29, 2026
 */
package com.railway;


import com.railway.ui.LoginController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.SQLException;

import com.railway.config.DatabaseConnection;
import com.railway.ui.AppContext;

/**
 * Main Application Entry Point
 * Launches the Railway Ticketing System
 * 
 * Start-up sequence:
 *   1. Create the AppContext (holds stage + session data for the state management)
 *   2. Verify railway.db is reachable via DatabaseConnection
 *   3. If connection successful → pass context to LoginController and show login screen
 *   4. If connection fails → display a clear error screen with troubleshooting steps
 *   
 *   Design Decisions:
 * - Uses AppContext as a singleton-like state container to avoid passing Stage
 *   and user session data through multiple controller layers
 * - Implements graceful degradation with user-friendly error messages
 * - Follows MVC pattern: Main acts as the controller for application lifecycle
 */
public class Main extends Application 
{

	/*  (non-JavaDoc)
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	@Override
	public void start(Stage stage) throws Exception
	{
		// Configure primary application window
		stage.setTitle("Railway Ticketing System");
		stage.setMinWidth(900);
		stage.setMinHeight(650);
		
		// Ensure application exits cleanly when window is closed
		stage.setOnCloseRequest(e -> Platform.exit());
		

		// Initialize application context to manage shared state across controllers
    AppContext context = new AppContext(stage);
	
    // Verify database connectivity before proceeding
		if(!isDatabaseReachable()) 
		{
			showDbErrorScreen(stage);
		}
		else
		// Database connection successful -> proceed to login
		{
			 new LoginController(context).show();
		}
		
		// Display the primary stage
		stage.show();
		
	}
	
	/**
	 * This method will: 	Displays an error screen when the database connection fails.
   * 										Provides clear troubleshooting instructions to help users resolve the issue.
	 * @param stage The primary stage to display the error screen on
	 */
	private void showDbErrorScreen(Stage stage)
	{
		Label icon = new Label("!"); 		
		icon.setStyle("-fx-font-size: 40px;");
		
		Label heading = new Label("Database not found");
		heading.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #dc2626;");
		
		// Detailed troubleshooting instructions
		Label detail = new Label (
					"Could not connect to railway.db.\n\n" +
					"Make sure you have: \n" +
					"	1. Created railway.db in DB Browser for SQLite\n" +
					"	2. Run schema.sql to create all tables.\n" + 
					"	3. Placed railway.db in the project root folder\n" +
					"			(same level as the src)"
				);
		detail.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");
    detail.setWrapText(true);
    
    // Display the expected file path for debugging
    Label path = new Label("Expected path:  <project-root>/railway.db");
    path.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280; -fx-font-style: italic;");
    
    // Create a card-style container with shadow effect
    VBox box = new VBox(14, icon, heading, detail, path);
    box.setAlignment(Pos.CENTER);
    box.setMaxWidth(480);
    box.setPadding(new Insets(40));
    box.setStyle(
    		"-fx-background-color: white;" +
    		"-fx-background-radius: 12px;" +
    		"-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 12, 0, 0, 4);");
    
    // Center the card on screen 
    VBox root = new VBox(box);
    root.setAlignment(Pos.CENTER);
    root.setStyle("-fx-background-color: #f0f4f8;");
    
    // Set the error scene
    stage.setScene(new Scene(root, 900, 500));
		
		// Empty point
    
		
	}
	
	/**
	 * This method will: Verifies that the database connection is available and responsive.
   * 										Uses try-with-resources to ensure proper connection cleanup.
   * 
   * This method serves as a health check for the application's database dependency.
	 * @return  true if a connection can be established, false otherwise
	 * @throws SQLException 
	 */
	private boolean isDatabaseReachable() 
	{
		// Try-with-resources automatically closes the connection
		try (Connection connection = DatabaseConnection.getConnection())
		{
			// Connection successful if we get a non-null connection
			return connection != null;
		} 
		catch (SQLException e ) 
		{
			// Log the error for debugging while providing a graceful user experience
			System.err.println("[Main] Database connection failed: " + e.getMessage());
			return false;
		}
	}
	



	/**
	 * This method will:  Standard application entry point.
   * 										Launches the JavaFX application.
	 * @param args Command line arguments (not used)
	 */
	public static void main(String[] args)
	{
		launch(args);
		

	}

	
} // end Main
