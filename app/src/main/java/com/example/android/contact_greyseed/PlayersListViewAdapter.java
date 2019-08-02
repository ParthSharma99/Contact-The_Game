package com.example.android.contact_greyseed;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class PlayersListViewAdapter extends RecyclerView.Adapter {

    private ArrayList<String> players;
    String host = "";
    public PlayersListViewAdapter(ArrayList<String> players) {
        this.players = players;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.player_list_view,viewGroup,false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        String name = players.get(i);
        String side = "";
        if(name.equals(host)){
            side = "( HOST ) ( Leader )";
        }else if(name.equals(new playerName().getName())){
            side = "( YOU )";
        }
        ((ViewHolder)viewHolder).player_name.setText(name);
        ((ViewHolder)viewHolder).player_side.setText(side);
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView player_name,player_side;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            player_name = itemView.findViewById(R.id.player_name_list_adapter);
            player_side = itemView.findViewById(R.id.player_side_list_adapter);
        }
    }

    @Override
    public int getItemCount() {
        return players.size();
    }
}
