/**
 * Program Name: AdminDashboard.java
 * Purpose:  Admin dashboard — Dashboard and All Alerts screens.
 * 						Reads User from AppContext.
 * @author Ainash Zhumagulova 
 * Date Jul 1, 2026
 */
package com.railway.ui;

import java.util.ArrayList;
import java.util.function.Function;

import com.railway.dao.BookingDAO;
import com.railway.dao.FraudAlertDAO;
import com.railway.dao.PassengerDAO;
import com.railway.models.FraudAlert;
import com.railway.models.Passenger;
import com.railway.models.User;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * 
 */
public class AdminDashboard
{
	private final AppContext context;

	/**
	 * Constructor a new AdminDashboard object
	 * @param context2
	 */
	public AdminDashboard(AppContext context)
	{
		this.context = context;
	}

	/**
	 * This method will: TODO
	 */
	public void show()
	{
		showDashboard();
		
	}

	//─────────────────────────────────────────────────────────
  // DASHBOARD SCREEN
  // ─────────────────────────────────────────────────────────
	
	/**
	 * This method will: show Dashboard screen
	 */
	private void showDashboard()
	{
		Stage stage = context.getStage();
		
		VBox sidebar = buildSidebar("dashboard");
		
		HBox stats = new HBox(14, 
				statCard("Total Bookings",	String.valueOf(BookingDAO.getTotalCount()), 				"stat-blue"),
				statCard("Passengers",			String.valueOf(PassengerDAO.getTotalCount()),				"stat-purple"),
				statCard("Open Alerts",			String.valueOf(FraudAlertDAO.getUnreviewedCount()),	"stat-red"),
				statCard("Total Alerts",		String.valueOf(FraudAlertDAO.getTotalCount()),			"stat-amber")		
				);
		stats.setPadding(new Insets(16));
		
		Label tableTitle = new Label("Recent Unreviewed Alerts");
		tableTitle.getStyleClass().add("section-title");
		
		TableView<AlertRow> table = buildAlertTable();
		
		ArrayList<FraudAlert> unreviewed = FraudAlertDAO.getUnreviewedAlerts();
		if (unreviewed == null) unreviewed = new ArrayList<>();
		
		ArrayList<AlertRow> rows = new ArrayList<>();
		for (FraudAlert a : unreviewed) rows.add(new AlertRow(a));
		table.setItems(FXCollections.observableArrayList(rows));
		
		Button refreshBtn = new Button("Refresh");
		refreshBtn.getStyleClass().addAll("btn-outline", "btn-sm");
		refreshBtn.setOnAction(e -> showDashboard());
		
		Button viewAllBtn = new Button("View All Allerts ->");
		viewAllBtn.getStyleClass().addAll("btn-primary", "btn-sm");
		viewAllBtn.setOnAction(e -> showAlerts());
		
		Region sp = new Region();
		HBox.setHgrow(sp, Priority.ALWAYS);
		
		
		HBox toolbar = new HBox(10, tableTitle, sp, refreshBtn, viewAllBtn);
		toolbar.setAlignment(Pos.CENTER_LEFT);
		toolbar.setPadding(new Insets(0, 0, 10, 0));
		
		VBox tableArea = new VBox(toolbar, table);
		tableArea.setPadding(new Insets(0, 16, 16, 16));
		VBox.setVgrow(table, Priority.ALWAYS);
		
		VBox center = new VBox(stats, tableArea);
		VBox.setVgrow(tableArea, Priority.ALWAYS);
		
		BorderPane root = new BorderPane();
		root.setLeft(sidebar);
		root.setCenter(center);
		
		Scene scene = new Scene(root, 1100, 720);
		LoginController.applyStylesheet(scene);
		stage.setScene(scene);
				
	}
	
