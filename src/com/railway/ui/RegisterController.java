/**
 * Program Name: RegisterController.java
 * Purpose: Registration screen — validates and creates a new passenger account (username, email, password, optional phone), 
 * 					then creates the linked Passenger profile and returns to login.
 * @author Ainash Zhumagulova 
 * Date Jul 1, 2026
 */
package com.railway.ui;

import com.railway.dao.PassengerDAO;
import com.railway.dao.UserDAO;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * 
 */
public class RegisterController
{
	private final AppContext context;
	/**
	 * 
	 * Constructor a new RegisterController object
	 * @param context shared application context
	 */
	public RegisterController(AppContext context) 
	{
		this.context = context;
	}
	
	/**
	 * 
	 * This method will: Builds and displays the registration screen.
	 */
	public void show() 
	{
		Stage stage = context.getStage();
		
		//-------------- Header -----------------
		Label title = new Label("Create Account");
		title.getStyleClass().add("login-title");
		
		Label sub = new Label("Join the Railway Ticketing System");
		sub.getStyleClass().add("login-subtitle");
		
		VBox header = new VBox(4, title, sub);
		header.setAlignment(Pos.CENTER);
		header.setPadding(new Insets(0, 0, 20, 0));
		
		
		//------------------- Fields --------------------
		TextField fullNameField = field("Full Name");
		TextField usernameField = field("Username");
		TextField emailField = field("Email Address");
		TextField phoneField = field("Phone Number (optional)");
		
		PasswordField passField = new PasswordField();
		passField.setPromptText("Password (min 6 characters)");
		
		PasswordField confirmField = new PasswordField();
		confirmField.setPromptText("Confirm Password");
		
		Label errorLabel = new Label();
		errorLabel.getStyleClass().add("error-label");
		errorLabel.setVisible(false);
		errorLabel.setWrapText(true); //Prevents horizontal overflow when error messages are long
		
		
		// ---------------------- Buttons ------------------------
		Button createBtn = new Button("Create Account");
		createBtn.getStyleClass().add("btn-primary");
		createBtn.setMaxWidth(Double.MAX_VALUE);
		createBtn.setPrefHeight(44);
		
		Button backBtn = new Button("<- Back to Login");
		backBtn.getStyleClass().add("btn-outline");
		backBtn.setMaxWidth(Double.MAX_VALUE);
		backBtn.setPrefHeight(40);
		
		// ---------------------- Card -----------------------------
		VBox form = new VBox(8, 
				lbl("Full Name"),					fullNameField,
				lbl("Username"),					usernameField,
				lbl("Email"),  						emailField,
				lbl("Phone (optional)"), 	phoneField,
				lbl("Password"), 					passField,
				lbl("Confirm Password"), 	confirmField,
				errorLabel,
				createBtn,
				new Separator(),
				backBtn
				);
		form.setPadding(new Insets(28));
		form.setMaxWidth(440);
		form.getStyleClass().add("card");
		
		VBox root = new VBox(header, form);
		root.setAlignment(Pos.CENTER);
		root.getStyleClass().add("page-bg");
		root.setPadding(new Insets(40, 20, 40, 20));
		
		//------------- Create logic ------------------------
		createBtn.setOnAction(e -> {
			errorLabel.setVisible(false);
			
			/**
			 * Read fields
			 * Note: .trim() on everything EXCEPT passwords
			 */
			String fullName = fullNameField.getText().trim();
			String username = usernameField.getText().trim();
			String email = emailField.getText().trim();
			String phone = normalisedPhone(phoneField.getText().trim());
			String pass = passField.getText();			    // No .trim()
			String confirm = confirmField.getText();		// No .trim()
			
			
			//-------------- Validation --------------------------
			
			
			// Required fields
			if(fullName.isEmpty() || username.isEmpty() || email.isEmpty() || pass.isEmpty())
			{
				LoginController.showError(errorLabel, "Please fill in all required fields");
				return;
			}
			
			// Username (no spaces allowed)
			if(username.contains(" "))
			{
				LoginController.showError(errorLabel, "Username cannot contain spaces.");
				return;
			}
			
			// Email format
			if (!email.contains("@") || !email.contains("."))
			{
				LoginController.showError(errorLabel, "Please enter a valid email address.");
				return;
			}
			
			// Phone (if provided, must be at least 10 digits)
			if(!phone.isEmpty() && phone.length() < 10)
			{
				LoginController.showError(errorLabel, "Please enter a valid phone number (at least 10 digits)");
				return;
			}
			
			// Password length
			if(pass.length() < 6)
			{
				LoginController.showError(errorLabel, "Password must be at least 6 characters");
				return;
			}
			// Password match
			if(!pass.equals(confirm))
			{
				LoginController.showError(errorLabel, "Passwords do not match");
				return;
			}
			// Duplicate username
			if(UserDAO.usernameExists(username))
			{
				LoginController.showError(errorLabel, "Username ' " + username + " ' is already taken");
				return;
			}
			// Duplicate email
			if(UserDAO.emailExists(email))
			{
				LoginController.showError(errorLabel, "An account with this email already exists");
				return;
			}
			
			// ---------------------- Create records ---------------------------------------
			
			// Create user account (stores SHA-256 hash of password)
			int userId = UserDAO.createUser(username, pass, email);
			if(userId < 0)
			{
				LoginController.showError(errorLabel, "Account creation failed. Please try again.");
				return;
			}
			
			// Create passenger profile linked to the user
			// Phone stored as normalised digits (e.g. "4373667899")
			int passengerId = PassengerDAO.createPassenger(
					userId, 
					fullName, 
					phone.isEmpty() ? null : phone, 
					email
			);
			if(passengerId < 0)
			{
				LoginController.showError(errorLabel, "Profile creation failed. Please try again.");
				return;
			}
			
			// ------- Success ----------------------------
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Account Created");
			alert.setHeaderText("Welcome " + fullName + "!");
			alert.setContentText("Account created successfully.\n"
					+ "Username: " + username + "\n\n"
					+ "You can now log in");								
			alert.showAndWait();
			
			// Return to login, context still has no user set yet
			new LoginController(context).show();
			
		});
		
		backBtn.setOnAction(e -> new LoginController(context).show());
		
		// ---------------- Scene -------------------------
		Scene scene = new Scene(root, 900, 700);
		LoginController.applyStylesheet(scene);
		stage.setScene(scene);
		stage.show();
		
	}

