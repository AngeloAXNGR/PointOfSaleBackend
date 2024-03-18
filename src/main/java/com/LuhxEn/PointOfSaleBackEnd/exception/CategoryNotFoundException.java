package com.LuhxEn.PointOfSaleBackEnd.exception;

import com.LuhxEn.PointOfSaleBackEnd.category.Category;

public class CategoryNotFoundException extends RuntimeException{
	public CategoryNotFoundException(String message){super(message);}
}