	// ---------------------------------------------------------------------
	//                      	ALL ALERT SCREENS
	// ---------------------------------------------------------------------
	/**
	 * This method will: Show all alerts screen
	 * @return 
	 */
	public void showAlerts()
	{
		Stage stage = context.getStage();
		
		VBox sidebar = buildSidebar("alerts");
		
		Label title = new Label("All Fraud Alerts");
		title.getStyleClass().add("section-title");
		
		ComboBox<String> sevFilter = new ComboBox();
		sevFilter.getItems().addAll("All", "high", "medium", "low");
		sevFilter.setValue("All");
		sevFilter.setPrefWidth(120);
		
		ComboBox<String> statusFilter = new ComboBox<>();
    statusFilter.getItems().addAll("All", "Unreviewed", "Reviewed");
    statusFilter.setValue("Unreviewed");
    statusFilter.setPrefWidth(120);
    
    Button applyBtn = new Button("Apply Filter");
    applyBtn.getStyleClass().addAll("btn-primary", "btn-sm");
    
    Button refreshBtn = new Button("Refresh");
    refreshBtn.getStyleClass().addAll("btn-outline", "btn-sm");
    
    Label sevLabel = new Label("Severity");
    sevLabel.getStyleClass().add("field-label");
    
    Label statusLabel = new Label("Status:");
    statusLabel.getStyleClass().add("field-label");
    
    HBox filterBar = new HBox(10, 
    		sevLabel, sevFilter, statusLabel, statusFilter, applyBtn, refreshBtn);
    filterBar.setAlignment(Pos.CENTER_LEFT);
    filterBar.setPadding(new Insets(10, 0, 10, 0));
    
    TableView<AlertRow> table = buildAlertTable();
    VBox.setVgrow(table, Priority.ALWAYS);
    
    Runnable loadData = () -> {
    	ArrayList<FraudAlert> all = FraudAlertDAO.getAllAlerts();
    	if (all == null) all = new ArrayList<>();
    	String sev = sevFilter.getValue();
    	String status = statusFilter.getValue();
    	
    	ArrayList<AlertRow> rows = new ArrayList<>();
    	for(FraudAlert a : all) {
    		boolean sevOk = sev.equals("All") || a.severity().equalsIgnoreCase(sev);
    		boolean statusOk = status.equals("All") 
    				|| (status.equals("Unreviewed") ? !a.isReviewed() : a.isReviewed());
    		if (sevOk && statusOk) rows.add(new AlertRow(a));
    	}
    	table.setItems(FXCollections.observableArrayList(rows));
    };
		
    loadData.run();
    applyBtn.setOnAction(e -> loadData.run());
    refreshBtn.setOnAction(e -> loadData.run());
    
    VBox center = new VBox(title, filterBar, table);
    center.setPadding(new Insets(16));
    VBox.setVgrow(table, Priority.ALWAYS);
    
    BorderPane root = new BorderPane();
    root.setLeft(sidebar);
    root.setCenter(center);
    
    Scene scene = new Scene(root, 1100, 720);
    LoginController.applyStylesheet(scene);
    stage.setScene(scene);
		
		
	}
	
	// -------------------------------------------------------------------
	//												SHARED ALERT TABLE
	// -------------------------------------------------------------------
	
	/**
	 * This method will: TODO
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private TableView<AlertRow> buildAlertTable()
	{
		TableView<AlertRow> table = new TableView<>();
		table.setPlaceholder(new Label("No alerts found."));
		
		TableColumn<AlertRow, String> sevCol =
        alertCol("Severity", 80, r -> r.alert().severity());
		sevCol.setCellFactory(tc -> new TableCell<>() 
				{
			@Override protected void updateItem (String item, boolean empty)
			{
				super.updateItem(item, empty);
				getStyleClass().removeAll("severity-high", "severity-medium", "severity-low");
				if(empty || item == null) 
				{
					setText(null);
					return;
				}
				setText(item.toUpperCase());
				getStyleClass().add("severity-" + item.toLowerCase());
			}
				});
		
		TableColumn<AlertRow, Void> actionCol = new TableColumn<>("Action");
		actionCol.setPrefWidth(135);
		actionCol.setCellFactory(tc -> new TableCell<>()
				{
			private final Button btn = new Button("Mark Reviewed");
			{
				btn.getStyleClass().addAll("btn-success", "btn-sm");
				btn.setOnAction(e -> {
					AlertRow row = getTableView().getItems().get(getIndex());
					if (!row.alert().isReviewed())
					{
						FraudAlertDAO.markAsReviewed(row.alert().alertId());
						getTableView().getItems().remove(getIndex());
					}
				});
			}
			 @Override protected void updateItem(Void v, boolean empty) 
			 {
         super.updateItem(v, empty);
         if (empty) 
         { 
        	 setGraphic(null); 
        	 return; 
         }
         btn.setDisable(getTableView().getItems().get(getIndex()).alert().isReviewed());
         setGraphic(btn);
			 }
			
				});
		
		
		
		 table.getColumns().addAll(
         alertCol("Alert #",      70,  r -> "#" + r.alert().alertId()),
         alertCol("Passenger",   150,  r -> r.passengerName()),
         alertCol("Rule",        190,  r -> r.alert().fraudType()),
         alertCol("Description", 280,  r -> {
             String d = r.alert().alertDescription();
             return (d != null && d.length() > 75) ? d.substring(0, 75) + "…" : (d != null ? d : "");
         }),
         sevCol,
         alertCol("Time",        160,  r -> r.alert().alertTime() != null
                                            ? r.alert().alertTime().replace("T", " ") : ""),
         alertCol("Status",      100,  r -> r.alert().isReviewed() ? "✓ Reviewed" : "Pending"),
         actionCol
     );
		
		return table;
		
	}

	
	// --------------------------------------------------------------------
	//													SIDEBAR 
	// --------------------------------------------------------------------
	
	

	/**
	 * This method will: build Sidebar 
	 * @param string
	 * @return Vbox sidebar
	 */
	private VBox buildSidebar(String active)
	{
		User user = context.getUser();
		
		Label logo = new Label("");					// TODO: Image
		logo.getStyleClass().add("login-logo");
		
		Label appName = new Label("Admin panel");
		appName.getStyleClass().add("sidebar-app-name");
		
		Label uname = new Label("" + user.username());
		uname.getStyleClass().add("sidebar-username");
		
		VBox header = new VBox(4, logo, appName, uname);
		header.setPadding(new Insets(16, 14, 14, 14));
		
		Button dashBtn 		= sidebarBtn("Dashboard", active.equals("dashboard"));
		Button alertsBtn 	= sidebarBtn("Fraud Alerts", active.equals("alerts"));
		Button logoutBtn	= sidebarBtn("Logout", false);
		
		dashBtn.setOnAction(e -> showDashboard());
		alertsBtn.setOnAction(e -> showAlerts());
		logoutBtn.setOnAction(e -> context.logout());
		
		Region spacer = new Region();
		VBox.setVgrow(spacer, Priority.ALWAYS);
		
		VBox navItems = new VBox(4, dashBtn, alertsBtn);
		navItems.setPadding(new Insets(10, 8, 8, 8));
		
		VBox logoutArea = new VBox(logoutBtn);
		logoutArea.setPadding(new Insets(8, 8, 14, 8));
		
		VBox sidebar = new VBox(header, new Separator(), navItems, spacer, logoutArea);
		sidebar.getStyleClass().add("sidebar");
		return sidebar;
		
	}
	/**
		 * This method will: TODO
		 * @param string
		 * @param equals
		 * @return Button btn
		 */
		private Button sidebarBtn(String text, boolean active)
		{
			Button btn = new Button(text);
			btn.getStyleClass().add(active ? "sidebar-btn-active" : "sidebar-btn");
			btn.setMaxWidth(Double.MAX_VALUE);
			return btn;
		}
		

	
	
	
	
