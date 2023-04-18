package com.example.hasiri;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MinyanAdapter extends RecyclerView.Adapter<MinyanAdapter.Holder> {

    List<Minyan_model> Models;
    Context context;
    private DatabaseReference mDatabase;
    Activity activity;

    public MinyanAdapter(List<Minyan_model> models , Context context , Activity activity) {
        Models = models;
        this.context = context;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        this.activity = activity;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.minyan_recycler_view,parent,false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.ShowOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,MainActivity.class);
                intent.putExtra("latLng", Models.get(holder.getAdapterPosition()).getLatLng());
                intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            }
         });
         holder.Sign.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 mDatabase.child("Minyan").child(Models.get(holder.getAdapterPosition()).getId()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                     @Override
                     public void onComplete(@NonNull Task<DataSnapshot> task) {
                         if(task.isSuccessful()) {
                             task.getResult().getRef().child("signUps").setValue((Long) task.getResult().child("signUps").getValue() + 1);
                         }
                     }
                 });
             }
         });

         holder.address.setText(Models.get(position).getAddress());
         holder.signs.setText(Models.get(position).getSignsUp());
         holder.Time.setText(Models.get(position).getTime());
         holder.Distance.setText(Models.get(position).getDistance());
         if(Models.get(position).getImage() == 1)
         {holder.imageView.setImageDrawable(context.getDrawable(R.drawable.sahrit_marker)); holder.moed.setText("(שחרית)"); }
        if(Models.get(position).getImage() == 2)
        {holder.imageView.setImageDrawable(context.getDrawable(R.drawable.minha_marker)); holder.moed.setText("(מנחה)");}
        if(Models.get(position).getImage() == 3)
        {holder.imageView.setImageDrawable(context.getDrawable(R.drawable.arvit_marker)); holder.moed.setText("(ערבית)");}

    }

    @Override
    public int getItemCount() {
        return Models.size();
    }

    public static class Holder extends RecyclerView.ViewHolder
    {
        public ImageView imageView;
        public TextView signs,Time,address,moed,Distance;
        public Button ShowOnMap,Sign;
        public Holder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            moed = itemView.findViewById(R.id.moed);
            signs = itemView.findViewById(R.id.signs);
            Time = itemView.findViewById(R.id.time);
            address = itemView.findViewById(R.id.address);
            Sign = itemView.findViewById(R.id.Sign);
            ShowOnMap = itemView.findViewById(R.id.ShowOnMap);
            Distance = itemView.findViewById(R.id.Distance);
        }
    }

}
