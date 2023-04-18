package com.example.hasiri;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
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
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Locale;


public class AddMinyan extends AppCompatActivity implements OnMapReadyCallback {

    Button timeButton, Back, tzor;
    ImageButton shaharitB, minhaB, arvitB, MyLocationB;
    EditText heara;
    int PLACE_PICKER_REQUEST = 1;
    int hour, minute;
    LatLng latLng;
    int moed;
    private GoogleMap mMap;
    MapView mapView;
    String TimeButtStr;
    PublicPrayer publicPrayer;
    SearchView mSearchView;

    FusedLocationProviderClient fusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_minyan);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        getSupportActionBar().hide();

        timeButton = findViewById(R.id.TimePickerButton);
        Back = findViewById(R.id.Back);
        mapView = findViewById(R.id.map_view);
        shaharitB = findViewById(R.id.Shaharit);
        minhaB = findViewById(R.id.Minha);
        arvitB = findViewById(R.id.Arvit);
        heara = findViewById(R.id.heara);
        tzor = findViewById(R.id.tzor);
        MyLocationB = findViewById(R.id.MyLocation);


        TimeButtStr = "";
        moed = 0;

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);


        mSearchView = findViewById(R.id.search_view);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform the search here
                searchAddress(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Update the search results here
                return false;
            }
        });


    }

    private void searchAddress(String address) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(address.length() <=0)
                        { return;}

                        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                        List<Address> addresses = null;

                        try {
                            addresses = geocoder.getFromLocationName(address, 1);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (addresses != null && !addresses.isEmpty()) {
                            mMap.clear();
                            Address foundAddress = addresses.get(0);
                            LatLng latLngC = new LatLng(foundAddress.getLatitude(), foundAddress.getLongitude());
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngC, 15f));
                            Marker marker = mMap.addMarker(new MarkerOptions().position(latLngC));
                            latLng = latLngC;
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "כתובת לא נמצאה", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }}).start();
    }


    public void OnClick(View view)
    {
        if(view == Back)
            finish();
        if(view == shaharitB)
        {
            shaharitB.setBackgroundColor(Color.parseColor("#64dbf5"));
            minhaB.setBackgroundColor(Color.parseColor("#E6E6E6"));
            arvitB.setBackgroundColor(Color.parseColor("#E6E6E6"));
            moed = 1;
        }
        if(view == minhaB)
        {
            minhaB.setBackgroundColor(Color.parseColor("#64dbf5"));
            shaharitB.setBackgroundColor(Color.parseColor("#E6E6E6"));
            arvitB.setBackgroundColor(Color.parseColor("#E6E6E6"));
            moed = 2;
        }
        if(view == arvitB)
        {
            arvitB.setBackgroundColor(Color.parseColor("#64dbf5"));
            shaharitB.setBackgroundColor(Color.parseColor("#E6E6E6"));
            minhaB.setBackgroundColor(Color.parseColor("#E6E6E6"));
            moed = 3;
        }
        if(view == tzor)
        {
            if(TimeButtStr != "" && latLng != null && moed != 0)
            {
                Toast.makeText(getApplicationContext(), "מניין נוצר בהצלחה", Toast.LENGTH_SHORT).show();
                SetInFireBace();
            }
            else
            {
                if(latLng == null)
                Toast.makeText(getApplicationContext(), "יש לבחור מיקום", Toast.LENGTH_SHORT).show();

                if(TimeButtStr == "")
                    Toast.makeText(getApplicationContext(), "יש לבחור שעה", Toast.LENGTH_SHORT).show();

                if(moed == 0)
                    Toast.makeText(getApplicationContext(), "יש לבחור תפילה", Toast.LENGTH_SHORT).show();

            }
        }
        if(view == MyLocationB)
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

                            if (location != null) {
                                String addres;
                                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                                List <Address> addresses;
                                try {
                                    addresses = geocoder.getFromLocation(location.getLatitude() ,location.getLongitude(), 1);
                                    addres = addresses.get(0).getAddressLine(0);
                                    mSearchView.setQuery(addres,true);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            else
                                Toast.makeText(getApplicationContext(), "אין אפשרות למצוא את מיקומך", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public  void SetInFireBace()
    {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference Ref = database.getReference("Minyan");
        DatabaseReference RefCounter = database.getReference("Counter");
        publicPrayer = new PublicPrayer(moed,latLng.latitude,latLng.longitude,hour,minute,"" + heara.getText());

        RefCounter.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful())
                {
                    String Count = "" + task.getResult().getValue();
                    Ref.child(Count).setValue(publicPrayer);
                    int i = Integer.parseInt(Count);
                    i++;
                    RefCounter.setValue("" + i);
                }
            }
        });

       // ref.child("Minyan").child("" + ref2.getKey()).setValue(publicPrayer);
        finish();
    }
    public void popTimePicker(View view)
    {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int SelectedHour, int SelectedMinute) {
                hour = SelectedHour;
                minute = SelectedMinute;

                String HourStr = "" + hour;
                if(hour / 10 < 1)
                    HourStr = "0" + hour;
                String MinuteStr = "" + minute;
                if(minute / 10 < 1)
                    MinuteStr = "0" + minute;

                TimeButtStr = HourStr + " : " + MinuteStr;
                timeButton.setText(TimeButtStr);
            }
        };
        int style = AlertDialog.THEME_HOLO_LIGHT;
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,style,onTimeSetListener,hour,minute,true);
        timePickerDialog.setTitle("בחר שעה");
        timePickerDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        // mMap.addMarker();

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
        mMap.setMyLocationEnabled(true);
        }
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}