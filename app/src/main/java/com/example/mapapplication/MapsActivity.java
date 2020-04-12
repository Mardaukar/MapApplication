package com.example.mapapplication;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnCameraIdleListener, GoogleMap.OnCameraMoveStartedListener, OnMapReadyCallback, LocationListener {
    RequestQueue queue;

    private LocationManager locationManager;
    private Location lastKnownLocation;
    private GoogleMap mMap;

    final int defaultZoomLevel = 5;
    final double defaultLatitude = 60.18;
    final double defaultLongitude = 24.95;
    final double noGPScheckValue = 1000;

    private double home_lat = defaultLatitude;
    private double home_lon = defaultLongitude;
    private double my_lat = noGPScheckValue;
    private double my_lon = noGPScheckValue;
    private double position_lat;
    private double position_lon;

    private int zoomLevel = defaultZoomLevel;
    private float dragZoomLevel = defaultZoomLevel;

    private boolean at_home = true;
    private boolean weather_on = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        TextView temperature_TextView = (TextView)findViewById(R.id.temperature_textView);
        TextView description_TextView = (TextView)findViewById(R.id.weather_textView);

        if ( savedInstanceState != null ) {
            home_lat = savedInstanceState.getDouble("home_lat", defaultLatitude);
            home_lon = savedInstanceState.getDouble("home_lon", defaultLongitude);
            my_lat = savedInstanceState.getDouble("my_lat", noGPScheckValue);
            my_lon = savedInstanceState.getDouble("my_lon", noGPScheckValue);
            zoomLevel = savedInstanceState.getInt("zoomLevel",defaultZoomLevel);
            at_home = savedInstanceState.getBoolean("at_home", true);
            weather_on = savedInstanceState.getBoolean("weather_on", true);
        }

        home_lat = getIntent().getDoubleExtra("home_lat", defaultLatitude);
        home_lon = getIntent().getDoubleExtra("home_lon", defaultLongitude);
        my_lat = getIntent().getDoubleExtra("my_lat", noGPScheckValue);
        my_lon = getIntent().getDoubleExtra("my_lon", noGPScheckValue);
        zoomLevel = getIntent().getIntExtra("zoomLevel",defaultZoomLevel);
        dragZoomLevel = getIntent().getIntExtra("zoomLevel",defaultZoomLevel);
        at_home = getIntent().getBooleanExtra("at_home", true);
        weather_on = getIntent().getBooleanExtra("weather_on", true);

        prepareActivity();

        //Set visibility of weather
        if (weather_on) {
            temperature_TextView.setVisibility(View.VISIBLE);
            description_TextView.setVisibility(View.VISIBLE);
        } else {
            temperature_TextView.setVisibility(View.INVISIBLE);
            description_TextView.setVisibility(View.INVISIBLE);
        }
    }

    //Common initialization for onCreate and onRestore
    public void prepareActivity() {
        queue = Volley.newRequestQueue(this);
        start_gps();

        position_lat = home_lat;
        position_lon = home_lon;

        if (at_home) {
            move_home();
        } else {
            move_my_location();
        }

        getWeatherData();
        updateSelected();
        getMap();
    }
    public void openInfoDialog(View view){
        AppInfoDialog appInfoDialog = new AppInfoDialog();
        appInfoDialog.show(getSupportFragmentManager(), "App-info");
    }

    @Override
    protected void onRestoreInstanceState( Bundle savedInstanceState ) {
        super.onRestoreInstanceState(savedInstanceState);

        TextView temperature_TextView = (TextView)findViewById(R.id.temperature_textView);
        TextView description_TextView = (TextView)findViewById(R.id.weather_textView);

        if ( savedInstanceState != null ) {
            home_lat = savedInstanceState.getDouble("home_lat", defaultLatitude);
            home_lon = savedInstanceState.getDouble("home_lon", defaultLongitude);
            my_lat = savedInstanceState.getDouble("my_lat", defaultLatitude);
            my_lon = savedInstanceState.getDouble("my_lon", defaultLongitude);
            zoomLevel = savedInstanceState.getInt("zoomLevel",defaultZoomLevel);
            at_home = savedInstanceState.getBoolean("at_home", true);
            weather_on = savedInstanceState.getBoolean("weather_on", true);
        }

        prepareActivity();

        if (weather_on) {
            temperature_TextView.setVisibility(View.VISIBLE);
            description_TextView.setVisibility(View.VISIBLE);
        } else {
            temperature_TextView.setVisibility(View.INVISIBLE);
            description_TextView.setVisibility(View.INVISIBLE);
        }
    }

    //Retrieves weather data from Open Weather Map API
    public void getWeatherData () {
        String url = "https://api.openweathermap.org/data/2.5/weather?units=metric&lat="+ position_lat + "&lon=" + position_lon + "&appid=6c433438776b5be4ac86001dc88de74d";
        StringRequest getWeatherRequest = new StringRequest(Request.Method.GET, url, response->{
            parseWeatherJsonAndUpdateUI ( response );
        }, error -> {
            Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
        });

        queue.add(getWeatherRequest);
    }

    //Parses retrieved weather data for station name, temperature and weather condition
    private void parseWeatherJsonAndUpdateUI(String response) {
        try {
            JSONObject weatherObject = new JSONObject( response );
            String placeName = weatherObject.getString("name");
            JSONObject main = weatherObject.getJSONObject("main");
            int temperature = (int)main.getDouble("temp");
            String weatherDescription =
                    weatherObject.getJSONArray("weather").getJSONObject(0).getString("main");

            //Updates UI from data
            TextView placeName_TextView = (TextView)findViewById(R.id.placeName_textView);
            placeName_TextView.setText(placeName);
            TextView temperature_TextView = (TextView)findViewById(R.id.temperature_textView);
            temperature_TextView.setText("" + temperature + " °C");
            TextView description_TextView = (TextView)findViewById(R.id.weather_textView);
            description_TextView.setText(weatherDescription);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //Manipulates the map once available
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveStartedListener(this);

        mMap.clear(); //Clears marker

        // Adds a marker in desired location and moves the camera
        LatLng position = new LatLng(position_lat, position_lon);
        mMap.addMarker(new MarkerOptions().position(position).title("Marker in position"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(dragZoomLevel));
        mMap.setMinZoomPreference(2);
        mMap.setMaxZoomPreference(11);
    }

    public void click_my_location(View view) {
        move_my_location();
    }

    //Moves to my location with GPS
    public void move_my_location() {
        if (my_lat == noGPScheckValue || my_lon == noGPScheckValue) {
            Toast.makeText(getApplicationContext(),"No GPS",Toast.LENGTH_SHORT).show();
        } else {
            position_lat = my_lat;
            position_lon = my_lon;
            at_home = false;

            getMap();
            getWeatherData();
            updateSelected();
        }
    }

    public void click_home(View view) {
        move_home();
    }

    //Moves home
    public void move_home() {
        position_lat = home_lat;
        position_lon = home_lon;
        at_home = true;

        getMap();
        getWeatherData();
        updateSelected();
    }

    // Obtains the SupportMapFragment and gets notified when the map is ready to be used
    public void getMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    //Start location manager for GPS
    public void start_gps() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Check permission for GPS
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    0);

            return;
        }

        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        //Check lastKnownLocation is not null (otherwise crashes)
        if (lastKnownLocation != null) {
            my_lat = Math.round(lastKnownLocation.getLatitude()*100)/100.0;
            my_lon = Math.round(lastKnownLocation.getLongitude()*100)/100.0;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0, this);
    }

    //Update location if changes
    @Override
    public void onLocationChanged(Location location) {
        my_lat = Math.round(location.getLatitude()*100)/100.0;
        my_lon = Math.round(location.getLongitude()*100)/100.0;
    }

    //Intent to options/settings activity
    public void open_options( View view ) {
        Intent openOptionsIntent = new Intent(this, OptionsActivity.class);
        openOptionsIntent.putExtra("home_lat", home_lat);
        openOptionsIntent.putExtra("home_lon", home_lon);
        openOptionsIntent.putExtra("my_lat", my_lat);
        openOptionsIntent.putExtra("my_lon", my_lon);
        openOptionsIntent.putExtra("zoomLevel", zoomLevel);
        openOptionsIntent.putExtra("at_home", at_home);
        openOptionsIntent.putExtra("weather_on", weather_on);
        startActivity(openOptionsIntent);
    }

    @Override
    public void onSaveInstanceState(Bundle saveInstanceState) {
        super.onSaveInstanceState(saveInstanceState);
        saveInstanceState.putDouble("home_lat", home_lat);
        saveInstanceState.putDouble("home_lon", home_lon);
        saveInstanceState.putDouble("my_lat", my_lat);
        saveInstanceState.putDouble("my_lon", my_lon);
        saveInstanceState.putInt("zoomLevel", zoomLevel);
        saveInstanceState.putBoolean("at_home", at_home);
        saveInstanceState.putBoolean("weather_on", weather_on);
    }

    //Update which button is selected: home/my location
    public void updateSelected() {
        ImageButton home_button = (ImageButton)findViewById(R.id.home_button);
        ImageButton location_button = (ImageButton)findViewById(R.id.location_button);

        if(at_home) {
            //home_button.setTextColor(getColor(R.color.blue));
            //location_button.setTextColor(getColor(R.color.black));
            home_button.setImageResource(R.drawable.home_icon_blue);
            location_button.setImageResource(R.drawable.my_location_icon);
            home_button.setBackgroundResource(R.drawable.button_chosen);
            location_button.setBackgroundResource(R.drawable.btn);
        } else {
            //home_button.setTextColor(getColor(R.color.black));
            //location_button.setTextColor(getColor(R.color.blue));
            home_button.setImageResource(R.drawable.home_icon);
            location_button.setImageResource(R.drawable.my_location_icon_red);
            home_button.setBackgroundResource(R.drawable.btn);
            location_button.setBackgroundResource(R.drawable.button_chosen);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onCameraIdle() {
        LatLng position = mMap.getCameraPosition().target;
        dragZoomLevel = mMap.getCameraPosition().zoom;

        position_lat = position.latitude;
        position_lon = position.longitude;
        mMap.clear(); //Clears marker
        mMap.addMarker(new MarkerOptions().position(position).title("Marker in position"));

        getWeatherData();
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        //If map is dragged by user, unselect home and location
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            ImageButton home_button = (ImageButton)findViewById(R.id.home_button);
            ImageButton location_button = (ImageButton)findViewById(R.id.location_button);
            location_button.setImageResource(R.drawable.my_location_icon);
            location_button.setBackgroundResource(R.drawable.btn);
            home_button.setImageResource(R.drawable.home_icon);
            home_button.setBackgroundResource(R.drawable.btn);

            at_home = true;
        }
    }
}