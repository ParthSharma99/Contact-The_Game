package com.example.android.contact_greyseed;

import java.util.ArrayList;

public class HintTrack {
    public ArrayList<Message> track;
    public ArrayList<Integer> list;
    public String author = "";
    public long count = 1;
    private int idx = 0;
    public String timeStamp = "";
    public int hintTrackNumber = 0;

    public HintTrack( ArrayList<Integer> list,ArrayList<Message> track,String a,int h,String timeStamp) {
        this.list = list;
        this.track = track;
        this.author = a;
        this.hintTrackNumber = h;
        this.timeStamp = timeStamp;
    }
    public int getNext(){
        int temp;
        temp = list.get(idx);
        idx = (idx+1)%list.size();
        return temp;
    }


}