	//-------------Helpers ----------------------
	
	/**
	 * This method will: create a styled field  label
	 * @param string
	 * @return
	 */
	private Label lbl(String text)
	{
		Label lbl = new Label(text);
		lbl.getStyleClass().add("field-label");
		return lbl;
	}

	/**
	 * This method will: help to create styled text fields with a prompt
	 * @param String prompt placeholder text shown when field is empty
	 * @return configured TextField
	 */
	private TextField field(String prompt)
	{
		TextField tf = new TextField();
		tf.setPromptText(prompt);
		return tf;
	}
	
	/**
	 * 
	 * This method will: Strips all non-digit characters from a phone number.
	 * "(416) 555-0123" → "4165550123"
	 * "416-555-0123"   → "4165550123"
	 * "416.555.0123"   → "4165550123"
	 * @param phone (raw phone input from the text field)
	 * @return digits only, or empty string if input was blank
	 */

	private String normalisedPhone(String phone)
	{
		return phone.replaceAll("[^0-9]", "");
	}
	/**
	 * 
	 * This method will: Format a 10-digit phone string for display.
	 * "4373667899" -> "437-366-7899"
	 * Return the original string unchanged if it is not exactly 10 digits.
	 * 
	 * @param digits normalized phone digits
	 * @return formatted phone string
	 */
	public static String formatPhone(String digits)
	{
		if (digits == null || digits.length() != 10)
			return digits;
		
		return digits.substring(0, 3) + "-"
				+ digits.substring(3, 6) + "-"
				+ digits.substring(6);
	}
	
	
}
