package com.ecom.trial.DTOs.ResponseDTOs;

public class Response {
    private String mess;
        public Response(String mess){
            this.mess = mess;
        }
        public void setMess(String mess){ this.mess = mess; }

        public String getMess(){ return mess;}
}