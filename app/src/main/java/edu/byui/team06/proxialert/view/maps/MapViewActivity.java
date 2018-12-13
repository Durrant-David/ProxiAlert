
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
        import java.lang.reflect.Array;
        import java.util.ArrayList;
        import java.util.List;
        import java.util.Locale;

        import edu.byui.team06.proxialert.R;
        import edu.byui.team06.proxialert.database.model.ProxiDB;
        import edu.byui.team06.proxialert.utils.Permissions;
        import edu.byui.team06.proxialert.database.DatabaseHelper;



/**
 * MapsActivity it handles the activity
 * used for selecting location of the task
 */
public class MapViewActivity extends FragmentActivity
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
    private DatabaseHelper db;
    private ArrayList <ProxiDB> TaskList;

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
        setContentView(R.layout.activity_task_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        db = new DatabaseHelper(this);
        TaskList.addAll(db.getAllTasks());

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

        for(ProxiDB task:TaskList){
            setSearchMarker(new LatLng(Double.parseDouble(task.getLat()), Double.parseDouble(task.getLong())));

        }



    }

    /**
     * MapClick sets a marker to where
     * the user clicked. Sets the text
     * of the location search bar
     * to the nearest address.
     * @param latLng
     */

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
     * onMapCancel
     * It sets the result to cancelled and closes
     * the activity.
     * @param view
     */
    public void onMapCancel(View view) {
        finish();
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }
}
