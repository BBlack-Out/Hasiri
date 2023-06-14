package com.example.hasiri;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.Vibrator;
import android.util.Log;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    public static final String TPHILA_SIGHN = "sharedPrefs";
    public static final String MOED = "moed";
    public static final String IDs = "ID";
    public static final String AUODIO = "AOUDIO";

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
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(TPHILA_SIGHN, MODE_PRIVATE);

        if(view == Back)
        {
            finish();
            if(sharedPreferences.getBoolean(AUODIO,true))
            {
                final MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.click);
                mediaPlayer.start();
            }
        }

        if(view == shaharitB)
        {
            Calendar calendar = Calendar.getInstance();
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            if(currentHour > 20 || currentHour < 12)
            {
                shaharitB.setBackgroundColor(Color.parseColor("#64dbf5"));
                minhaB.setBackgroundColor(Color.parseColor("#E6E6E6"));
                arvitB.setBackgroundColor(Color.parseColor("#E6E6E6"));
                moed = 1;

                if(hour != 0 || minute != 0)
                {
                    Toast.makeText(getApplicationContext(), "נא לבחור שעה עוד הפעם", Toast.LENGTH_SHORT).show();
                    hour = 0;
                    timeButton.setText("בחר שעה");
                    TimeButtStr = "";
                    if(sharedPreferences.getBoolean(AUODIO,true))
                    {
                        final MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.error);
                        mediaPlayer.start();
                    }
                }
            }
            else
            {
                Toast.makeText(getApplicationContext(), "אפשר ליצור מניין שחרית למחר רק החל מהשעה שמונה בערב", Toast.LENGTH_SHORT).show();
                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(100);
                if(sharedPreferences.getBoolean(AUODIO,true))
                {
                    final MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.error);
                    mediaPlayer.start();
                }
            }
        }
        if(view == minhaB)
        {
            minhaB.setBackgroundColor(Color.parseColor("#64dbf5"));
            shaharitB.setBackgroundColor(Color.parseColor("#E6E6E6"));
            arvitB.setBackgroundColor(Color.parseColor("#E6E6E6"));
            moed = 2;

            if(hour != 0 || minute != 0)
            {
                Toast.makeText(getApplicationContext(), "נא לבחור שעה עוד הפעם", Toast.LENGTH_SHORT).show();
                hour = 0;
                timeButton.setText("בחר שעה");
                TimeButtStr = "";
                if(sharedPreferences.getBoolean(AUODIO,true))
                {
                    final MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.error);
                    mediaPlayer.start();
                }
            }
        }
        if(view == arvitB)
        {
            arvitB.setBackgroundColor(Color.parseColor("#64dbf5"));
            shaharitB.setBackgroundColor(Color.parseColor("#E6E6E6"));
            minhaB.setBackgroundColor(Color.parseColor("#E6E6E6"));
            moed = 3;
            if(hour != 0 || minute != 0)
            {
                Toast.makeText(getApplicationContext(), "נא לבחור שעה עוד הפעם", Toast.LENGTH_SHORT).show();
                hour = 0;
                timeButton.setText("בחר שעה");
                TimeButtStr = "";
                if(sharedPreferences.getBoolean(AUODIO,true))
                {
                    final MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.error);
                    mediaPlayer.start();
                }
            }
        }
        if(view == tzor)
        {
            if(TimeButtStr != "" && latLng != null && moed != 0 && TimeButtStr != "00:00")
            {
                if(sharedPreferences.getString(MOED,"000").charAt(moed - 1) != '1')
                {
                    Toast.makeText(getApplicationContext(), "מניין נוצר בהצלחה", Toast.LENGTH_SHORT).show();
                    if(sharedPreferences.getBoolean(AUODIO,true))
                    {
                        final MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.beep_sign);
                        mediaPlayer.start();
                    }
                    SetInFireBace();
                }
                else
                {
                    String moedStr = "";
                    if(moed == 1)
                        moedStr = "שחרית";
                    if(moed == 2)
                        moedStr = "מנחה";
                    if(moed == 3)
                        moedStr = "ערבית";
                    Toast.makeText(getApplicationContext(),"נרשמת כבר למניין " + moedStr, Toast.LENGTH_SHORT).show();
                    if(sharedPreferences.getBoolean(AUODIO,true))
                    {
                        final MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.error);
                        mediaPlayer.start();
                    }
                }
            }
            else
            {
                if(latLng == null)
                Toast.makeText(getApplicationContext(), "יש לבחור מיקום", Toast.LENGTH_SHORT).show();

                if(TimeButtStr == "")
                    Toast.makeText(getApplicationContext(), "יש לבחור שעה", Toast.LENGTH_SHORT).show();

                if(moed == 0)
                    Toast.makeText(getApplicationContext(), "יש לבחור תפילה", Toast.LENGTH_SHORT).show();

                if(TimeButtStr == "00:00")
                    Toast.makeText(getApplicationContext(), "אי אפשר ליצור מניין ב12 בלילה", Toast.LENGTH_SHORT).show();
            }
        }
        if(view == MyLocationB)
        {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fusedLocationClient = LocationServices.getFusedLocationProviderClient(AddMinyan.this);
                            if (ActivityCompat.checkSelfPermission(AddMinyan.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AddMinyan.this
                                    , Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                                    .addOnSuccessListener(AddMinyan.this, new OnSuccessListener<Location>() {
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
                                            {
                                                Toast.makeText(getApplicationContext(), "אין אפשרות למצוא את מיקומך", Toast.LENGTH_SHORT).show();
                                                if(sharedPreferences.getBoolean(AUODIO,true))
                                                {
                                                    final MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.error);
                                                    mediaPlayer.start();
                                                }
                                            }
                                        }
                                    });
                        }
                    });

                }}).start();
        }
    }

    public  void SetInFireBace()
    {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(TPHILA_SIGHN, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference Ref = database.getReference("Minyan");
        DatabaseReference RefCounter = database.getReference("Counter");

        RefCounter.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful())
                {

                    String Count = "" + task.getResult().getValue();
                    String hearaText = heara.getText().toString();
                    String Heara = hearaText.replaceAll("\\r?\\n", " ");
                    publicPrayer = new PublicPrayer(moed,latLng.latitude,latLng.longitude,hour,minute,Heara,Count);
                    Ref.child(Count).setValue(publicPrayer);
                    Ref.child(Count).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if(task.isSuccessful()) {
                                task.getResult().getRef().child("signUps").setValue((Long) task.getResult().child("signUps").getValue() + 1);
                            }
                        }
                    });
                    int i = Integer.parseInt(Count);
                    i++;
                    RefCounter.setValue("" + i);

                    StringBuilder strB = new StringBuilder(sharedPreferences.getString(MOED, "000"));
                    strB.setCharAt(moed - 1,'1');
                    editor.putString(MOED, strB.toString());

                    editor.putString(IDs, SaveID(moed - 1 ,task.getResult().getValue().toString(),sharedPreferences.getString(IDs,"n,n,n,")));
                    editor.apply();

                    int hour = publicPrayer.getHour();
                    int minute = publicPrayer.getMinute();
                    if(minute <= 15)
                    {
                        int paar = 10 - minute;
                        minute = 60 - paar;
                        hour -= 1;
                    }
                    else
                        minute -= 10;
                    String moedStr = "";
                    if(moed == 1)
                        moedStr = "שחרית";
                    if(moed == 2)
                        moedStr = "מנחה";
                    if(moed == 3)
                        moedStr = "ערבית";

                    String minStr = "" + publicPrayer.getMinute();
                    String hourStr= "" + publicPrayer.getHour();
                    if(publicPrayer.getMinute() / 10 < 1)
                        minStr = "0" + publicPrayer.getMinute();
                    if(publicPrayer.getHour() / 10 < 1)
                        hourStr = "0" + publicPrayer.getHour();

                    setAlarm(hour ,minute ,"מניין " + moedStr, "הינך נרשמת למניין " + moedStr  + " בשעה " + minStr + " : " + hourStr + " " ,Integer.parseInt(publicPrayer.getId()));
                    setAlarm(publicPrayer.getHour() ,publicPrayer.getMinute() ,"מניין " + moedStr, "המניין " + moedStr +"שנרשמת אליו התחיל! ", Integer.parseInt(publicPrayer.getId()));

                }
            }
        });
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);

        intent.putExtra("latLng", latLng);
        intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    public void popTimePicker(View view)
    {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(TPHILA_SIGHN,MODE_PRIVATE);
        if(moed != 0)
        {
            Calendar calendar = Calendar.getInstance();
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(Calendar.MINUTE);

            //Calendar.getInstance().getTime().getMinutes()

            TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int SelectedHour, int SelectedMinute) {
               if(SelectedHour > currentHour || (SelectedHour == currentHour && SelectedMinute > currentMinute) || moed == 1)
               {
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
               else
               {
                   if(SelectedHour == 0 && SelectedMinute == 0)
                   {
                       Toast.makeText(getApplicationContext(), "אי אפשר ליצור מניין לשעה 12 בלילה", Toast.LENGTH_SHORT).show();
                   }
                   else if(SelectedHour < currentHour ||(SelectedHour == currentHour && SelectedMinute < currentMinute) &&  moed !=1)
                   {
                       Toast.makeText(getApplicationContext(), "אי אפשר לבחור שעה שכבר עברה", Toast.LENGTH_SHORT).show();
                       if(Calendar.getInstance().getTime().getHours() < 23)
                           hour = Calendar.getInstance().getTime().getHours() + 1;
                       else
                           hour = 0;
                       minute = Calendar.getInstance().getTime().getMinutes();
                       String HourStr = "" + hour;
                       if(hour / 10 < 1)
                           HourStr = "0" + hour;
                       String MinuteStr = "" + minute;
                       if(minute / 10 < 1)
                           MinuteStr = "0" + minute;
                       TimeButtStr = HourStr + " : " + MinuteStr;
                       timeButton.setText(TimeButtStr);
                   }
                    else if(SelectedHour >= 23)
                   {
                       Toast.makeText(getApplicationContext(), "אי אפשר ליצור מניין לאחרי השעה 11 בערב.", Toast.LENGTH_SHORT).show();
                   }
                   Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                   vibrator.vibrate(100);
               }
            }

        };
            int style = AlertDialog.THEME_HOLO_LIGHT;
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,style,onTimeSetListener,hour,minute,true);
            timePickerDialog.setTitle("בחר שעה");
            timePickerDialog.show();
        }
        else
        {
            Toast.makeText(getApplicationContext(), "יש לבחור מועד קודם", Toast.LENGTH_SHORT).show();
            if(sharedPreferences.getBoolean(AUODIO,true))
            {
                final MediaPlayer mediaPlayer = MediaPlayer.create(AddMinyan.this,R.raw.error);
                mediaPlayer.start();
            }
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(100);

        }

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

    public void setAlarm(int hours, int minutes, String title, String notes , int IDmoed) {
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


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(TPHILA_SIGHN,MODE_PRIVATE);
        if(sharedPreferences.getBoolean(AUODIO,true))
        {
            final MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.click);
            mediaPlayer.start();
        }
    }
}
