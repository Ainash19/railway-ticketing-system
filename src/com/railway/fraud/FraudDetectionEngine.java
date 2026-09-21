/**
 * Program Name: FraudDetectionEngine.java
 * Purpose: This class will implementing the three fraud rules:
 *
 *   R01 -> Duplicate Booking:      	same passenger + schedule within 1 hour			// Prevents system abuse and holds seats unnecessarily
 *   R02 -> Rapid Booking:          	more than 3 bookings in 10 minutes					// Could be a bot or automated script trying to hoard tickets
 *   R03 -> Excessive Cancellations: 	more than 2 cancellations in 24 hours				// Frequent cancellations might indicate ticket manipulation
 *
 * Usage:
 *   FraudCheckResult result = engine.runPreBookingChecks(passengerId, scheduleId);
 *   if (!result.allowed()) { show result.reason(); }
 *   
 * @author Ainash Zhumagulova 
 * Date Jun 26, 2026
 */
package com.railway.fraud;


import com.railway.dao.BookingDAO;
import com.railway.dao.FraudAlertDAO;

/**
 * 
 */
/**
 * Fraud Detection Engine — implements three rules:
 *
 *   R01  Duplicate Booking        same passenger + schedule within 1 hour  → block
 *   R02  Rapid Booking            more than 3 bookings in 10 minutes        → block
 *   R03  Excessive Cancellations  more than 2 cancellations in 24 hours     → flag (allow but alert)
 *
 * All DAO calls use the static ProductDB-style methods; no instances needed.
 */
public class FraudDetectionEngine {

    // Rule constants thresholds
    private static final int RAPID_BOOKING_LIMIT       = 3;   // max allowed bookings
    private static final int RAPID_BOOKING_WINDOW_MINS = 10;  // look-back window in minutes
    private static final int EXCESS_CANCEL_LIMIT       = 2;   // max cancellations allowed

    // ------------------------------------------------------------------
    // Public API — call these from the UI controllers
    // ------------------------------------------------------------------

    
    /**
     * This method will: Runs R01 + R02 before a booking is created.
     * 										If the result is not allowed, do NOT proceed with the booking.
     * @param passengerId
     * @param scheduleId
     * @return FraudCheckResult
     */
    public static FraudCheckResult runPreBookingChecks(int passengerId, int scheduleId) 
    {
        FraudCheckResult r01 = checkDuplicateBooking(passengerId, scheduleId);
        if (!r01.allowed()) 
        	return r01;

        FraudCheckResult r02 = checkRapidBooking(passengerId);
        if (!r02.allowed()) 
        	return r02;
        
        // If r01 and r02 passed, booking may continue.
        return FraudCheckResult.ok();
    }

   
    /**
     * This method will: Run R03 after a booking has been successfully cancelled.
     *									 Never blocks; only creates an alert and returns a flagged result (cancellation already happened).
     * @param passengerId
     * @return FraudCheckResult
     */
    public static FraudCheckResult runPostCancellationChecks(int passengerId) 
    {
        return checkExcessiveCancellations(passengerId);
    }

    // ------------------------------------------------------------------
    // Rule implementations
    // ------------------------------------------------------------------

    /**
     * R01 — Duplicate Booking
     * Blocks if the passenger already has a confirmed booking
     * on the same schedule within the last hour.
     */
    public static FraudCheckResult checkDuplicateBooking(int passengerId, int scheduleId) 
    {
        boolean duplicate = BookingDAO.hasDuplicateBooking(passengerId, scheduleId);

        if (duplicate) 
        {
            String description = "Passenger " + passengerId
                + " attempted duplicate booking on schedule " + scheduleId
                + " within 1 hour.";

            FraudAlertDAO.createFraudAlert(
                passengerId,
                "R01-DUPLICATE_BOOKING",
                description,
                "high"
            );

            return FraudCheckResult.blocked(
                "R01",
                "Duplicate booking detected.\n"
                + "You already have a confirmed booking on this train schedule.\n"
                + "Please wait at least 1 hour before booking the same train again."
            );
        }

        return FraudCheckResult.ok();
    }

    /**
     * R02 — Rapid Booking
     * Blocks if the passenger made more than 3 confirmed bookings
     * within the last 10 minutes.
     */
    public static FraudCheckResult checkRapidBooking(int passengerId) 
    {
        int recentCount = BookingDAO.countBookingsInLastMinutes(
                passengerId, RAPID_BOOKING_WINDOW_MINS);

        if (recentCount > RAPID_BOOKING_LIMIT) 
        {
            String description = "Passenger " + passengerId
                + " made " + recentCount + " bookings in the last "
                + RAPID_BOOKING_WINDOW_MINS + " minutes (limit: " + RAPID_BOOKING_LIMIT + ").";

            FraudAlertDAO.createFraudAlert(
                passengerId,
                "R02-RAPID_BOOKING",
                description,
                "high"
            );

            return FraudCheckResult.blocked(
                "R02",
                "Too many bookings in a short time.\n"
                + "You have made " + recentCount + " bookings in the last "
                + RAPID_BOOKING_WINDOW_MINS + " minutes.\n"
                + "Please wait before making another booking."
            );
        }

        return FraudCheckResult.ok();
    }

  
    /**
     * R03 — Excessive Cancellations
     * This method will: Allow the action but flags the passenger if they cancelled
     *  more than 2 bookings within the last 24 hours.
     * @param passengerId
     * @return the result of the fraud checks
     */
    public static FraudCheckResult checkExcessiveCancellations(int passengerId) 
    {
        int cancelCount = BookingDAO.countCancellationsInLast24Hours(passengerId);

        if (cancelCount > EXCESS_CANCEL_LIMIT) 
        {
            String description = "Passenger " + passengerId
                + " has cancelled " + cancelCount
                + " bookings in the last 24 hours (limit: " + EXCESS_CANCEL_LIMIT + ").";

            FraudAlertDAO.createFraudAlert(
                passengerId,
                "R03-EXCESSIVE_CANCELLATIONS",
                description,
                "medium"
            );

            return FraudCheckResult.flagged(
                "R03",
                "Excessive cancellations detected.\n"
                + "You have cancelled " + cancelCount + " bookings in the last 24 hours.\n"
                + "Your account has been flagged for admin review."
            );
        }

        return FraudCheckResult.ok();
    }

    // ------------------------------------------------------------------
    // Result record
    // ------------------------------------------------------------------

    /**
     * Immutable result returned by every fraud check.
     *
     *   allowed = false  → the booking must be blocked, show reason to user
     *   flagged = true   → booking is allowed but an alert was raised
     */
    public record FraudCheckResult(
        boolean allowed,
        boolean flagged,
        String  ruleId,
        String  reason
    ) {
        /** No fraud detected — proceed normally. */
        public static FraudCheckResult ok() 
        {
            return new FraudCheckResult(true, false, null, null);
        }

        /** Fraud detected — block the booking. */
        public static FraudCheckResult blocked(String ruleId, String reason) 
        {
            return new FraudCheckResult(false, false, ruleId, reason);
        }

        /** Booking allowed but passenger has been flagged for review. */
        public static FraudCheckResult flagged(String ruleId, String reason) 
        {
            return new FraudCheckResult(true, true, ruleId, reason);
        }

        /** Short title suitable for an Alert dialog header. */
        public String getTitle() 
        {
            if (!allowed) 
            	return "Booking Blocked — Fraud Detected";
            if (flagged)  
            	return "Account Flagged";
            return "Booking Approved";
        }
    }
}
