/**
 * Program Name: BookingsController.java
 * Purpose: Displays a passenger's full booking history:  summary stat cards (total / confirmed / cancelled / flagged), 
 * 					a sortable  table of past and current bookings with train and departure details, and navigation to start a new booking.
 *
 * Reads the logged-in Passenger from AppContext.
 *
 * @author Ainash Zhumagulova
 * Date Jul 1, 2026
 */

package com.railway.ui;

import com.railway.dao.BookingDAO;
import com.railway.dao.ScheduleDAO;
import com.railway.dao.TrainDAO;
import com.railway.models.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.function.Function;

/**
 * Displays the passenger's full booking history.
 * Reads Passenger from AppContext.
 */
public class BookingsController {

    private final AppContext context;

    public BookingsController(AppContext context) {
        this.context = context;
    }

    public void show() {
        Stage     stage     = context.getStage();
        Passenger passenger = context.getCurrentPassenger();

        HBox navbar = PassengerDashboard.buildNavbar(context, "My Bookings");

        // ── Load data ─────────────────────────────────────────
        ArrayList<Booking> bookings = BookingDAO.getBookingsByPassenger(passenger.passengerId());
        if (bookings == null) bookings = new ArrayList<>();

        long confirmed = bookings.stream().filter(Booking::isConfirmed).count();
        long cancelled = bookings.stream().filter(Booking::isCancelled).count();
        long flagged   = bookings.stream().filter(Booking::isFraudFlagged).count();

        // ── Stat cards ────────────────────────────────────────
        HBox stats = new HBox(14,
            statCard("Total",     String.valueOf(bookings.size()), "stat-blue"),
            statCard("Confirmed", String.valueOf(confirmed),       "stat-green"),
            statCard("Cancelled", String.valueOf(cancelled),       "stat-red"),
            statCard("Flagged",   String.valueOf(flagged),         "stat-amber")
        );
        stats.setPadding(new Insets(14, 18, 10, 18));

        // ── Table ─────────────────────────────────────────────
        TableView<BookingRow> table = new TableView<>();
        table.setPlaceholder(new Label("No bookings found."));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<BookingRow, String> statusCol =
            col("Status", 110, r -> r.booking().getStatusDisplay());

        statusCol.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("status-confirmed", "status-cancelled", "status-flagged");
                if (empty || item == null) { setText(null); return; }
                setText(item);
                if      (item.contains("confirmed")) getStyleClass().add("status-confirmed");
                else if (item.contains("cancelled")) getStyleClass().add("status-cancelled");
                else                                 getStyleClass().add("status-flagged");
            }
        });

        table.getColumns().addAll(
            col("Booking #",  90,  r -> "#" + r.booking().bookingId()),
            col("Train",      200, r -> r.trainName()),
            col("Departure",  170, r -> r.departure()),
            col("Seat",        70, r -> r.booking().seatNumber()),
            col("Price",       80, r -> r.booking().getFormattedPrice()),
            statusCol,
            col("Booked on",  160, r -> r.booking().bookingTime() != null
                                        ? r.booking().bookingTime().replace("T", " ") : "")
        );

        ArrayList<BookingRow> rows = new ArrayList<>();
        for (Booking b : bookings) { 
            Schedule sched = ScheduleDAO.getScheduleById(b.scheduleId());
            String trainName = "", departure = "";
            if (sched != null) {
                Train t = TrainDAO.getTrainById(sched.trainId());
                if (t != null) trainName = t.trainName() + " (" + t.trainNumber() + ")";
                departure = sched.departureDate() + "  " + sched.departureTime();
            }
            rows.add(new BookingRow(b, trainName, departure));
        }
        table.setItems(FXCollections.observableArrayList(rows));

        // ── Toolbar ───────────────────────────────────────────
        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().addAll("btn-outline", "btn-sm");
        refreshBtn.setOnAction(e -> new BookingsController(context).show());

        Button newBtn = new Button("+ Book New Ticket");
        newBtn.getStyleClass().addAll("btn-primary", "btn-sm");
        newBtn.setOnAction(e -> new BookingController(context).show());

        HBox toolbar = new HBox(10, refreshBtn, newBtn);
        toolbar.setPadding(new Insets(0, 18, 10, 18));

        // ── Root ──────────────────────────────────────────────
        VBox content = new VBox(stats, toolbar, table);
        content.setPadding(new Insets(0, 14, 14, 14));
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox root = new VBox(navbar, content);
        VBox.setVgrow(content, Priority.ALWAYS);

        Scene scene = new Scene(root, 1000, 680);
        LoginController.applyStylesheet(scene);
        stage.setScene(scene);
    }

    // ── Helpers ───────────────────────────────────────────────

    private TableColumn<BookingRow, String> col(
            String header, int width, Function<BookingRow, String> getter) {
        TableColumn<BookingRow, String> col = new TableColumn<>(header);
        col.setCellValueFactory(cd ->
            new SimpleStringProperty(getter.apply(cd.getValue())));
        col.setPrefWidth(width);
        return col;
    }

    private VBox statCard(String label, String value, String colorClass) {
        Label val = new Label(value);
        val.getStyleClass().addAll("stat-value", colorClass);
        Label lbl = new Label(label);
        lbl.getStyleClass().add("stat-label");
        VBox card = new VBox(2, val, lbl);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("stat-card");
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    record BookingRow(Booking booking, String trainName, String departure) {}
}
