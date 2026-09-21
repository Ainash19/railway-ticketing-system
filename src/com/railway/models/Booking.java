/**
 * Program Name: Booking.java
 * Purpose: This class represents a ticket booking made by a passenger.
 * @author Ainash Zhumagulova 
 * Date Jun 26, 2026
 */
package com.railway.models;

/**
 * 
 */
public record Booking(
		int bookingId,
    int passengerId,
    int scheduleId,
    String bookingTime,
    String cancellationTime,
    String seatNumber,
    double ticketPrice,
    String status,
    boolean isFraudFlagged
		)
{
	 // Returns true if this booking is currently confirmed (not cancelled). 
  public boolean isConfirmed() {
      return "confirmed".equalsIgnoreCase(status);
  }

  // Returns true if this booking has been cancelled. 
  public boolean isCancelled() {
      return "cancelled".equalsIgnoreCase(status);
  }

  // Returns a formatted price string (e.g., "$142.00"). 
  public String getFormattedPrice() {
      return "$%.2f".formatted(ticketPrice);
  }

  // Returns the status with a fraud flag indicator if applicable. 
  public String getStatusDisplay() {
      return status + (isFraudFlagged ? "Attention " : " ");
  }

  @Override
  public String toString() {
      return "Booking[id=%d, passenger=%d, seat=%s, status=%s, price=$%.2f]"
          .formatted(bookingId, passengerId, seatNumber, status, ticketPrice);
  }
}