	// ------------------------------------------------------------------------
	// 																HELPERS
	//-------------------------------------------------------------------------
	/**
	 * This method will: TODO
	 * @param string
	 * @param valueOf
	 * @param string2
	 * @return VBox card
	 */
	private VBox statCard(String label, String value, String colorClass)
	{
		Label val = new Label(value);
		val.getStyleClass().addAll("stat-value", colorClass);
		
		Label lbl = new Label(label);
		lbl.getStyleClass().add("stat-label");
		
		VBox card = new VBox(4, val, lbl);
		card.setAlignment(Pos.CENTER_LEFT);
		card.getStyleClass().add("stat-card");
		HBox.setHgrow(card, Priority.ALWAYS);
		return card;

	}
	
	/**
	 *  Create a record AlertRow that creates a wrapper around a FraudAlert object.
	 *  Takes one component: a FraudAlert object named alert
	 *  Automatically generates:
						Constructor: new AlertRow(alert)
						Getter: alert() (returns the FraudAlert)
						equals(), hashCode(), toString()
	 */
	record AlertRow(FraudAlert alert)
	{
		/**
		 * 
		 * This method will: - gets the passenger ID from the FraudAlert object
		 * 									- use PassengerDAO to fetch the full Passenger object from the database
		 * @return the passenger's name associated with the alert
		 */
		String passengerName() 
		{
	    try 
	    {
	        Passenger p = PassengerDAO.getPassengerById(alert.passengerId());
	        return p != null ? p.fullName() : "Passenger #" + alert.passengerId();
	    } catch (Exception e) 
	    {
	        return "Unknown Passenger";
	    }
	}
	}
	
	/**
	 * This method will: create and configures a TableColumn for a JavaFX TableView. 
	 * 										It's using functional programming to make column creation reusable and clean.
	 * @param String header - The column header text (e.g., "Passenger", "Alert Type", "Date")
	 * @param int width - The preferred width of the column in pixels
	 * @param Function<AlertRow, String> getter - A function that extracts a String from an AlertRow object
	 * @return TableColumn<AlertRow, String> - A configured TableColumn for displaying AlertRow data
	 */
	private TableColumn<AlertRow, String> alertCol(String header, int width, Function<AlertRow, String> getter) 
	{
		// Create a new TableColumn with the specified header text.
		TableColumn<AlertRow, String> col = new TableColumn<>(header);
		
		// Set the Cell Value Factory 
		col.setCellValueFactory(celldata -> new SimpleStringProperty(getter.apply(celldata.getValue())));
		col.setPrefWidth(width);
		return col;
}
	
}
