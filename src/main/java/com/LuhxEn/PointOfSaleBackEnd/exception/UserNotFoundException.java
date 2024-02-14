package com.LuhxEn.PointOfSaleBackEnd.exception;

public class UserNotFoundException extends RuntimeException{
	public UserNotFoundException(String message){
		super(message);
	}
}
