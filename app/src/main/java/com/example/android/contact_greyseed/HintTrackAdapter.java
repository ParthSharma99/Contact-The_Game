package com.example.android.contact_greyseed;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class HintTrackAdapter extends RecyclerView.Adapter {

    private ArrayList<HintTrack> list;
    private OnHintClickListener onHintClickListener;

    public HintTrackAdapter(ArrayList<HintTrack> list, player_game_screen listener) {
        this.list = list;
        this.onHintClickListener = listener;
    }
    public HintTrackAdapter(ArrayList<HintTrack> list, leader_game_screen listener) {
        this.list = list;
        this.onHintClickListener = listener;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.hints_track,viewGroup,false);
        return new ViewHolder(view,onHintClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        final HintTrack hintTrack = list.get(i);
        ((ViewHolder)viewHolder).number.setText(String.valueOf(i+1));
        ((ViewHolder)viewHolder).no_contacts.setText(String.valueOf(hintTrack.count));

//        ((ViewHolder)viewHolder).letter.setText(hintTrack.getName().charAt(0));
    }
    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView letter;
        public TextView number;
        public TextView no_contacts;

        OnHintClickListener clickListener;
        public ViewHolder(@NonNull View itemView, OnHintClickListener listener) {
            super(itemView);
            letter = itemView.findViewById(R.id.hint_track_letter);
            number = itemView.findViewById(R.id.hint_track_number);
            no_contacts = itemView.findViewById(R.id.number_of_contacts);
            this.clickListener = listener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            clickListener.onClick(getAdapterPosition());
        }

    }
    public interface OnHintClickListener{
        void onClick(int pos);
    }

}
