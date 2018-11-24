package edu.byui.team06.proxialert.view.maps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationSearch = (EditText) findViewById(R.id.editText);

    }

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
    }

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
        String title = latLng.latitude + ", " + latLng.longitude;
        // Define marker options
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title);
        if ( mMap!=null ) {
            // Remove last searchMarker
            if (searchMarker != null)
                searchMarker.remove();

            searchMarker = mMap.addMarker(markerOptions);

        }
    }

    public void onMapSearch(View view) {

        EditText locationSearch = findViewById(R.id.editText);
        location = locationSearch.getText().toString();
        List<Address> addressList = null;

        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(MapsActivity.this, "Invalid Location/Address. Please Try Again.", Toast.LENGTH_SHORT).show();
            }
            Address address = addressList.get(0);
            latlng = new LatLng(address.getLatitude(), address.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latlng).title("Marker"));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
            setSearchMarker(latlng);
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
        }
    }

    public void onMapSubmit(View view) {
        Intent intent = new Intent();
        if(location != null && location.length() > 0) {
            intent.putExtra("ADDRESS", location);
        }
        else {
            intent.putExtra("ADDRESS", latlng.toString());

        }
        intent.putExtra("COORDINATES", latlng.toString());
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onMapCancel(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    private String getMarkerAddress(LatLng latLng) {
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