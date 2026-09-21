/**
 * Program Name: Passenger.java
 * Purpose: This record represents a passenger profile linked to a User account.
 * @author Ainash Zhumagulova 
 * Date Jun 26, 2026
 */
package com.railway.models;

/**
 * 
 */
public record Passenger(
	int passengerId,
  int userId,
  String fullName,
  String phone,
  String email
  ) 
{
	@Override
	public String toString() 
	{
    return "Passenger[id=%d, name=%s, email=%s]".formatted(passengerId, fullName, email);
	}
}
