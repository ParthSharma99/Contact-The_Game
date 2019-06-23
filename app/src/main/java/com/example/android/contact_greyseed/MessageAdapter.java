package com.example.android.contact_greyseed;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter {


    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private ArrayList<Message> listItems = new ArrayList<>();
    private Context context;

    public MessageAdapter(ArrayList<Message> l, Context context) {
        this.listItems = l;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view ;
        if(getItemViewType(i) == VIEW_TYPE_MESSAGE_RECEIVED){
            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.recieved_messages,viewGroup,false);
            return new ReceivedViewHolder(view);
        }

        else if(getItemViewType(i) == VIEW_TYPE_MESSAGE_SENT){
            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.sent_message,viewGroup,false);
            return new SentViewHolder(view);
        }
        return null;
    }

    @Override
    public int getItemViewType(final int position) {
//        if(position > 0){
//            Message msg = listItems.get(position-1);
//
//            if (msg.getSender().equals(new playerName().getName())) {
//                return VIEW_TYPE_MESSAGE_SENT;
//            } else {
//                return VIEW_TYPE_MESSAGE_RECEIVED;
//            }
//        }
        return 1;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int i) {
        final Message msg = listItems.get(i);
        switch (viewHolder.getItemViewType()){
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedViewHolder)viewHolder).message.setText(msg.getMsg());
                ((ReceivedViewHolder)viewHolder).player.setText(msg.getSender());
                viewHolder.itemView.setBackgroundColor(msg.isSelected() ? Color.LTGRAY : Color.WHITE);
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        msg.toggleSelect();
                        if(getSelectedCount() > 3){
                            msg.toggleSelect();
                        }else{
                            viewHolder.itemView.setBackgroundColor(msg.isSelected() ? Color.LTGRAY : Color.WHITE);
                        }
                    }
                });
                break;
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentViewHolder)viewHolder).message.setText(msg.getMsg());
                ((SentViewHolder)viewHolder).player.setText(msg.getSender());
                viewHolder.itemView.setBackgroundColor(msg.isSelected() ? Color.LTGRAY : Color.WHITE);
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        msg.toggleSelect();
                        if(getSelectedCount() > 3){
                            msg.toggleSelect();
                        }else{
                            viewHolder.itemView.setBackgroundColor(msg.isSelected() ? Color.LTGRAY : Color.WHITE);
                        }
                    }
                });
                break;
//            default:
//                ((SentViewHolder)viewHolder).message.setText(msg.getMsg());
//                ((SentViewHolder)viewHolder).player.setText(msg.getSender());
//                break;
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


    public class SentViewHolder extends RecyclerView.ViewHolder{

        public TextView message;
        public TextView player;
        SentViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.text_message_body);
            player = itemView.findViewById(R.id.text_message_time);
        }


    }

    public class ReceivedViewHolder extends RecyclerView.ViewHolder{

        public TextView message;
        public TextView player;
        ReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.text_message_body_recieved);
            player = itemView.findViewById(R.id.text_message_time_recieved);
        }
    }



}
