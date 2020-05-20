package com.example.mynewgooglemaps;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{

    private boolean mLocationPermissionGranted;

    private static final int PERMISSION_REQUEST_CODE = 9001;
    private static final int PLAY_SERVICES_ERROR_CODE = 9002;
    private static final int GPS_REQUEST_CODE = 9003 ;

    private GoogleMap mGoogleMap;
    private FusedLocationProviderClient mLocationClient;
    private LocationCallback mLocationCallback;

    Polyline polyline = null;
    final PolylineOptions polylineOptions = new PolylineOptions();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Button mCr_Btn = findViewById(R.id.current_location);
        mCr_Btn.setOnClickListener(this::getLocationUpdates);

        Button mLocateTrails = findViewById(R.id.mapLocationTrail);
        mLocateTrails.setOnClickListener(this::mapCSVLocationTrail);

        initGoogleMap();
        mLocationClient = new FusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...


                    Toast.makeText(MainActivity.this, location.getLatitude() + "\n"+
                            location.getLongitude(), Toast.LENGTH_SHORT).show();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String timeStamp  = dateFormat.format(new Date());
                    //gotoLocation(location.getLatitude(),location.getLongitude());
                    Log.d("MAP_DEBUG","onLocation Result: location is : "+ location.getLatitude() + "\n"+
                            location.getLongitude());
                    showMarker(location.getLatitude(),location.getLongitude(), timeStamp);
                    //adding new polyline
                    polylineOptions.add(new LatLng(location.getLatitude(),location.getLongitude()));
                    polyline = mGoogleMap.addPolyline(polylineOptions);
                    polyline.setColor(Color.rgb(0,0,243));

                    writeCSVLocationTrail(location,timeStamp);

                }
            }
        };

        Button scan = findViewById(R.id.discover);
        scan.setOnClickListener(v -> openBTActivity());

    }

    private void openBTActivity() {
        Intent intent = new Intent(this,BTActivity.class);
        startActivity(intent);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

    }


    private void initGoogleMap() {
        if(isServicesOk()){
            if(isGPSEnabled()){
                if(checkLocationPermission() && checkReadWriteExternalPermission()){
                    Toast.makeText(this, "Ready to Map", Toast.LENGTH_SHORT).show();

                    SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map_fragment_container);

                    supportMapFragment.getMapAsync(this);
                }else{
                    requestLocationPermission();
                    requestReadWritePermission();
                }

            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.maptype_none: {
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            }
            case R.id.maptype_normal: {
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            }
            case R.id.maptype_satellite: {
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            }
            case R.id.maptype_terrain: {
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            }
            case R.id.maptype_hybrid: {
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            }case R.id.current_location: {
                getCurrentLocation();
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void getCurrentLocation() {
        mLocationClient.getLastLocation().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Location location = task.getResult();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timeStamp  = dateFormat.format(new Date());
                showMarker(location.getLatitude(),location.getLongitude(),timeStamp);
                gotoLocation(location.getLatitude(),location.getLongitude());
            }else{
                Log.d("MAP_DEBUG","getCurrentLocation: Error: "+ task.getException().getMessage());
            }
        });
    }

    public void gotoLocation(double lat,double lng){

        LatLng latLng = new LatLng(lat,lng);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,15);
        //showMarker(latLng);

        mGoogleMap.moveCamera(cameraUpdate);

    }

    private void showMarker(LatLng latLng, String timeStamp) {

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng).title("TimeStamp: "+timeStamp);
        mGoogleMap.addMarker(markerOptions);
    }

    private void showMarker(double lat, double lng, String timeStamp) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(lat, lng)).title("TimeStamp: "+timeStamp);
        mGoogleMap.addMarker(markerOptions);
        gotoLocation(lat,lng);
    }

    private void getLocationUpdates(View view){

        LocationRequest locationRequest = LocationRequest.create();

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(40000);

        //locationRequest.setSmallestDisplacement(1);
//        if(locationRequest.getSmallestDisplacement() == 1){
//            Toast.makeText(this, "5m travelled", Toast.LENGTH_SHORT).show();
//        }

        //locationRequest.setFastestInterval(2000);
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            return;
        }

        mLocationClient.requestLocationUpdates(locationRequest,mLocationCallback,null);
    }


    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkReadWriteExternalPermission() {
        return ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED));
    }

    private boolean isServicesOk() {

        GoogleApiAvailability googleApi = GoogleApiAvailability.getInstance();
        int result = googleApi.isGooglePlayServicesAvailable(this);

        if(result == ConnectionResult.SUCCESS){
            return true;
        }else if(googleApi.isUserResolvableError(result)){
            Dialog dialog = googleApi.getErrorDialog(this,result,PLAY_SERVICES_ERROR_CODE, task->
                    Toast.makeText(this,"Dialog is cancelled by User",Toast.LENGTH_SHORT).show());
            dialog.show();
        }else{
            Toast.makeText(this, "Play Services are required by this application", Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    private void requestLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void requestReadWritePermission() {
        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
            mLocationPermissionGranted = true;
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this,"Permission not granted",Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isGPSEnabled(){

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        boolean providerEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if(providerEnabled){
            return true;
        } else{
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("GPS Permissions")
                    .setMessage("GPS is required for this app to work. Please enable GPS")
                    .setPositiveButton("Yes",((dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent,GPS_REQUEST_CODE);
                    }))
                    .setCancelable(false)
                    .show();
        }

        return false;
    }

    private void writeCSVLocationTrail(Location location,String timeStamp){
        try {
            String Latitude = String.valueOf(location.getLatitude());
            String Longitude = String.valueOf(location.getLongitude());

            File file = new File("/sdcard/MyFolder/");
            file.mkdirs();

            String csv = "/sdcard/MyFolder/locationTrail.csv";
            CSVWriter csvWriter = new CSVWriter(new FileWriter(csv,true));

            String row[] = new String[]{Latitude,Longitude,timeStamp};
            csvWriter.writeNext(row);
            csvWriter.close();

            //Toast.makeText(MainActivity.this, "File Successfully Created", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mapCSVLocationTrail(View view) {

        try {

            String csv ="/sdcard/MyFolder/locationTrail.csv";
            CSVReader csvReader = new CSVReader(new FileReader(csv));

            String[] nextLine;

            LatLng latLng;

            while((nextLine=csvReader.readNext())!=null){
                latLng = new LatLng(Double.parseDouble(nextLine[0]),Double.parseDouble(nextLine[1]));
                showMarker(latLng,nextLine[2]);
                gotoLocation(latLng.latitude,latLng.longitude);

                //adding new polyline
                polylineOptions.add(latLng);
                polyline = mGoogleMap.addPolyline(polylineOptions);
                polyline.setColor(Color.rgb(0,0,243));
                //Toast.makeText(this, nextLine[0], Toast.LENGTH_LONG).show();
            }
        } catch (Exception e){
            e.printStackTrace();
        }


    }

}
