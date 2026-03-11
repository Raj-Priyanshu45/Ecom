package com.ecom.trial.ExceptionalHandling;


public class ProductNotFoundException extends RuntimeException{

    public ProductNotFoundException(String mess){
        super(mess);
    }
}