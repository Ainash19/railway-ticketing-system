/**
 * Program Name: BookingController.java
 * Purpose: Passenger-facing booking flow: search trains by route and date,
 *          pick an available seat from a live seat map, and confirm a
 *          booking. Every booking attempt is checked by the fraud
 *          detection engine (duplicate-booking and rapid-booking rules)
 *          before it is created.
 *
 * Reads the logged-in User and Passenger from AppContext.
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
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.function.Function;

/**
 * Train search → seat selection → booking with fraud detection.
 * Reads User and Passenger from AppContext.
 */
public class BookingController {

    private final AppContext context;

    // Current selection state — instance fields instead of statics
    private Train    selectedTrain;
    private Schedule selectedSchedule;
    private String   selectedSeat;

    public BookingController(AppContext context) {
        this.context = context;
    }

    public void show() {
        Stage     stage     = context.getStage();
        Passenger passenger = context.getCurrentPassenger();

        HBox navbar = PassengerDashboard.buildNavbar(context, "Book a Ticket");

        // ── Search form ───────────────────────────────────────
        ComboBox<String> fromBox = new ComboBox<>();
        fromBox.setPromptText("From");
        fromBox.setEditable(true);
        fromBox.setPrefWidth(180);
        ArrayList<String> sources = TrainDAO.getAllSourceStations();
        if (sources != null) fromBox.getItems().addAll(sources);

        ComboBox<String> toBox = new ComboBox<>();
        toBox.setPromptText("To");
        toBox.setEditable(true);
        toBox.setPrefWidth(180);
        ArrayList<String> dests = TrainDAO.getAllDestinationStations();
        if (dests != null) toBox.getItems().addAll(dests);

        DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(1));
        datePicker.setPrefWidth(155);

        Button searchBtn = new Button("Search Trains");
        searchBtn.getStyleClass().addAll("btn-primary", "btn-sm");

        Button clearBtn = new Button("Clear");
        clearBtn.getStyleClass().addAll("btn-outline", "btn-sm");

        HBox searchForm = new HBox(12,
            labeledBox("From", fromBox),
            labeledBox("To",   toBox),
            labeledBox("Date", datePicker),
            searchBtn, clearBtn
        );
        searchForm.setAlignment(Pos.BOTTOM_LEFT);
        searchForm.setPadding(new Insets(14, 14, 10, 14));
        searchForm.getStyleClass().add("card-sm");

        // ── Results table ─────────────────────────────────────
        TableView<TrainRow> table = new TableView<>();
        table.setPlaceholder(new Label("Search above to find trains."));
        table.setPrefHeight(220);
        VBox.setVgrow(table, Priority.ALWAYS);

        table.getColumns().addAll(
            col("Train #",   100, r -> r.train().trainNumber()),
            col("Name",      180, r -> r.train().trainName()),
            col("From",      110, r -> r.train().sourceStation()),
            col("To",        110, r -> r.train().destinationStation()),
            col("Departs",   165, r -> r.schedule().departureDate()
                                        + "  " + r.schedule().departureTime()),
            col("Arrives",   165, r -> r.schedule().arrivalDate()
                                        + "  " + r.schedule().arrivalTime()),
            col("Seats",      65, r -> String.valueOf(r.schedule().availableSeats())),
            col("Fare",       85, r -> r.train().getFormattedFare())
        );

        // ── Seat panel ────────────────────────────────────────
        Label seatTitle = new Label("Select a Seat");
        seatTitle.getStyleClass().add("section-title");

        HBox legend = new HBox(12,
            legendItem("seat-available", "Available"),
            legendItem("seat-booked",    "Booked"),
            legendItem("seat-selected",  "Selected")
        );

        GridPane seatGrid = new GridPane();
        seatGrid.setHgap(6);
        seatGrid.setVgap(6);

        Label seatStatus = new Label("Select a train first");
        seatStatus.getStyleClass().add("hint-text");

        Button bookBtn = new Button("Confirm Booking");
        bookBtn.getStyleClass().add("btn-success");
        bookBtn.setMaxWidth(Double.MAX_VALUE);
        bookBtn.setPrefHeight(44);
        bookBtn.setDisable(true);

        VBox seatPanel = new VBox(12, seatTitle, legend, seatGrid, seatStatus, bookBtn);
        seatPanel.setPadding(new Insets(16));
        seatPanel.setPrefWidth(315);
        seatPanel.getStyleClass().add("card-sm");

        // ── Search action ─────────────────────────────────────
        Runnable doSearch = () -> {
            String from = fromBox.getValue() == null ? "" : fromBox.getValue().trim();
            String to   = toBox.getValue()   == null ? "" : toBox.getValue().trim();
            String date = datePicker.getValue() == null ? "" : datePicker.getValue().toString();

            ArrayList<Train> trains = TrainDAO.searchTrains(from, to);
            ArrayList<TrainRow> rows = new ArrayList<>();

            if (trains != null) {
                for (Train t : trains) {
                    ArrayList<Schedule> schedules = date.isEmpty()
                        ? ScheduleDAO.getSchedulesByTrain(t.trainId())
                        : ScheduleDAO.getSchedulesByTrainAndDate(t.trainId(), date);
                    if (schedules != null)
                        for (Schedule s : schedules) rows.add(new TrainRow(t, s));
                }
            }

            table.setItems(FXCollections.observableArrayList(rows));
            if (rows.isEmpty())
                table.setPlaceholder(new Label("No trains found for this route / date."));

            seatGrid.getChildren().clear();
            selectedSeat = null;
            seatStatus.setText("Select a train from the results");
            seatStatus.getStyleClass().setAll("hint-text");
            bookBtn.setDisable(true);
        };

