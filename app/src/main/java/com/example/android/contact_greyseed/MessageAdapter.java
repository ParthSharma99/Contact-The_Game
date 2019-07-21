package com.example.android.contact_greyseed;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter {


    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private static final int VIEW_TYPE_MESSAGE_UNIVERSAL = 3;
    private static final int VIEW_TYPE_MESSAGE_LEADER = 4;
    public int state = 0;
    public String timeStamp;
    private ArrayList<Message> listItems = new ArrayList<>();
    private OnMessageClickListener messageClickListener;

    public MessageAdapter(ArrayList<Message> l, player_game_screen listener) {
        this.listItems = l;
        this.messageClickListener = listener;
    }


    public MessageAdapter(ArrayList<Message> l, leader_game_screen listener) {
        this.listItems = l;
        this.messageClickListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view ;
        if(i == VIEW_TYPE_MESSAGE_RECEIVED){
            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.recieved_messages,viewGroup,false);
            return new ReceivedViewHolder(view,messageClickListener);
        }

        else if(i == VIEW_TYPE_MESSAGE_SENT){
            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.sent_message,viewGroup,false);
            return new SentViewHolder(view,messageClickListener);
        }else if(i == VIEW_TYPE_MESSAGE_UNIVERSAL){
            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.universal_message,viewGroup,false);
            return new UniversalViewHolder(view);
        }else if(i == VIEW_TYPE_MESSAGE_LEADER){
            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.recieved_leader_message,viewGroup,false);
            return new ReceivedLeaderViewHolder(view,messageClickListener);

        }
        return null;
    }

    @Override
    public int getItemViewType(final int position) {
//        if(position > 0){
            Message msg = listItems.get(position);
            return msg.side;
//        }
//        return 1;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int i) {
        final Message msg = listItems.get(i);

            if(viewHolder.getItemViewType() ==  VIEW_TYPE_MESSAGE_RECEIVED){

                ((ReceivedViewHolder)viewHolder).message.setText(msg.getMsg());
                ((ReceivedViewHolder)viewHolder).player.setText(msg.getSender());
                if(state == 1){
                    ((ReceivedViewHolder) viewHolder).unselect.setVisibility(View.VISIBLE);
                    ((ReceivedViewHolder) viewHolder).select.setVisibility(View.INVISIBLE);
                    if(msg.isSelected()){
                        ((ReceivedViewHolder) viewHolder).select.setVisibility(View.VISIBLE);
                    }
                }else{
                    ((ReceivedViewHolder) viewHolder).unselect.setVisibility(View.GONE);
                    ((ReceivedViewHolder) viewHolder).select.setVisibility(View.GONE);
                }
//                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        msg.toggleSelect();
//
//                        if(msg.isSelected() && ((ReceivedViewHolder) viewHolder).select.getVisibility()==View.GONE){
//                            ((ReceivedViewHolder) viewHolder).select.setVisibility(View.VISIBLE);
//                        }else{
//                            ((ReceivedViewHolder) viewHolder).select.setVisibility(View.GONE);
//                        }
//                    }
//                });
            }
            else if(viewHolder.getItemViewType() == VIEW_TYPE_MESSAGE_SENT){
                ((SentViewHolder)viewHolder).message.setText(msg.getMsg());
                ((SentViewHolder)viewHolder).player.setText(msg.getSender());
                if(state == 1){
                    ((SentViewHolder) viewHolder).unselect.setVisibility(View.VISIBLE);
                    ((SentViewHolder) viewHolder).select.setVisibility(View.INVISIBLE);
                    if(msg.isSelected()){
                        ((SentViewHolder) viewHolder).select.setVisibility(View.VISIBLE);
                    }
                }else{
                    ((SentViewHolder) viewHolder).unselect.setVisibility(View.GONE);
                    ((SentViewHolder) viewHolder).select.setVisibility(View.GONE);
                }

//                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//
//                        msg.toggleSelect();
//                        if(msg.isSelected() && ((SentViewHolder) viewHolder).select.getVisibility() == View.GONE){
//                            ((SentViewHolder) viewHolder).select.setVisibility(View.VISIBLE);
//                        }else{
//                            ((SentViewHolder) viewHolder).select.setVisibility(View.GONE);
//                        }
//                    }
//                });
            }
            else if(viewHolder.getItemViewType() == VIEW_TYPE_MESSAGE_UNIVERSAL){
                ((UniversalViewHolder)viewHolder).message.setText(msg.getMsg());
            }else if(viewHolder.getItemViewType() == VIEW_TYPE_MESSAGE_LEADER){

                    ((ReceivedLeaderViewHolder)viewHolder).message.setText(msg.getMsg());
                    String temp = msg.getSender();
                    temp = temp + " (leader)";
                    ((ReceivedLeaderViewHolder)viewHolder).player.setText(temp);
                if(state == 1){
                    ((ReceivedLeaderViewHolder) viewHolder).unselect.setVisibility(View.VISIBLE);
                    ((ReceivedLeaderViewHolder) viewHolder).select.setVisibility(View.INVISIBLE);
                    if(msg.isSelected()){
                        ((ReceivedLeaderViewHolder) viewHolder).select.setVisibility(View.VISIBLE);
                    }
                }else{
                    ((ReceivedLeaderViewHolder) viewHolder).unselect.setVisibility(View.GONE);
                    ((ReceivedLeaderViewHolder) viewHolder).select.setVisibility(View.GONE);
                }

//                    viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            msg.toggleSelect();
//
//                            if(msg.isSelected() && ((ReceivedLeaderViewHolder) viewHolder).select.getVisibility()==View.GONE){
//                                ((ReceivedLeaderViewHolder) viewHolder).select.setVisibility(View.VISIBLE);
//                            }else{
//                                ((ReceivedLeaderViewHolder) viewHolder).select.setVisibility(View.GONE);
//                            }
//                        }
//                    });

            }
    }



    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public int getSelectedCount(){
        int count = 0;
        for(int i=0;i<getItemCount();i++){
            if(listItems.get(i).isSelected()){
                count++;
            }
        }
        return count;
    }


    public class SentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener{

        public TextView message;
        public TextView player;
        public ImageView select,unselect;
        OnMessageClickListener clickListener;
        SentViewHolder(@NonNull View itemView, OnMessageClickListener listener) {
            super(itemView);
            message = itemView.findViewById(R.id.text_message_body);
            player = itemView.findViewById(R.id.text_message_player);
            select = itemView.findViewById(R.id.selected);
            unselect = itemView.findViewById(R.id.unselected);
            this.clickListener = listener;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }
        @Override
        public void onClick(View view) {
            if(state == 1){
                clickListener.onMessageClick(getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if(state == 0) {
                state = 1;
                clickListener.onMessageClick(getAdapterPosition());
                return true;
            }
            return false;
        }
    }

    public class ReceivedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener{

        public TextView message;
        public TextView player;
        public ImageView select,unselect;
        OnMessageClickListener clickListener;
        ReceivedViewHolder(@NonNull View itemView, OnMessageClickListener listener) {
            super(itemView);
            message = itemView.findViewById(R.id.text_message_body_recieved);
            player = itemView.findViewById(R.id.text_message_name);
            select = itemView.findViewById(R.id.selected);
            unselect = itemView.findViewById(R.id.unselected);
            this.clickListener = listener;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }
        @Override
        public void onClick(View view) {
            if(state == 1){
                clickListener.onMessageClick(getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if(state == 0) {
                state = 1;
                clickListener.onMessageClick(getAdapterPosition());
                return true;
            }
            return false;
        }
    }

    public class ReceivedLeaderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

        public TextView message;
        public TextView player;
        public ImageView select,unselect;
        OnMessageClickListener clickListener;

        ReceivedLeaderViewHolder(@NonNull View itemView, OnMessageClickListener listener) {
            super(itemView);
            message = itemView.findViewById(R.id.text_message_body_recieved);
            player = itemView.findViewById(R.id.text_message_name);
            select = itemView.findViewById(R.id.selected);
            unselect = itemView.findViewById(R.id.unselected);
            this.clickListener = listener;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }
        @Override
        public void onClick(View view) {
            if(state == 1){
                clickListener.onMessageClick(getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if(state == 0) {
                state = 1;
                clickListener.onMessageClick(getAdapterPosition());
                return true;
            }
            return false;
        }
    }

    public class UniversalViewHolder extends RecyclerView.ViewHolder{

        public TextView message;
        UniversalViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.text_message_body_universal);
        }
    }

    public interface OnMessageClickListener{
        void onMessageClick(int pos);
    }

}
