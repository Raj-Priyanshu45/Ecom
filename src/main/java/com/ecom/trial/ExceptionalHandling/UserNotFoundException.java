package com.ecom.trial.ExceptionalHandling;

public class UserNotFoundException extends RuntimeException{
    
    public UserNotFoundException(String mess){
        super(mess);
    }
}