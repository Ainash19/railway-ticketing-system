/**
 * Program Name: Passengerdashboard.java
 * Purpose:  	The passenger's home screen after login: a welcome banner and a grid of action cards (Book Ticket, My Bookings, Cancel Ticket, Logout) 
 * 						that route to the corresponding screen.
 *	          Also provides the shared back-navigation navbar reused by BookingController, BookingsController, and CancelController.
 * @author Ainash Zhumagulova 
 * Date Jun 29, 2026
 */
package com.railway.ui;

import com.railway.models.Passenger;
import com.railway.models.User;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/**
 * 
 */
public class PassengerDashboard
{
	private final  AppContext context;
	/**
	 * Constructor a new PassengerDashboard object
	 * @param context
	 */
	public PassengerDashboard(AppContext context)
	{
		this.context = context;
	}
	
	/**
	 * 
	 * This method will: Instance method
	 */
	public void show()
	{
		
		Stage     stage     = context.getStage();
    User      user      = context.getUser();
    Passenger passenger = context.getCurrentPassenger();
		
    
    // ------------------------------------------------------------
    //                        NAVBAR
    // ------------------------------------------------------------
    
		Label navTitle = new Label("Railway Ticketing System");
		navTitle.getStyleClass().add("navbar-title");
		
		Label navUser = new Label(""+ passenger.fullName()); //TODO
		navUser.getStyleClass().add("navbar-user");
		
		Button logoutBtn = new Button("Logout");
		logoutBtn.getStyleClass().add("btn-nav");
		logoutBtn.setOnAction(e -> context.logout());
		
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		HBox navbar = new HBox(12, navTitle, spacer, navUser, logoutBtn);
		navbar.setAlignment(Pos.CENTER_LEFT);
		navbar.setPadding(new Insets(14, 24, 14, 24));
		navbar.getStyleClass().add("navbar");
		
		// ----------------------------------------------------------------
		//               WELCOME
		// ----------------------------------------------------------------
		
		Label welcome = new Label("Welcome back, " + passenger.fullName() + "!");
		welcome.getStyleClass().add("welcome-heading");
		
		Label welcomeSub = new Label("What would you like to do today?");
		welcomeSub.getStyleClass().add("welcome-sub");
		
		VBox welcomeBox = new VBox(6, welcome,welcomeSub);
		welcomeBox.setPadding(new Insets(28, 0, 20, 0));
		
		//----------------------------------------------------------------
		//                	ACTION CARD
		// ---------------------------------------------------------------
		
		VBox bookCard = actionCard("", "Book Ticket", "Search trains and reserve a seat");			//TODO: icon image
		VBox historyCard = actionCard("", "My Bookings", "View all your trips");								// TODO:icon
		VBox cancelCard = actionCard("", "Cancel Ticket", "Cancel a confirmed booking");				// TODO: icon
		VBox logoutCard = actionCard("", "Logout", "Sign out safely"); 													// TODO: icon
		
		bookCard.setOnMouseClicked(e -> new BookingController(context).show());
		historyCard.setOnMouseClicked(e -> new BookingsController(context).show());
		cancelCard.setOnMouseClicked(e -> new CancelController(context).show());
		logoutCard.setOnMouseClicked(e -> context.logout());
		
		GridPane grid = new GridPane();
		grid.setHgap(20);
		grid.setVgap(20);
		grid.add(bookCard, 0, 0);
		grid.add(historyCard, 1, 0);
		grid.add(cancelCard, 0, 1);
		grid.add(logoutCard, 1, 1);
		
		ColumnConstraints col = new ColumnConstraints();
		col.setPercentWidth(50);
		grid.getColumnConstraints().addAll(col, col);
		
		// -----------------------------------------------------------------------
		//                    ROOT
		// -----------------------------------------------------------------------
		VBox content = new VBox(welcomeBox, grid);
		content.setPadding(new Insets(0, 32, 32, 32));
		
		BorderPane root = new BorderPane();
		root.setTop(navbar);
		root.setCenter(content);
		
		
		Scene scene = new Scene(root, 900, 650);
		LoginController.applyStylesheet(scene);
		stage.setScene(scene);
	}
		
		// --------------------------------------------------------------------------
		//                    SHARED NAVBAR FOR CHILD SCREEN
		// --------------------------------------------------------------------------
		
		/**
		 * Builds the standard back-navigation navbar used by
     * BookingController, BookingsController, and CancelController.
		 */
		static HBox buildNavbar(AppContext context, String pageTitle)
		{
			 Button backBtn = new Button("← Dashboard");
       backBtn.getStyleClass().add("btn-nav");
       backBtn.setOnAction(e -> new PassengerDashboard(context).show());

       Label title = new Label("" + pageTitle);	//TODO: icon
       title.getStyleClass().add("navbar-title");

       Label name = new Label("" + context.getCurrentPassenger().fullName()); //TODO: icon
       name.getStyleClass().add("navbar-user");

       Region spacer = new Region();
       HBox.setHgrow(spacer, Priority.ALWAYS);

       HBox navbar = new HBox(14, backBtn, title, spacer, name);
       navbar.setAlignment(Pos.CENTER_LEFT);
       navbar.setPadding(new Insets(12, 20, 12, 20));
       navbar.getStyleClass().add("navbar");
       return navbar;
		}
	
		
		
		
	
	
	// --------------------------------------------------------------------
	//                         ACTION CARD BUILDER
	// --------------------------------------------------------------------

	/**
	 * This method will: build a card
	 * @param string icon
	 * @param string title
	 * @param string desc
	 * @return VBox card
	 */
	private static VBox actionCard(String icon, String title, String desc)
	{
		VBox card;
		
		Label ico = new Label(icon);
		ico.getStyleClass().add("card-icon");
		
		Label ttl = new Label(title);
		ttl.getStyleClass().add("card-title");
		
		Label dsc = new Label(desc);
		dsc.getStyleClass().add("card-desc");
		dsc.setWrapText(true);
		dsc.setTextAlignment(TextAlignment.CENTER);
		
		card = new VBox(10, ico, ttl, dsc);
		card.setAlignment(Pos.CENTER);
		card.setPrefHeight(170);
		card.getStyleClass().add("action-card");
		
		card.setOnMouseEntered(ev -> {
			ttl.getStyleClass().setAll("card-title-hover");
			dsc.getStyleClass().setAll("card-desc-hover");
		});
		
		card.setOnMouseExited(ev -> {
			ttl.getStyleClass().setAll("card-title");
			dsc.getStyleClass().setAll("card-desc");
		});
		
		return card;
	}
}
