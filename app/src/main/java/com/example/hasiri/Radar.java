package com.example.hasiri;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Radar extends AppCompatActivity {

    FusedLocationProviderClient fusedLocationClient;
    Button Back;
    RecyclerView recyclerView;
    ArrayList<Minyan_model> minyanModels = new ArrayList<>();
    private DatabaseReference mDatabase;
    long size;
    List<Minyan_model> Mlist = new ArrayList<>();

    static int moed;
    static boolean close;

    LatLng MylatLng;

    List<Minyan_model> list;
    CheckBox shahritBox,minhaBox,arvitBox,hacolBox,closestBox;

    public static final String TPHILA_SIGHN = "sharedPrefs";
    public static final String MOED = "moed";
    public static final String IDs = "ID";
    public static final String AUODIO = "AOUDIO";


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radar);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        getSupportActionBar().hide();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        Back = findViewById(R.id.Back);
        recyclerView = findViewById(R.id.RecycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        shahritBox = findViewById(R.id.ShaharitcheckBox);
        minhaBox = findViewById(R.id.MinhacheckBox);
        arvitBox = findViewById(R.id.ArvitcheckBox);
        hacolBox = findViewById(R.id.HacolcheckBox);
        closestBox = findViewById(R.id.closestBox);

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(TPHILA_SIGHN,MODE_PRIVATE);

        MinyanAdapter adapter = new MinyanAdapter(Mlist, getApplicationContext(),this,0);
        recyclerView.setAdapter(adapter);

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
                        addresses = geocoder.getFromLocation(publicPrayer.getLat(), publicPrayer.getLag(), 2);
                        if(!addresses.isEmpty())
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

                fusedLocationClient = LocationServices.getFusedLocationProviderClient(Radar.this);
                if (ActivityCompat.checkSelfPermission(Radar.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Radar.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                        .addOnSuccessListener(Radar.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {

                                String finalTime = finalHourStr + " : " + finalMinuteStr;
                                Calendar calendar = Calendar.getInstance();
                                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                                int currentMinute = calendar.get(Calendar.MINUTE);
                                if(publicPrayer.getHour() == currentHour && publicPrayer.getMinute() <= currentMinute && publicPrayer.getMinute() >= currentMinute + 4)
                                {
                                    finalTime = "התחיל";
                                }
                                MylatLng  = new LatLng(location.getLatitude(), location.getLongitude());
                                int Distance = (int)getDistance(MylatLng,latLng);
                                Mlist.add(new Minyan_model("נרשמו: 10 / " + publicPrayer.getSignUps(), finalTime , finalAddress, publicPrayer.getMoed(),latLng, Distance + " מטר ממך","" + dataSnapshot.getKey()));
                                adapter.notifyDataSetChanged();
                            }
                        });

                }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
//                 int id = Integer.parseInt("" + dataSnapshot.getRef().getKey());
//                 PublicPrayer publicPrayer = dataSnapshot.getValue(PublicPrayer.class);
//                 Mlist.get(id).setSignsUp( "נרשמו: 10 / " + publicPrayer.getSignUps());
//                 Mlist.set(id,Mlist.get(id));
//                 adapter.notifyDataSetChanged();
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


    private void setUpMinyanModeles(int moed, boolean close) throws IOException {

        mDatabase.child("Counter").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful())
                    size = Long.parseLong("" + task.getResult().getValue());
            }
        });

        if(moed == 0)
        list = new ArrayList<>(Mlist);
        else
        {
            list = new ArrayList<>();
            for(int i = 0; i < Mlist.size() ; i++)
            {
                if(Mlist.get(i).getImage() == moed)
                    list.add(Mlist.get(i));
            }
        }
        if(close == true)
        {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            LatLng latLng;
                            if (location != null) {
                                LatLng latLng1= new LatLng(location.getLatitude(),location.getLongitude());

                                list = sortMinyanByDistance(latLng1,list);

                                MinyanAdapter adapter = new MinyanAdapter(list, getApplicationContext(), Radar.this,0);
                                recyclerView.setAdapter(adapter);
                            }
                            else
                                Toast.makeText(getApplicationContext(), "אין אפשרות למצוא את מיקומך", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        MinyanAdapter adapter = new MinyanAdapter(list, getApplicationContext(), this,0);
        recyclerView.setAdapter(adapter);
    }


    public static List<Minyan_model> sortMinyanByDistance(LatLng target, List<Minyan_model> minyanList) {
        // Define a Comparator to compare the distance between two Minyan_model objects and the target LatLng

        Comparator<Minyan_model> distanceComparator = new Comparator<Minyan_model>() {
            @Override
            public int compare(Minyan_model m1, Minyan_model m2) {
                LatLng m1LatLng = m1.getLatLng();
                LatLng m2LatLng = m2.getLatLng();
                double m1Distance = getDistance(target, m1LatLng);
                double m2Distance = getDistance(target, m2LatLng);

                return Double.compare(m1Distance, m2Distance);
            }
        };

        // Sort the minyanList using the distanceComparator
        Collections.sort(minyanList, distanceComparator);

        // Return the sorted list
        return minyanList;

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



    public void OnClick(View view) throws IOException {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(TPHILA_SIGHN,MODE_PRIVATE);
        if(view == Back)
        {
            finish();
            if(sharedPreferences.getBoolean(AUODIO,true))
            {
                final MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.click);
                mediaPlayer.start();
            }

        }
        if(view == shahritBox)
        {
            minhaBox.setChecked(false);
            arvitBox.setChecked(false);
            hacolBox.setChecked(false);
            moed = 1;
            if(sharedPreferences.getBoolean(AUODIO,true))
            {
                final MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.check_box);
                mediaPlayer.start();
            }
        }
        if(view == minhaBox)
        {
            shahritBox.setChecked(false);
            arvitBox.setChecked(false);
            hacolBox.setChecked(false);
            moed = 2;
            if(sharedPreferences.getBoolean(AUODIO,true))
            {
                final MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.check_box);
                mediaPlayer.start();
            }
        }
        if(view == arvitBox)
        {
            shahritBox.setChecked(false);
            minhaBox.setChecked(false);
            hacolBox.setChecked(false);
            moed = 3;
            if(sharedPreferences.getBoolean(AUODIO,true))
            {
                final MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.check_box);
                mediaPlayer.start();
            }
        }
        if(view == hacolBox)
        {
            shahritBox.setChecked(false);
            minhaBox.setChecked(false);
            arvitBox.setChecked(false);
            moed = 0;
            if(sharedPreferences.getBoolean(AUODIO,true))
            {
                final MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.check_box);
                mediaPlayer.start();
            }
        }
        if(view == closestBox){
            if(closestBox.isChecked())
                close = true;
            else
                close = false;

            if(sharedPreferences.getBoolean(AUODIO,true))
            {
                final MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.check_box);
                mediaPlayer.start();
            }
        }
        setUpMinyanModeles(moed,close);
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


