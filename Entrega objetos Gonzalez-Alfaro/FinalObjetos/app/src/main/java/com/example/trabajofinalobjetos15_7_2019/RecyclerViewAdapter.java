package com.example.trabajofinalobjetos15_7_2019;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends  RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    private ArrayList<String> locationList;

    public RecyclerViewAdapter(ArrayList<String> locationList) {
        this.locationList = locationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_item,viewGroup,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {

        viewHolder.location.setText(locationList.get(i));
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView location;
        RelativeLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            location = itemView.findViewById(R.id.item);
            layout = itemView.findViewById(R.id.parent_layout);
        }
    }
}
