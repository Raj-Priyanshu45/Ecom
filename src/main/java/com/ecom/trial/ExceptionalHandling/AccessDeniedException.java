package com.ecom.trial.ExceptionalHandling;

public class AccessDeniedException extends RuntimeException{
    
    public AccessDeniedException(String mess){
        super(mess);
    }
    
}