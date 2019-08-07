package com.example.android.contact_greyseed;

import java.util.ArrayList;

public class HintTrack {
    public ArrayList<Integer> list;
    public String author = "";
    public long count = 1;
    private int idx = 0;
    public String timeStamp = "";
    public int hintTrackNumber = 0;
    private boolean selected = false;

    public HintTrack( ArrayList<Integer> list,String a,int h,String timeStamp) {
        this.list = list;
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
    public void setSelected(){selected = true;}
    public boolean getSelected(){return selected;}
    public void toggleSelected(){selected = !selected;}

}
