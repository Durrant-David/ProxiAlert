package edu.byui.team06.proxialert.view.maps;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import edu.byui.team06.proxialert.R;
import edu.byui.team06.proxialert.utils.Permissions;



/**
 * MapsActivity it handles the activity
 * used for selecting location of the task
 */
public class MapsActivity extends FragmentActivity
        implements
        GoogleMap.OnMapClickListener,
        OnMapReadyCallback
{
    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private Marker searchMarker;
    private EditText locationSearch;
    private LatLng latlng;
    private String location;
    private Permissions permissions;
    private String taskName;

    // TODO zoom in on current location onStart
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        permissions = new Permissions();
        if ( permissions.checkMapsPermission(this) ) {

        } else {
            permissions.askMapsPermission(this);
        }
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        boolean themeName = pref.getBoolean("themes", false);
        if (themeName) {
            setTheme(R.style.ThemeOverlay_MaterialComponents_Dark);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationSearch = (EditText) findViewById(R.id.editText);
        Intent intent = getIntent();
        taskName = intent.getStringExtra("TaskName");
    }

    /**
     * onMapReady asks user for permission
     * to access for Google maps.
     * It marks user's current location
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(this);

        LocationManager lm = (LocationManager)getSystemService(LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(lm.getBestProvider(new Criteria(), true));
        LatLng myLoc = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(myLoc, 10);
        mMap.animateCamera(camera);

    }

    /**
     * MapClick sets a marker to where
     * the user clicked. Sets the text
     * of the location search bar
     * to the nearest address.
     * @param latLng
     */
    @Override
    public void onMapClick(LatLng latLng)
    {
        Log.d(TAG, "onMapClick("+latLng +")");
        setSearchMarker(latLng);
        latlng = latLng;
        if(searchMarker != null) {
            locationSearch.setText(getMarkerAddress(searchMarker.getPosition()));
            location = locationSearch.getText().toString();
        }
    }

    private void setSearchMarker(LatLng latLng) {
        Log.i(TAG, "setSearchMarker("+latLng+")");
       String title;
        if(taskName.length() > 0) {
            title = taskName;
        } else {
            title = latLng.latitude + ", " + latLng.longitude;
        }

        // Define marker options
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title);
        if ( mMap!=null ) {
            // Remove last searchMarker
            if (searchMarker != null)
                searchMarker.remove();
            mMap.clear();
            searchMarker = mMap.addMarker(markerOptions);

        }
    }

    /**
     * onMapSearch
     * When the user clicks the Search button, it gets
     * the search result and searches for the address using
     * Google database. It sets the marker at the location of that
     * address and moves the map camera to that spot.
     * @param view
     */
    public void onMapSearch(View view) {

        if (validateAddress()) {

            String title;
            if (taskName.length() > 0) {
                title = taskName;
            } else {
                title = latlng.latitude + ", " + latlng.longitude;
            }

            mMap.addMarker(new MarkerOptions().position(latlng).title(title));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
            setSearchMarker(latlng);
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
        }
    }

    private boolean validateAddress() {
        List<Address> addressList = null;

        EditText locationSearch = findViewById(R.id.editText);
        location = locationSearch.getText().toString();
        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(MapsActivity.this, "Error searching for address. Please Try Again.", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (addressList.size() == 0) {
                Toast.makeText(MapsActivity.this, "Invalid Location/Address. Please Try Again.", Toast.LENGTH_SHORT).show();
                return false;
            }

            Address address = addressList.get(0);
            latlng = new LatLng(address.getLatitude(), address.getLongitude());
            return true;
        }
        Toast.makeText(MapsActivity.this, "No location entered. Please Try Again.", Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * onMapSubmit
     * Submit button which saves the information from the
     * search bar and the coordinates to an intent that will be
     * used in the TaskActivity. It then closes the map activity.
     * @param view
     */
    public void onMapSubmit(View view) {


        if (validateAddress()) {
            Intent intent = new Intent();
            intent.putExtra("ADDRESS", location);
            intent.putExtra("COORDINATES", latlng.toString());
            setResult(RESULT_OK, intent);
            finish();
        }
    }
    /**
     * onMapCancel
     * It sets the result to cancelled and closes
     * the activity.
     * @param view
     */
    public void onMapCancel(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public String getMarkerAddress(LatLng latLng) {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String address = addresses.get(0).getAddressLine(0);

        Log.d(TAG, "Marker address = ("+address +")");
        return address;
    }


}