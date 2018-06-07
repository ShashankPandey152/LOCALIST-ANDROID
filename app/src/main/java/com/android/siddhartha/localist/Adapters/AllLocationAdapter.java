package com.android.siddhartha.localist.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.siddhartha.localist.DataStructures.AllLocation;
import com.android.siddhartha.localist.Interfaces.CustomItemClickListener;
import com.android.siddhartha.localist.MainActivity;
import com.android.siddhartha.localist.R;

import java.util.ArrayList;
import java.util.List;

public class AllLocationAdapter extends RecyclerView.Adapter<AllLocationAdapter.Holder> {

    private Context context;
    private List<AllLocation> location;
    CustomItemClickListener listener;

    public AllLocationAdapter(Context applicationContext, ArrayList<AllLocation> listLocation, CustomItemClickListener listener) {
        context = applicationContext;
        location = listLocation;
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.bindLocation(location.get(position), context);
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_location, parent, false);
        final Holder holder = new Holder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(v, holder.getPosition());
            }
        });
        return holder;
    }

    @Override
    public int getItemCount() {
        return location.size();
    }

    class Holder extends RecyclerView.ViewHolder {

        public View locationView;
        public TextView locationNameText;

        public Holder(View locationView) {
            super(locationView);
            locationNameText = locationView.findViewById(R.id.locationNameText);
        }

        public void bindLocation(AllLocation allLocation, Context context) {
            locationNameText.setText(allLocation.locationName);
        }
    }

}
