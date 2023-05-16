package com.example.hasiri;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Icon;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
//import android.widget.SearchView;
import androidx.appcompat.widget.SearchView;

import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.BitmapDescriptor;
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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

//import kotlinx.coroutines.scheduling.Task;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    public static final int REQUEST_CODE = 121;
    private GoogleMap mMap;
    MapView mapView;
    boolean isPermissionGranted;
    private LocationRequest locationRequest;
    public static final int REQUEST_CHECK_SETTING = 1001;
    public SearchView mSearchView;
    RelativeLayout relativeLayout;
    private DatabaseReference mDatabase;
    ImageButton AddMinyanB,ProfileB,RadarB;

    List<PublicPrayer> Plist = new ArrayList<>();

    FusedLocationProviderClient fusedLocationClient;

    public static final String TPHILA_SIGHN = "sharedPrefs";
    public static final String MOED = "moed";
    public static final String IDs = "ID";
    public static final String AUODIO = "AOUDIO";


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Calendar calendar = Calendar.getInstance();
        Toast.makeText(MainActivity.this, calendar.getTimeZone().getDisplayName(), Toast.LENGTH_SHORT).show();

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        getSupportActionBar().hide();

        SharedPreferences sharedPreferences = getSharedPreferences(TPHILA_SIGHN,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(AUODIO,true);

        mapView = findViewById(R.id.map_view);

        checkPremission();
        mapView.getMapAsync(this);

        mapView.onCreate(savedInstanceState);

        AddMinyanB = findViewById(R.id.AddMinyan);
        RadarB = findViewById(R.id.ClosestMinyan);
        ProfileB = findViewById(R.id.Profile);

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
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Minyan");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    PublicPrayer publicPrayer = childSnapshot.getValue(PublicPrayer.class);
                    Plist.add(publicPrayer);
                }
                for(int i = 0; i < 3 ; i++)
                {
                     if(sharedPreferences.getString(MOED,"000").charAt(i) == '1')
                        Log.d("log", "" +checkIdInList(Plist , getIDsave(i), i));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors here
            }
        });

        mDatabase = database.getReference();
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
                            Address foundAddress = addresses.get(0);
                            LatLng latLng = new LatLng(foundAddress.getLatitude(), foundAddress.getLongitude());
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));

                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "כתובת לא נמצאה", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        }}).start();
    }



    private void checkPremission() {
        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                Toast.makeText(MainActivity.this, "Permission is Granted", Toast.LENGTH_SHORT).show();
                isPermissionGranted = true;
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), "");
                intent.setData(uri);
                startActivity(intent);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        boolean searchForMarker = false;
        mMap = googleMap;
        SharedPreferences sharedPreferences = getSharedPreferences(TPHILA_SIGHN,MODE_PRIVATE);


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

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Minyan");
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                PublicPrayer publicPrayer = dataSnapshot.getValue(PublicPrayer.class);

                Calendar calendar = Calendar.getInstance();
                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                int currentMinute = calendar.get(Calendar.MINUTE);

                if (publicPrayer.getHour() > currentHour || (publicPrayer.getHour() == currentHour && publicPrayer.getMinute() > currentMinute) || (publicPrayer.getMoed() == 1 && (currentHour > 20 || currentHour < 12)))
                {

                    LatLng minyan = new LatLng(publicPrayer.getLat(), publicPrayer.getLag());
                    String addres = "";
                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    List <Address> addresses;
                    try {
                        addresses = geocoder.getFromLocation(minyan.latitude ,minyan.longitude, 1);
                        addres = addresses.get(0).getAddressLine(0);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int i =0;

                    int height = 118   ;
                    int width = 118;
                    BitmapDrawable bitmapdraw = null;

                    if(publicPrayer.getMoed() == 1)
                    {bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.sahrit_marker);}
                    if(publicPrayer.getMoed() == 2)
                    {bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.minha_marker);}
                    if(publicPrayer.getMoed() == 3)
                    {bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.arvit_marker);}

                    Bitmap b = bitmapdraw.getBitmap();
                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                    Marker marker = mMap.addMarker(new MarkerOptions().position(minyan).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)).title("m" + dataSnapshot.getKey() +") " + addres));
                }
                else
                {
                    SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(TPHILA_SIGHN, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if(Objects.equals(getIDsave(publicPrayer.getMoed() - 1), dataSnapshot.getKey()))
                    {
                        editor.putString(IDs, SaveID(publicPrayer.getMoed() - 1 ,"n" ,sharedPreferences.getString(IDs,"n,n,n,")));
                        StringBuilder strB = new StringBuilder(sharedPreferences.getString(MOED, "000"));
                        strB.setCharAt(publicPrayer.getMoed() - 1,'0');
                        editor.putString(MOED, strB.toString());
                        editor.apply();
                    }
                    dataSnapshot.getRef().removeValue();
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {

            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so remove it.
                String commentKey = dataSnapshot.getKey();
                Log.d("log", dataSnapshot.getKey()+" deleted");
                mapView.getMapAsync(MainActivity.this);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        ref.addChildEventListener(childEventListener);
        Toast.makeText(MainActivity.this, "Map Loaded", Toast.LENGTH_SHORT).show();



        try {

            LatLng latLng = getIntent().getParcelableExtra("latLng");
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20f));
            if(isGPSon())
                mMap.setMyLocationEnabled(true);
            if(sharedPreferences.getBoolean(AUODIO,true))
            {
                final MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.map);
                mediaPlayer.start();
            }
            searchForMarker = true;
        }
        catch (Exception e){}

        if(isGPSon() && !searchForMarker){
            mMap.setMyLocationEnabled(true);

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                            }
                        }
                    });
        }
        else
        {
            turnOnGps();
            if(isGPSon() && !searchForMarker){
                mMap.setMyLocationEnabled(true);
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                                }
                            }
                        });
            }


        }

        mMap.setOnMarkerClickListener(this);
    }

    public boolean checkIdInList(List<PublicPrayer> list, String id , int index) {
        SharedPreferences sharedPreferences = getSharedPreferences(TPHILA_SIGHN,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for (int i = 0 ; i < list.size() ; i++) {
            if (Objects.equals(list.get(i).getId(), id)) {
                Log.d("log", id + " == " + list.get(i).getId());
                return true;
            }
        }
        Log.d("log", sharedPreferences.getString(MOED, "000"));
        Log.d("log", sharedPreferences.getString(IDs, "n,n,n,"));
        Log.d("log", getIDsave(index) + "  " + id);
        StringBuilder strB = new StringBuilder(sharedPreferences.getString(MOED, "000"));
        strB.setCharAt(index,'0');
        editor.putString(MOED, strB.toString());
        editor.putString(IDs, SaveID(index , "n",sharedPreferences.getString(IDs,"n,n,n,")));
        editor.apply();
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private boolean isGPSon()
    {
        LocationManager locationManager = null;
        boolean isON= false;
        if(locationManager == null)
        {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        isON = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isON;
    }

    private void turnOnGps()
    {
        locationRequest = com.google.android.gms.location.LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext()).checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(MainActivity.this, "Gps IS ON", Toast.LENGTH_SHORT).show();
                } catch (ApiException e) {

                    switch (e.getStatusCode())
                    {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException= (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(MainActivity.this,REQUEST_CHECK_SETTING);
                            }
                            catch (IntentSender.SendIntentException ex) {

                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;

                    }
                }
            }
          });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CHECK_SETTING)
        {
            switch (resultCode){
                case Activity.RESULT_OK:
                    Toast.makeText(this, "GPS turned On", Toast.LENGTH_SHORT).show();
                    mapView.getMapAsync(this);
                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(this, "GPS is requird be turned on", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK)
        {
            LatLng latLng = data.getParcelableExtra("latLng");
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
            Log.d("loh", "hegia");

        }
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
    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {


        SharedPreferences sharedPreferences = getSharedPreferences(TPHILA_SIGHN,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(MOED , "000");
        editor.putString(IDs , "n,n,n,");

        String id = "";
        for(int i = 0; i < marker.getTitle().length(); i++)
        {
            if(marker.getTitle().charAt(i) == ')')
                break;
            id = id + marker.getTitle().charAt(i);
        }

        if(sharedPreferences.getBoolean(AUODIO,true))
        {
            final MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.click);
            mediaPlayer.start();
        }

        if(id.charAt(0) == 'm')
        {
            id = id.substring(1);
            Log.d("log", id);
            mDatabase.child("Minyan").child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(task.isSuccessful()) {
                        PublicPrayer p = task.getResult().getValue(PublicPrayer.class);
                        Dialog dialog = new Dialog(MainActivity.this);
                        dialog.setContentView(R.layout.minyandialog);
                        TextView Moed,Signs,time,address,heara;
                        Button signTo;
                        Moed = dialog.findViewById(R.id.Moed);
                        Signs = dialog.findViewById(R.id.Signs);
                        time = dialog.findViewById(R.id.Time);
                        address = dialog.findViewById(R.id.Address);
                        heara = dialog.findViewById(R.id.Heara);
                        signTo = dialog.findViewById(R.id.SignTo);



                        Long moedFire = (Long) task.getResult().child("moed").getValue();
                        if(moedFire == 1) Moed.setText("שחרית");
                        else if(moedFire == 2) Moed.setText("מנחה");
                        else Moed.setText("ערבית");

                        Long signsFire = (Long) task.getResult().child("signUps").getValue();
                        Signs.setText("נרשמו: 10/ " + signsFire);

                        Long minuteFire =(Long) task.getResult().child("minute").getValue();
                        Long hourFire =(Long) task.getResult().child("hour").getValue();
                        String minStr = "" + minuteFire;
                        String hourStr= "" + hourFire;
                        if(minuteFire / 10 < 1)
                            minStr = "0" + minuteFire;

                        if(hourFire / 10 < 1)
                            hourStr = "0" + hourFire;

                        time.setText("שעה: "+minStr + " : " + hourStr);

                        Calendar calendar = Calendar.getInstance();
                        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int currentMinute = calendar.get(Calendar.MINUTE);

                        if(p.getHour() == currentHour && p.getMinute() >= currentMinute && p.getMinute() <= currentMinute + 4)
                        {
                            time.setText("התחיל");
                            time.setTypeface(null, Typeface.BOLD);
                        }

                        double latFire = (double) task.getResult().child("lat").getValue();
                        double lagFire = (double) task.getResult().child("lag").getValue();
                        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                        List <Address> addresses;
                        try {
                            addresses = geocoder.getFromLocation(latFire ,lagFire, 1);
                            address.setText(addresses.get(0).getAddressLine(0));
                            String str = (String) address.getText();
                            if(address.getText().length() > 30)
                                address.setTextSize(17);
                            if(address.getText().length() > 50)
                            {address.setTextSize(15);
                                address.setText(str.substring(0,50) + "...");
                            }
                            Log.d("log", str);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        String hearaFire = (String) task.getResult().child("heara").getValue();
                        heara.setText(hearaFire);

                        int i = ((Long)task.getResult().child("moed").getValue()).intValue() - 1;

                        if(Objects.equals(getIDsave(i), task.getResult().getKey()))
                        {
                            signTo.setText("נרשמת למניין");
                            signTo.setEnabled(false);
                            signTo.setBackgroundColor(Color.parseColor("#22f57e"));
                        }

                        signTo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int i = ((Long)task.getResult().child("moed").getValue()).intValue() - 1;
                                if(sharedPreferences.getString(MOED,"000").charAt(i) != '1')
                                {
                                    long signupFire = (Long) task.getResult().child("signUps").getValue();
                                    signupFire += 1;

                                    task.getResult().getRef().child("signUps").setValue((Long) task.getResult().child("signUps").getValue() + 1);

                                    Signs.setText("נרשמו: 10/ " + signupFire);

                                    signTo.setText("נרשמת למניין");
                                    signTo.setEnabled(false);
                                    signTo.setBackgroundColor(Color.parseColor("#22f57e"));

                                    StringBuilder strB = new StringBuilder(sharedPreferences.getString(MOED, "000"));
                                    strB.setCharAt(i,'1');
                                    editor.putString(MOED, strB.toString());

                                    editor.putString(IDs, SaveID(i , task.getResult().getKey(),sharedPreferences.getString(IDs,"n,n,n,")));
                                    editor.apply();

                                    if(sharedPreferences.getBoolean(AUODIO,true))
                                    {
                                        final MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.beep_sign);
                                        mediaPlayer.start();
                                    }

                                    Long minuteFire =(Long) task.getResult().child("minute").getValue();
                                    Long hourFire =(Long) task.getResult().child("hour").getValue();
                                    String minStr = "" + minuteFire;
                                    String hourStr= "" + hourFire;
                                    if(minuteFire / 10 < 1)
                                        minStr = "0" + minuteFire;

                                    if(hourFire / 10 < 1)
                                        hourStr = "0" + hourFire;

                                    int hour = p.getHour();
                                    int minute = p.getMinute();
                                    if(minute <= 15 && minute >= 10)
                                        minute = 0;
                                    else if(minute < 10)
                                    {minute = 50; hour -= 1;}
                                    else minute -= 10;
                                    setAlarm(hour ,minute ,"מניין " + Moed.getText().toString(), "הינך נרשמת למניין " + Moed.getText().toString() + " בשעה " +minStr + " : " + hourStr + " ");
                                    Toast.makeText(MainActivity.this, "נרשמת בהצלחה למניין", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    i++;
                                    String moedStr = "";
                                    if(i == 1)
                                        moedStr = "שחרית";
                                    if(i == 2)
                                        moedStr = "מנחה";
                                    if(i == 3)
                                        moedStr = "ערבית";
                                    Toast.makeText(MainActivity.this,"נרשמת כבר למניין " + moedStr, Toast.LENGTH_SHORT).show();

                                    Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(VIBRATOR_SERVICE);
                                    vibrator.vibrate(100);

                                    if(sharedPreferences.getBoolean(AUODIO,true))
                                    {
                                        final MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.error);
                                        mediaPlayer.start();
                                    }
                                }
                            }
                        });
                        Log.d("log", sharedPreferences.getString(MOED, "000"));
                        Log.d("log", sharedPreferences.getString(IDs, "n,n,n,"));

                        dialog.show();
                    }
                }
            });
        }
        return false;
    }
