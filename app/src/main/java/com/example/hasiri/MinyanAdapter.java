package com.example.hasiri;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.VIBRATOR_SERVICE;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.nio.file.ClosedFileSystemException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MinyanAdapter extends RecyclerView.Adapter<MinyanAdapter.Holder>{

    List<Minyan_model> Models;
    Context context , SharedContext;
    private DatabaseReference mDatabase;
    Activity activity;
    int ActivityNamber;
    public static final String TPHILA_SIGHN = "sharedPrefs";
    public static final String MOED = "moed";
    public static final String IDs = "ID";
    public static final String AUODIO = "AOUDIO";

    public MinyanAdapter(List<Minyan_model> models , Context context , Activity activity, int AcitivityNamber) {
        Models = models;
        this.context = context;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        this.activity = activity;
        this.ActivityNamber = AcitivityNamber;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.minyan_recycler_view,parent,false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        if(ActivityNamber == 1)
        {
            holder.Sign.setVisibility(View.GONE);
            holder.ShowOnMap.setVisibility(View.GONE);

            holder.unSign.setVisibility(View.VISIBLE);
            holder.showonmap.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.Sign.setVisibility(View.VISIBLE);
            holder.ShowOnMap.setVisibility(View.VISIBLE);

            holder.unSign.setVisibility(View.GONE);
            holder.showonmap.setVisibility(View.GONE);
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(TPHILA_SIGHN, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int i = Models.get(position).getImage() - 1;

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
        mDatabase.child("Minyan").child(Models.get(holder.getAdapterPosition()).getId()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(Objects.equals(getIDsave(i), task.getResult().getKey()))
                {
                    holder.Sign.setText("נרשמת");
                    holder.Sign.setEnabled(false);
                    holder.Sign.setBackgroundResource(R.drawable.signed_byn);
                }
                else
                {
                    holder.Sign.setText("הירשם");
                    holder.Sign.setEnabled(true);
                    holder.Sign.setBackgroundResource(R.drawable.byn);
                }
            }
        });

         holder.Sign.setOnClickListener(new View.OnClickListener() {
             @RequiresApi(api = Build.VERSION_CODES.M)
             @Override
             public void onClick(View v) {
                 if(sharedPreferences.getString(MOED,"000").charAt(i) != '1')
                 {
                 mDatabase.child("Minyan").child(Models.get(holder.getAdapterPosition()).getId()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                     @Override
                     public void onComplete(@NonNull Task<DataSnapshot> task) {
                         if(task.isSuccessful()) {
                                 holder.Sign.setText("נרשמת");
                                 holder.Sign.setEnabled(false);
                                 holder.Sign.setBackgroundResource(R.drawable.signed_byn);
                                 task.getResult().getRef().child("signUps").setValue((Long) task.getResult().child("signUps").getValue() + 1);
                                 int Signs= ((Long) task.getResult().child("signUps").getValue()).intValue() + 1;
                                 holder.signs.setText("נרשמו: 10 / " + Signs);
                                 StringBuilder strB = new StringBuilder(sharedPreferences.getString(MOED, "000"));
                                 strB.setCharAt(i,'1');
                                 editor.putString(MOED, strB.toString());
                                 editor.putString(IDs, SaveID(i , task.getResult().getKey(),sharedPreferences.getString(IDs,"n,n,n,")));
                                 editor.apply();
                                 if(sharedPreferences.getBoolean(AUODIO,true))
                                 {
                                     final MediaPlayer mediaPlayer = MediaPlayer.create(context,R.raw.beep_sign);
                                     mediaPlayer.start();
                                 }
                         }
                     }
                 });
                 }
                 else
                 {
                     int i = Models.get(holder.getAdapterPosition()).getImage();
                     String moedStr = "";
                     if(i == 1)
                         moedStr = "שחרית";
                     if(i == 2)
                         moedStr = "מנחה";
                     if(i == 3)
                         moedStr = "ערבית";
                     Toast.makeText(context,"נרשמת כבר למניין " + moedStr, Toast.LENGTH_SHORT).show();

                     Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
                     vibrator.vibrate(100);

                     if(sharedPreferences.getBoolean(AUODIO,true))
                     {
                         final MediaPlayer mediaPlayer = MediaPlayer.create(context,R.raw.error);
                         mediaPlayer.start();
                     }
                 }
             }
         });
         holder.showonmap.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(context,MainActivity.class);
                 intent.putExtra("latLng", Models.get(holder.getAdapterPosition()).getLatLng());
                 intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 context.startActivity(intent);
             }
         });
         holder.unSign.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 mDatabase.child("Minyan").child(Models.get(holder.getAdapterPosition()).getId()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                     @Override
                     public void onComplete(@NonNull Task<DataSnapshot> task) {
                         if(task.isSuccessful()) {
                             task.getResult().getRef().child("signUps").setValue((Long) task.getResult().child("signUps").getValue() - 1);
                             StringBuilder strB = new StringBuilder(sharedPreferences.getString(MOED, "000"));
                             strB.setCharAt(i,'0');
                             editor.putString(MOED, strB.toString());
                             editor.putString(IDs, SaveID(i , "n",sharedPreferences.getString(IDs,"n,n,n,")));
                             editor.apply();
                             Log.d("log", sharedPreferences.getString(MOED, "000"));
                             Log.d("log", sharedPreferences.getString(IDs, "n,n,n,"));
                             Toast.makeText(context, "ביטלת את הרשמתך למניין", Toast.LENGTH_SHORT).show();

                             if(sharedPreferences.getBoolean(AUODIO,true))
                             {
                                 final MediaPlayer mediaPlayer = MediaPlayer.create(context,R.raw.un_sign);
                                 mediaPlayer.start();
                             }
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
        public Button ShowOnMap,Sign,unSign,showonmap;
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
            unSign = itemView.findViewById(R.id.unSign);
            showonmap = itemView.findViewById(R.id.ShowOnMap2);
        }
    }

    public String SaveID(int num, String str, String shared) {
        String[] nums = shared.split(",");
        String resultStr = "";

        if (num == 0) {
            nums[0] = str;
        } else if (num == 1) {
            nums[nums.length/2] = str;
        } else if (num == 2) {
            nums[nums.length-1] = str;
        } else {
            return "";
        }
        for (String n : nums) {
            resultStr += n + ",";
        }
        return resultStr.substring(0, resultStr.length()-1);
    }
    public String getIDsave(int num)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TPHILA_SIGHN,MODE_PRIVATE);
        String str = sharedPreferences.getString(IDs,"n,n,n");
        String[] nums = str.split(",");

        if (num == 2) {
            return nums[nums.length-1];
        } else if (num == 1) {
            return nums[nums.length/2];
        } else if (num == 0) {
            return nums[0];
        } else {
            return "Invalid parameter. Must be 0, 1, or 2.";
        }
    }
}
