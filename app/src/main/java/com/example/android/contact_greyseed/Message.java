package com.example.android.contact_greyseed;

import java.text.SimpleDateFormat;

public class Message {
    private String msg = "";
    private String sender = "";
    public int side = 1;
    public String timeStamp;
    private boolean selected = false;

    public Message(String m,String i, String simpleDateFormat){
        this.msg = m;
        this.sender = i;
        this.timeStamp = simpleDateFormat;
    }

    public String getMsg(){
        return msg;
    }
    public String getSender(){
        return sender;
    }

    public boolean isSelected(){return selected;}

    public void toggleSelect(){selected = !selected;}
}