//לוקח ID משרד פרפרנסס. num לפי המועד - 1
    public String getIDsave(int num)
    {
          SharedPreferences sharedPreferences = getSharedPreferences(TPHILA_SIGHN,MODE_PRIVATE);
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
    }

    public void OnClick(View view)
    {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(TPHILA_SIGHN,MODE_PRIVATE);
        if(view == AddMinyanB)
        {
            if(sharedPreferences.getBoolean(AUODIO,true))
            {
                final MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.click);
                mediaPlayer.start();
            }
            Intent intent = new Intent(getApplicationContext(),AddMinyan.class);
            startActivity(intent);
        }
        if(view == RadarB)
        {

            if(sharedPreferences.getBoolean(AUODIO,true))
            {
                final MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.click);
                mediaPlayer.start();
            }
            Intent intent = new Intent(getApplicationContext(),Radar.class);
            startActivityForResult(intent, REQUEST_CODE);
        }
        if(view == ProfileB)
        {
            if(sharedPreferences.getBoolean(AUODIO,true))
            {
                final MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.click);
                mediaPlayer.start();
            }
            Intent intent = new Intent(getApplicationContext(),Profil.class);
            startActivityForResult(intent,REQUEST_CODE);
        }
    }

    public void setAlarm(int hours, int minutes, String title, String notes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, 0);
        Log.d("log", hours +" : " + minutes);

        long alarmTimeInMillis = calendar.getTimeInMillis();
        int notificationId = (int) System.currentTimeMillis();

        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), MyReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("notes", notes);
        intent.putExtra("id", notificationId);

        //saveAlarmsID.add(notificationId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), notificationId, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTimeInMillis, pendingIntent);
    }

}

