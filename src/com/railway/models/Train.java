/**
 * Program Name: Train.java
 * Purpose: This class will represents a train with its route and pricing info.
 * @author Ainash Zhumagulova 
 * Date Jun 26, 2026
 */
package com.railway.models;

/**
 * 
 */
public record Train(
		int trainId,
    String trainNumber,
    String trainName,
    String sourceStation,
    String destinationStation,
    int totalSeats,
    double baseFare,
    boolean isActive
		)
{
	// Returns a formatted fare string (e.g., "$142.00").
	 public String getFormattedFare() {
     return "$%.2f".formatted(baseFare);
 }

 // Returns route string "Source → Destination". 
 public String getRoute() {
     return sourceStation + " → " + destinationStation;
 }

 @Override
 public String toString() {
     return "Train[%s %s: %s → %s @ $%.2f]"
         .formatted(trainNumber, trainName, sourceStation, destinationStation, baseFare);
 }
}
