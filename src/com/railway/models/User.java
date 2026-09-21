/**
 * Program Name: User.java
 * Purpose: TODO
 * @author Ainash Zhumagulova 
 * Date Jun 26, 2026
 */
package com.railway.models;

/**
 * 
 */
public record User(
		int userId, 
    String username,
    String passwordHash,
    String email,
    String role
		)
{
	// Returns true if this user has admin privileges
	public boolean isAdmin()
	{
		return "admin".equalsIgnoreCase(role);
	}
	
	//Return true if this user is a passenger
	public boolean isPassenger() 
	{
		return "passenger".equalsIgnoreCase(role);
	}
	
	@Override
	public String toString() 
	{
		return "User[id=%d, username=%s, role=%s]".formatted(userId, username, role);
	}
	
}
