/**
 * Program Name: LoginController.java
 * Purpose: Login screen — authenticates a username/password against the Users table and routes to the correct dashboard based
 *          on role (AdminDashboard for admins, PassengerDashboard for passengers). Also provides navigation to registration.
 * @author Ainash Zhumagulova 
 * Date Jun 29, 2026
 */
package com.railway.ui;

import com.railway.dao.PassengerDAO;
import com.railway.dao.UserDAO;
import com.railway.models.Passenger;
import com.railway.models.User;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Login screen
 * All visual styling comes from style.css
 */
public class LoginController
{
	private final AppContext context;

	/**
	 * Constructor a new LoginController object
	 * @param context
	 */
	public LoginController(AppContext context)
	{
		this.context = context;
	}
	// end main
	public  void show()
	{
		Stage stage = context.getStage();
		
		
		// -------------- Header ------------
		Label logo = new Label("");			// Image
		logo.getStyleClass().add("login-logo");
		
		Label title = new Label("Railway Ticketing System");
		title.getStyleClass().add("login-title");
		
		Label sub = new Label("Sign in to your account");
		sub.getStyleClass().add("login-subtitle");
		
		VBox header = new VBox(6, logo, title, sub);
		header.setAlignment(Pos.CENTER);
		header.setPadding(new Insets(0, 0, 24, 0));
		
		// ---------- Fields --------------
		Label userLabel  = new Label("Username");
		userLabel.getStyleClass().add("field-label");
		
		TextField usernameField = new TextField();
		usernameField.setPromptText("Enter your username");
		
		Label passLabel = new Label("Password");
		passLabel.getStyleClass().add("field-label");
		
		PasswordField passwordField = new PasswordField();
		passwordField.setPromptText("Enter your password");
		
		Label errorLabel = new Label();
		errorLabel.getStyleClass().add("error-label");
		errorLabel.setVisible(false);
		errorLabel.setWrapText(true);
		
		// --------------------- Buttons --------------
		Button loginBtn = new Button("Login");
		loginBtn.getStyleClass().add("btn-primary");
		loginBtn.setMaxWidth(Double.MAX_VALUE);
		loginBtn.setPrefHeight(44);
		
		Button registerBtn = new Button("Create New Account");
		registerBtn.getStyleClass().add("btn-outline");
		registerBtn.setMaxWidth(Double.MAX_VALUE);
		registerBtn.setPrefHeight(40);
		
		// ------------------- Card ------------------
		VBox form = new VBox(10, 
				userLabel, usernameField, 
				passLabel, passwordField, 
				errorLabel, 
				loginBtn, new Separator(),
				registerBtn
			);
		form.setPadding(new Insets(32));
		form.setMaxWidth(420);
		form.getStyleClass().add("card");
		
		VBox root = new VBox(header, form);
		root.setAlignment(Pos.CENTER);
		root.getStyleClass().add("page-bg");
		root.setPadding(new Insets(60, 20, 60, 20));
		
		// ----------- Login Logic ------------
		Runnable doLogin = () -> {
			String username = usernameField.getText().trim();
			String password = passwordField.getText();
			
			if(username.isEmpty() || password.isEmpty())
			{
				showError(errorLabel, "Please enter both username and password.");
				return;
			}
			
			User user = UserDAO.authenticate(username, password);
			if(user == null)
			{
				showError(errorLabel, "Invalid username or password.");
				passwordField.clear();
				return;
			}
			
			// Store user in shared context
			context.setUser(user);
			errorLabel.setVisible(false);
			
			// TEMPORARILY COMMENT OUT NAVIGATION
	    // System.out.println("Login successful! User: " + user.username() + ", Role: " + user.role());
	    
	    // Show a success message 
	    showError(errorLabel, "Login successful! Welcome " + user.username() + "!");
	    errorLabel.setStyle("-fx-text-fill: green;");
			
	    
	    
			if(user.isAdmin())
			{
				new AdminDashboard(context).show();
			}
			else
			{
				Passenger passenger = PassengerDAO.getPassengerByUserId(user.userId());
				if(passenger == null)
				{
					showError(errorLabel, "Passenger profile not found. Please contact support.");
					return;
				}
				context.setCurrentPassenger(passenger);
				new PassengerDashboard(context).show();
			}	
		};
		
		loginBtn.setOnAction(e -> doLogin.run());
		passwordField.setOnAction(e -> doLogin.run());
		registerBtn.setOnAction(e -> new RegisterController(context).show());
		
	  // ── Scene ─────────────────────────────────────────────
    Scene scene = new Scene(root, 900, 650);
    applyStylesheet(scene);
    stage.setScene(scene);
		
	}	
		

    static void showError(Label label, String msg) {
        label.setText(msg);
        label.setVisible(true);
    }
    
    // Shared helpers
    
  
    /**
     * This method will: Loads styles.css from the resources/ source folder.
     * @param scene
     */
    static void applyStylesheet(Scene scene) {
      java.net.URL cssUrl = LoginController.class.getResource("/styles.css");

      if (cssUrl == null) {
          // Print classpath entries so you can see exactly where Java is looking
          System.err.println("[CSS] styles.css not found on classpath.");
          System.err.println("[CSS] Classpath entries:");
          String cp = System.getProperty("java.class.path");
          for (String entry : cp.split(System.getProperty("path.separator"))) {
              System.err.println("      " + entry);
          }
          System.err.println("[CSS] Fix: right-click resources/ in Eclipse " +
              "-> Build Path -> Use as Source Folder, then Project -> Clean.");
          return;
      }

      scene.getStylesheets().add(cssUrl.toExternalForm());
      //System.out.println("[CSS] Loaded: " + cssUrl.toExternalForm());
  }


		
		
	
	
}
