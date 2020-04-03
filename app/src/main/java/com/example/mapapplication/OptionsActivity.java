package com.example.mapapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class OptionsActivity extends AppCompatActivity {

    final int defaultZoomLevel = 5;
    final double defaultLatitude = 60.20;
    final double defaultLongitude = 24.90;
    final double noGPScheckValue = 1000; //This value is used if no GPS is detected

    private double home_lat = defaultLatitude;
    private double home_lon = defaultLongitude;
    private double my_lat = noGPScheckValue;
    private double my_lon = noGPScheckValue;
    private int zoomLevel = defaultZoomLevel;
    private boolean at_home = true;
    private boolean weather_on = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        if ( savedInstanceState != null ) {
            home_lat = savedInstanceState.getDouble("home_lat", defaultLatitude);
            home_lon = savedInstanceState.getDouble("home_lon", defaultLongitude);
            my_lat = savedInstanceState.getDouble("my_lat", defaultLatitude);
            my_lon = savedInstanceState.getDouble("my_lon", defaultLongitude);
            zoomLevel = savedInstanceState.getInt("zoomLevel",defaultZoomLevel);
            at_home = savedInstanceState.getBoolean("at_home", true);
            weather_on = savedInstanceState.getBoolean("weather_on", true);
        }

        home_lat = getIntent().getDoubleExtra("home_lat", defaultLatitude);
        home_lon = getIntent().getDoubleExtra("home_lon", defaultLongitude);
        my_lat = getIntent().getDoubleExtra("my_lat", defaultLatitude);
        my_lon = getIntent().getDoubleExtra("my_lon", defaultLongitude);
        zoomLevel = getIntent().getIntExtra("zoomLevel",defaultZoomLevel);
        at_home = getIntent().getBooleanExtra("at_home", true);
        weather_on = getIntent().getBooleanExtra("weather_on", true);

        setRadioButton();
        setWeatherRadioButton();

        TextView currentLat_value = (TextView) findViewById(R.id.currentLat_value);
        currentLat_value.setText(String.format("%.2f", home_lat));

        TextView currentLon_value = (TextView) findViewById(R.id.currentLon_value);
        currentLon_value.setText(String.format("%.2f", home_lon));

        TextView gpsLat_value = (TextView) findViewById(R.id.gpsLat_textView);
        gpsLat_value.setText(String.format("%.2f", my_lat));

        TextView gpsLon_value = (TextView) findViewById(R.id.gpsLon_textView);
        gpsLon_value.setText(String.format("%.2f", my_lon));
    }

    @Override
    protected void onRestoreInstanceState( Bundle savedInstanceState ) {
        super.onRestoreInstanceState(savedInstanceState);

        if ( savedInstanceState != null ) {
            home_lat = savedInstanceState.getDouble("home_lat", defaultLatitude);
            home_lon = savedInstanceState.getDouble("home_lon", defaultLongitude);
            my_lat = savedInstanceState.getDouble("my_lat", defaultLatitude);
            my_lon = savedInstanceState.getDouble("my_lon", defaultLongitude);
            zoomLevel = savedInstanceState.getInt("zoomLevel",defaultZoomLevel);
            at_home = savedInstanceState.getBoolean("at_home", true);
            weather_on = savedInstanceState.getBoolean("weather_on", true);
        }

        setRadioButton();
        setWeatherRadioButton();

        TextView currentLat_value = (TextView) findViewById(R.id.currentLat_value);
        currentLat_value.setText(String.format("%.2f", home_lat));

        TextView currentLon_value = (TextView) findViewById(R.id.currentLon_value);
        currentLon_value.setText(String.format("%.2f", home_lon));

        TextView gpsLat_value = (TextView) findViewById(R.id.gpsLat_textView);
        gpsLat_value.setText(String.format("%.2f", my_lat));

        TextView gpsLon_value = (TextView) findViewById(R.id.gpsLon_textView);
        gpsLon_value.setText(String.format("%.2f", my_lon));
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

    //Intent back to map activity
    public void back_to_map(View view) {
        Intent openMapIntent = new Intent(this, MapsActivity.class);
        openMapIntent.putExtra("home_lat", home_lat);
        openMapIntent.putExtra("home_lon", home_lon);
        openMapIntent.putExtra("my_lat", my_lat);
        openMapIntent.putExtra("my_lon", my_lon);
        openMapIntent.putExtra("zoomLevel",zoomLevel);
        openMapIntent.putExtra("at_home", at_home);
        openMapIntent.putExtra("weather_on", weather_on);
        startActivity(openMapIntent);
    }

    //Sets new home coordinates from input
    public void set_new_home(View view) {
        EditText lat_editText = (EditText) findViewById(R.id.lat_editText);
        EditText lon_editText = (EditText) findViewById(R.id.lon_editText);

        //This portion is to check that fields are not empty
        String try_lat_text = lat_editText.getText().toString();
        String try_lon_text = lon_editText.getText().toString();

        double try_lat;
        double try_lon;

        if (try_lat_text.equals("") || try_lon_text.equals("")) {
            Toast.makeText(getApplicationContext(),"Insert values to both fields.",Toast.LENGTH_SHORT).show();
            return;
        } else {
            try_lat = Double.parseDouble(lat_editText.getText().toString());
            try_lon = Double.parseDouble(lon_editText.getText().toString());
        }

        //Rounding to one decimal place
        try_lat = Math.round(try_lat*100)/100.0;
        try_lon = Math.round(try_lon*100)/100.0;

        //Check correct input
        if (try_lat > 80 || try_lat < -80) {
            Toast.makeText(getApplicationContext(),"Latitude must be between -80 and 80.",Toast.LENGTH_SHORT).show();
        } else if (try_lon < -180 || try_lon > 180) {
            Toast.makeText(getApplicationContext(),"Longitude must be between -180 and 180.",Toast.LENGTH_SHORT).show();
        } else {
            home_lat = try_lat;
            home_lon = try_lon;

            TextView currentLat_value = (TextView) findViewById(R.id.currentLat_value);
            currentLat_value.setText(String.format("%.2f", home_lat));

            TextView currentLon_value = (TextView) findViewById(R.id.currentLon_value);
            currentLon_value.setText(String.format("%.2f", home_lon));
        }
    }

    //Radio buttons that control zoom level
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_high:
                if (checked)
                    zoomLevel = 7;
                    break;
            case R.id.radio_med:
                if (checked)
                    zoomLevel = 5;
                    break;
            case R.id.radio_low:
                if (checked)
                    zoomLevel = 3;
                    break;
        }
    }

    //Sets selected/default button for zoom level when activity starts
    public void setRadioButton() {
        if (zoomLevel == 3) {
            RadioButton rb = (RadioButton)findViewById(R.id.radio_low);
            rb.setChecked(true);
        } else if (zoomLevel == 5) {
            RadioButton rb = (RadioButton)findViewById(R.id.radio_med);
            rb.setChecked(true);
        } else if (zoomLevel == 7) {
            RadioButton rb = (RadioButton)findViewById(R.id.radio_high);
            rb.setChecked(true);
        }
    }

    //Sets home coordinates to current GPS location (if GPS)
    public void set_home_here(View view) {
        if (my_lat == noGPScheckValue || my_lon == noGPScheckValue) {
            Toast.makeText(getApplicationContext(),"No GPS was detected.",Toast.LENGTH_SHORT).show();
        } else {
            home_lat = my_lat;
            home_lon = my_lon;

            TextView currentLat_value = (TextView) findViewById(R.id.currentLat_value);
            currentLat_value.setText(String.format("%.2f", home_lat));

            TextView currentLon_value = (TextView) findViewById(R.id.currentLon_value);
            currentLon_value.setText(String.format("%.2f", home_lon));
        }
    }

    //Radio buttons to control weather on/off
    public void onWeatherRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_on:
                if (checked)
                    weather_on = true;
                break;
            case R.id.radio_off:
                if (checked)
                    weather_on = false;
                break;
        }
    }

    //Sets selected/default button for weather when activity starts
    public void setWeatherRadioButton() {
        if (weather_on) {
            RadioButton rb = (RadioButton)findViewById(R.id.radio_on);
            rb.setChecked(true);
        } else if (!weather_on) {
            RadioButton rb = (RadioButton)findViewById(R.id.radio_off);
            rb.setChecked(true);
        }
    }
}
