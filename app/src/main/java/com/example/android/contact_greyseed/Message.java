package com.example.android.contact_greyseed;

public class Message {
    private String msg = "";
    private String sender = "";
    public int side = 1;
    private boolean selected = false;

    public Message(String m,String i){
        this.msg = m;
        this.sender = i;
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
