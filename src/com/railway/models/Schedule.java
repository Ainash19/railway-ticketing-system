/**
 * Program Name: Schedule.java
 * Purpose: This class represents a scheduled departure of a train on a specific date/time.
 * @author Ainash Zhumagulova 
 * Date Jun 26, 2026
 */
package com.railway.models;

/**
 * 
 */
public record Schedule(
		int scheduleId,
    int trainId,
    String departureDate,
    String departureTime,
    String arrivalDate,
    String arrivalTime,
    int availableSeats
		)
{
	// Returns true if there is at least one available seat. 
	 public boolean hasAvailableSeats() 
	 {
     return availableSeats > 0;
	 }
	 
	 // Returns a human-readable departure string. 
   public String getDepartureSummary() 
   {
       return departureDate + " at " + departureTime;
   }

   // Returns a human-readable arrival string. 
   public String getArrivalSummary() 
   {
       return arrivalDate + " at " + arrivalTime;
   }

   @Override
   public String toString() 
   {
       return "Schedule[id=%d, trainId=%d, %s → %s, seats=%d]"
           .formatted(scheduleId, trainId, getDepartureSummary(), getArrivalSummary(), availableSeats);
   }
}
