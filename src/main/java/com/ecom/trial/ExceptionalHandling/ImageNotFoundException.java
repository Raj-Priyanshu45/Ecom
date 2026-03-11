package com.ecom.trial.ExceptionalHandling;

public class ImageNotFoundException extends RuntimeException{

    public ImageNotFoundException(String mess) {
        super(mess);
    }

}
