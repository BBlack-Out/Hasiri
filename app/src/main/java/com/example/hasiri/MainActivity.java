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
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Icon;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

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
    ImageButton AddMinyanB,SearchB,RadarB;

    FusedLocationProviderClient fusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        getSupportActionBar().hide();

        mapView = findViewById(R.id.map_view);

        checkPremission();
        mapView.getMapAsync(this);

        mapView.onCreate(savedInstanceState);

        AddMinyanB = findViewById(R.id.AddMinyan);
        RadarB = findViewById(R.id.ClosestMinyan);

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


        mDatabase = FirebaseDatabase.getInstance().getReference();
      //  mDatabase.child("Minyan").child("0").setValue(p);
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
        boolean b = false;
        mMap = googleMap;



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

                Marker marker = mMap.addMarker(new MarkerOptions().position(minyan).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)).title(addres));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
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
        Toast.makeText(MainActivity.this, "Map Loaded", Toast.LENGTH_SHORT).show();


        try {
            LatLng latLng = getIntent().getParcelableExtra("latLng");
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20f));
            b = true;
        }
        catch (Exception e){}



        if(isGPSon() && !b){
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
            if(isGPSon() && !b){
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

        String id = marker.getId();
        id = id.substring(1);
        Toast.makeText(this, id + "" ,Toast.LENGTH_SHORT).show();

      //  MinyanFrag.getView().setVisibility(View.VISIBLE);
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


                    double latFire = (double) task.getResult().child("lat").getValue();
                    double lagFire = (double) task.getResult().child("lag").getValue();
                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    List <Address> addresses;
                    try {
                        addresses = geocoder.getFromLocation(latFire ,lagFire, 1);
                        address.setText(addresses.get(0).getAddressLine(0));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String hearaFire = (String) task.getResult().child("heara").getValue();
                    heara.setText(hearaFire);

                    signTo.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                         long signupFire = (Long) task.getResult().child("signUps").getValue();
                         signupFire += 1;

                         task.getResult().getRef().child("signUps").setValue((Long) task.getResult().child("signUps").getValue() + 1);

                         Signs.setText("נרשמו: 10/ " + signupFire);
                         signTo.setEnabled(false);
                         signTo.setBackgroundColor(Color.parseColor("#22f57e"));
                         signTo.setText("נרשמת למניין");
                       }
                    });

                    dialog.show();

                }
            }
        });
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void OnClick(View view)
    {
        if(view == AddMinyanB)
        {
            Intent intent = new Intent(getApplicationContext(),AddMinyan.class);
            startActivity(intent);
        }
        if(view == RadarB)
        {
            Intent intent = new Intent(getApplicationContext(),Radar.class);
            startActivityForResult(intent, REQUEST_CODE);
        }
    }
}

