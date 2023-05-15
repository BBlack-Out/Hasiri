package com.example.hasiri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Profil extends AppCompatActivity {
    FusedLocationProviderClient fusedLocationClient;
    RecyclerView recyclerView;
    List<Minyan_model> list;
    private DatabaseReference mDatabase;
    List<Minyan_model> Mlist = new ArrayList<>();

    LatLng MylatLng;

    Button Back;
    TextView DidntSigned;
    CheckBox AouidoCheck;
    ImageView AoudioImage;

    public static final String TPHILA_SIGHN = "sharedPrefs";
    public static final String MOED = "moed";
    public static final String IDs = "ID";
    public static final String AUODIO = "AOUDIO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        getSupportActionBar().hide();

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(TPHILA_SIGHN,MODE_PRIVATE);

        Back = findViewById(R.id.bACK);
        Back.setOnClickListener(new View.OnClickListener() {
        @Override
          public void onClick(View v) {
              finish();
               if(sharedPreferences.getBoolean(AUODIO,true))
               {
                final MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.click);
                mediaPlayer.start();
               }
            }
        });

        DidntSigned = findViewById(R.id.DidntSigned);
        if(Objects.equals(sharedPreferences.getString(MOED, "000"), "000"))
        {
            DidntSigned.setVisibility(View.VISIBLE);}

        recyclerView = findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        MinyanAdapter adapter = new MinyanAdapter(Mlist, getApplicationContext(),this,1);
        recyclerView.setAdapter(adapter);

        AouidoCheck = findViewById(R.id.AoudioCheckBox);
        AouidoCheck.setChecked(sharedPreferences.getBoolean(AUODIO,true));
        AoudioImage = findViewById(R.id.AoudioImage);

        if(AouidoCheck.isChecked())
            AoudioImage.setImageDrawable(getDrawable(R.drawable.aoudio_on));
        else
            AoudioImage.setImageDrawable(getDrawable(R.drawable.aoudio_off));

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Minyan");
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

                PublicPrayer publicPrayer = dataSnapshot.getValue(PublicPrayer.class);
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                List<Address> addresses;
                String address = "";
                try {
                    addresses = geocoder.getFromLocation(publicPrayer.getLat(), publicPrayer.getLag(), 1);
                    address = addresses.get(0).getAddressLine(0);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                String HourStr = "" + publicPrayer.getHour();
                if (publicPrayer.getHour() / 10 < 1)
                    HourStr = "0" + publicPrayer.getHour();
                String MinuteStr = "" + publicPrayer.getMinute();
                if (publicPrayer.getMinute() / 10 < 1)
                    MinuteStr = "0" + publicPrayer.getMinute();


                LatLng latLng = new LatLng(publicPrayer.lat,publicPrayer.lag);

                fusedLocationClient = LocationServices.getFusedLocationProviderClient(Profil.this);
                if (ActivityCompat.checkSelfPermission(Profil.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Profil.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                String finalHourStr = HourStr;
                String finalMinuteStr = MinuteStr;
                String finalAddress = address;

                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(Profil.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                MylatLng  = new LatLng(location.getLatitude(), location.getLongitude());
                                int Distance = (int)getDistance(MylatLng,latLng);
                                if(Objects.equals(getIDsave(publicPrayer.getMoed() - 1), dataSnapshot.getKey()))
                                {
                                    Mlist.add(new Minyan_model("נרשמו: 10 / " + publicPrayer.getSignUps(), finalHourStr + " : " + finalMinuteStr, finalAddress, publicPrayer.getMoed(),latLng, Distance + " מטר ממך","" + dataSnapshot.getKey()));
                                }
                                adapter.notifyDataSetChanged();
                            }
                        });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                for(int i = 0; i < Mlist.size(); i++)
                {
                    if(Objects.equals(Mlist.get(i).getId(), dataSnapshot.getKey()))
                        Mlist.remove(i);
                }
                if(Objects.equals(sharedPreferences.getString(MOED, "000"), "000"))
                {DidntSigned.setVisibility(View.VISIBLE);}
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so remove it.
                String commentKey = dataSnapshot.getKey();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        ref.addChildEventListener(childEventListener);

    }
    public void onCheckBoxClicked(View view)
    {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(TPHILA_SIGHN,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(AUODIO,AouidoCheck.isChecked());
        editor.apply();
        if(AouidoCheck.isChecked())
        {
            AoudioImage.setImageDrawable(getDrawable(R.drawable.aoudio_on));
            final MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.sound_on);
            mediaPlayer.start();
        }
        else
        {
            AoudioImage.setImageDrawable(getDrawable(R.drawable.aoudio_off));
            final MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.sound_off);
            mediaPlayer.start();
        }

    }
    private static double getDistance(LatLng p1, LatLng p2) {
        final int R = 6371000; // Earth radius in meters
        double lat1 = p1.latitude;
        double lat2 = p2.latitude;
        double lon1 = p1.longitude;
        double lon2 = p2.longitude;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        Log.d("loh", "" + R * c);
        return R * c;
    }

    //לוקח ID משרד פרפרנסס. num לפי המועד - 1
    public String getIDsave(int num)
    {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(TPHILA_SIGHN,MODE_PRIVATE);
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
//מכניס ID משרד פרפרנסס. num לפי המועד - 1 , str הID של המניין שנבחר, shared זה השרד פרפרנסס של IDs

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
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SharedPreferences sharedPreferences = getSharedPreferences(TPHILA_SIGHN,MODE_PRIVATE);

        if(sharedPreferences.getBoolean(AUODIO,true))
        {
            final MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.click);
            mediaPlayer.start();
        }
    }
}