        searchBtn.setOnAction(e -> doSearch.run());
        clearBtn.setOnAction(e -> {
            fromBox.setValue(null);
            toBox.setValue(null);
            datePicker.setValue(LocalDate.now().plusDays(1));
            table.setItems(FXCollections.emptyObservableList());
            seatGrid.getChildren().clear();
        });

        // ── Row selection → build seat map ────────────────────
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, row) -> {
            if (row == null) return;
            selectedTrain    = row.train();
            selectedSchedule = row.schedule();
            selectedSeat     = null;
            seatStatus.setText("No seat selected");
            seatStatus.getStyleClass().setAll("hint-text");
            bookBtn.setDisable(true);
            buildSeatMap(seatGrid, seatStatus, bookBtn);
        });

        // ── Book button ───────────────────────────────────────
        bookBtn.setOnAction(e -> {
            if (selectedSeat == null || selectedSchedule == null || selectedTrain == null) return;

            FraudCheckResult result = FraudDetectionEngine.runPreBookingChecks(
                passenger.passengerId(), selectedSchedule.scheduleId());

            if (!result.allowed()) {
                showAlert(Alert.AlertType.ERROR, result.getTitle(), result.reason());
                return;
            }

            int bookingId = BookingDAO.createBooking(
                passenger.passengerId(), selectedSchedule.scheduleId(),
                selectedSeat, selectedTrain.baseFare());

            if (bookingId < 0) {
                showAlert(Alert.AlertType.ERROR, "Booking Failed",
                    "Could not complete booking. The seat may have just been taken.");
                return;
            }

            ScheduleDAO.decrementAvailableSeats(selectedSchedule.scheduleId());

            String msg = "Booking confirmed!\n\n"
                + "Booking ID : #" + bookingId + "\n"
                + "Train      : " + selectedTrain.trainName() + "\n"
                + "Seat       : " + selectedSeat + "\n"
                + "Price      : " + selectedTrain.getFormattedFare();
            if (result.flagged()) msg += "\n\n⚠  " + result.reason();

            showAlert(Alert.AlertType.INFORMATION, "Booking Confirmed!", msg);

            selectedSeat = null;
            seatStatus.setText("No seat selected");
            seatStatus.getStyleClass().setAll("hint-text");
            bookBtn.setDisable(true);
            buildSeatMap(seatGrid, seatStatus, bookBtn);
            doSearch.run();
        });

        // ── Layout ────────────────────────────────────────────
        SplitPane split = new SplitPane(new VBox(searchForm, table), seatPanel);
        split.setDividerPositions(0.65);
        VBox.setVgrow(split, Priority.ALWAYS);

        VBox root = new VBox(navbar, split);
        VBox.setVgrow(split, Priority.ALWAYS);

        Scene scene = new Scene(root, 1100, 720);
        LoginController.applyStylesheet(scene);
        stage.setScene(scene);
    }

    // ── Seat map ──────────────────────────────────────────────

    private void buildSeatMap(GridPane grid, Label statusLabel, Button bookBtn) {
        grid.getChildren().clear();
        ArrayList<String> booked = ScheduleDAO.getBookedSeatNumbers(selectedSchedule.scheduleId());
        if (booked == null) booked = new ArrayList<>();

        char[] cols = {'A','B','C','D','E','F'};
        for (int row = 1; row <= 10; row++) {
            for (int c = 0; c < 6; c++) {
                String seatNum = row + String.valueOf(cols[c]);
                boolean isBooked = booked.contains(seatNum);

                Button btn = new Button(seatNum);
                btn.getStyleClass().add(isBooked ? "seat-booked" : "seat-available");
                btn.setDisable(isBooked);

                if (!isBooked) {
                    btn.setOnAction(ev -> {
                        grid.getChildren().forEach(child -> {
                            if (child instanceof Button b && !b.isDisabled())
                                b.getStyleClass().setAll("seat-available");
                        });
                        btn.getStyleClass().setAll("seat-selected");
                        selectedSeat = seatNum;
                        statusLabel.setText("Seat " + seatNum + " selected  —  "
                            + selectedTrain.getFormattedFare());
                        statusLabel.getStyleClass().setAll("status-confirmed");
                        bookBtn.setDisable(false);
                    });
                }

                int colIndex = c >= 3 ? c + 1 : c;
                grid.add(btn, colIndex, row - 1);
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    private TableColumn<TrainRow, String> col(
            String header, int width, Function<TrainRow, String> getter) {
        TableColumn<TrainRow, String> col = new TableColumn<>(header);
        col.setCellValueFactory(cd ->
            new SimpleStringProperty(getter.apply(cd.getValue())));
        col.setPrefWidth(width);
        return col;
    }

    private VBox labeledBox(String labelText, javafx.scene.Node field) {
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("field-label");
        return new VBox(3, lbl, field);
    }

    private HBox legendItem(String styleClass, String text) {
        Label swatch = new Label("  ");
        swatch.getStyleClass().add(styleClass);
        swatch.setPrefSize(16, 14);
        Label lbl = new Label(text);
        lbl.getStyleClass().add("hint-text");
        HBox box = new HBox(5, swatch, lbl);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }

    record TrainRow(Train train, Schedule schedule) {}
}
