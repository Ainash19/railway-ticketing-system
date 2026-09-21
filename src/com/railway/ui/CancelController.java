/**
 * Program Name: CancelController.java
 * Purpose: Lists a passenger's confirmed bookings and lets them cancel
 *          one, with a confirmation dialog before the cancel is applied.
 *          After a successful cancellation, runs the fraud detection
 *          engine's excessive-cancellations check (R03) and flags the
 *          passenger's account for admin review if the threshold is
 *          exceeded.
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
import com.railway.fraud.FraudDetectionEngine;
import com.railway.fraud.FraudDetectionEngine.FraudCheckResult;
import com.railway.models.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;

/**
 * Lists confirmed bookings and allows cancellation.
 * Runs R03 (Excessive Cancellations) after each cancel.
 * Reads Passenger from AppContext.
 */
public class CancelController {

    private final AppContext context;

    public CancelController(AppContext context) {
        this.context = context;
    }

    public void show() {
        Stage     stage     = context.getStage();
        Passenger passenger = context.getCurrentPassenger();

        HBox navbar = PassengerDashboard.buildNavbar(context, "Cancel a Ticket");

        Label info = new Label(
            "Select a confirmed booking and click Cancel to cancel your ticket.");
        info.getStyleClass().add("hint-text");
        info.setPadding(new Insets(12, 18, 4, 18));

        Label warningBar = new Label(
            "⚠   More than 2 cancellations in 24 hours will flag your account for admin review.");
        warningBar.getStyleClass().add("warning-bar");
        warningBar.setWrapText(true);

        // ── Table ─────────────────────────────────────────────
        TableView<CancelRow> table = new TableView<>();
        table.setPlaceholder(new Label("No confirmed bookings found."));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<CancelRow, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(100);
        actionCol.setCellFactory(tc -> new TableCell<>() {
            private final Button btn = new Button("Cancel");
            {
                btn.getStyleClass().addAll("btn-danger", "btn-sm");
                btn.setOnAction(e -> {
                    CancelRow row = getTableView().getItems().get(getIndex());
                    doCancel(row.booking(), table);
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btn);
            }
        });

        table.getColumns().addAll(
            col("Booking #",  90,  r -> "#" + r.booking().bookingId()),
            col("Train",      200, r -> r.trainName()),
            col("Departure",  170, r -> r.departure()),
            col("Seat",        70, r -> r.booking().seatNumber()),
            col("Price",       80, r -> r.booking().getFormattedPrice()),
            col("Booked on",  160, r -> r.booking().bookingTime() != null
                                        ? r.booking().bookingTime().replace("T", " ") : ""),
            actionCol
        );

        loadTable(table);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().addAll("btn-outline", "btn-sm");
        refreshBtn.setOnAction(e -> loadTable(table));

        HBox toolbar = new HBox(refreshBtn);
        toolbar.setPadding(new Insets(8, 18, 8, 18));

        // ── Root ──────────────────────────────────────────────
        VBox content = new VBox(info, warningBar, toolbar, table);
        content.setPadding(new Insets(0, 14, 14, 14));
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox root = new VBox(navbar, content);
        VBox.setVgrow(content, Priority.ALWAYS);

        Scene scene = new Scene(root, 1000, 680);
        LoginController.applyStylesheet(scene);
        stage.setScene(scene);
    }

    // ── Cancel logic ──────────────────────────────────────────

    private void doCancel(Booking booking, TableView<CancelRow> table) {
        Passenger passenger = context.getCurrentPassenger();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Cancellation");
        confirm.setHeaderText("Cancel Booking #" + booking.bookingId() + "?");
        confirm.setContentText("Seat: " + booking.seatNumber()
            + "\nPrice: " + booking.getFormattedPrice()
            + "\n\nThis action cannot be undone.");
        Optional<ButtonType> btn = confirm.showAndWait();
        if (btn.isEmpty() || btn.get() != ButtonType.OK) return;

        boolean cancelled = BookingDAO.cancelBooking(booking.bookingId());
        if (!cancelled) {
            new Alert(Alert.AlertType.ERROR, "Cancellation failed. Please try again.").showAndWait();
            return;
        }

        ScheduleDAO.incrementAvailableSeats(booking.scheduleId());

        FraudCheckResult result =
            FraudDetectionEngine.runPostCancellationChecks(passenger.passengerId());

        String msg = "Booking #" + booking.bookingId() + " cancelled successfully.";
        if (result.flagged()) {
            msg += "\n\n⚠   " + result.reason();
            new Alert(Alert.AlertType.WARNING, msg).showAndWait();
        } else {
            new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
        }

        loadTable(table);
    }

    // ── Data loader ───────────────────────────────────────────

    private void loadTable(TableView<CancelRow> table) {
        Passenger passenger = context.getCurrentPassenger();
        ArrayList<Booking> bookings =
            BookingDAO.getConfirmedBookingsByPassenger(passenger.passengerId());
        if (bookings == null) bookings = new ArrayList<>();

        ArrayList<CancelRow> rows = new ArrayList<>();
        for (Booking b : bookings) {
            Schedule sched = ScheduleDAO.getScheduleById(b.scheduleId());
            String trainName = "", departure = "";
            if (sched != null) {
                Train t = TrainDAO.getTrainById(sched.trainId());
                if (t != null) trainName = t.trainName() + " (" + t.trainNumber() + ")";
                departure = sched.departureDate() + "  " + sched.departureTime();
            }
            rows.add(new CancelRow(b, trainName, departure));
        }
        table.setItems(FXCollections.observableArrayList(rows));
    }

    // ── Helpers ───────────────────────────────────────────────

    private TableColumn<CancelRow, String> col(
            String header, int width, Function<CancelRow, String> getter) {
        TableColumn<CancelRow, String> col = new TableColumn<>(header);
        col.setCellValueFactory(cd ->
            new SimpleStringProperty(getter.apply(cd.getValue())));
        col.setPrefWidth(width);
        return col;
    }

    record CancelRow(Booking booking, String trainName, String departure) {}
}
