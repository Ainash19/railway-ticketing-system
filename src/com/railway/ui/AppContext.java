/**
 * Program Name: AppContext.java
 * Purpose: This class shares application context passed between every controller.
 * Holds:
 *   - the primary Stage  (so every controller can swap scenes)
 *   - the logged-in User (role, username, id)
 *   - the logged-in Passenger profile (name, email — null for admins)
 *
 * Usage:
 *   AppContext ctx = new AppContext(stage);          // in Main
 *   ctx.setUser(user);                               // after login
 *   ctx.setPassenger(passenger);                     // after login (passenger role)
 *   new BookingController(ctx).show();               // navigate
 * 
 * @author Ainash Zhumagulova 
 * Date Jul 1, 2026
 */
package com.railway.ui;

import com.railway.models.Passenger;
import com.railway.models.User;

import javafx.stage.Stage;

/**
 * 
 */
public class AppContext
{
	private final Stage stage;        	// the window
	private User currentUser;    				// set after login
	private Passenger currentPassenger; // set after login (null for admins)
	
	public AppContext(Stage stage) 
	{
		this.stage = stage;
	}
	
	//-----------Stage --------------
	public Stage getStage() 	
	{ 		
		return stage; 	
	}	
	
	// ----------- Current user ------------
	public User getUser()
	{
		return currentUser;
	}

	public void setUser(User user)
	{
		this.currentUser = user;
	}
	
	public boolean isLoggedIn()
	{
		return currentUser != null;
	}
	public boolean isAdmin()
	{
		return currentUser != null && currentUser.isAdmin();
	}
	public boolean isPassenger()
	{
		return currentUser !=null && currentUser.isPassenger();
	}


	// -----------Current Passenger --------------
	
		public Passenger getCurrentPassenger()
	{
		return currentPassenger;
	}

	public void setCurrentPassenger(Passenger currentPassenger)
	{
		this.currentPassenger = currentPassenger;
	}
	
	// --------------- Login Helper ----------------
	/**
	 * Clears all session data and navigates back to the Login screen
	 */
	
	public void logout() 
	{
		this.currentUser					= null;
		this.currentPassenger			= null;
		new LoginController(this).show();
	}
	
}
