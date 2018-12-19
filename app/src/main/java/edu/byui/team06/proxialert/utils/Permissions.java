package edu.byui.team06.proxialert.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * @author David Durrant, Chase Busacker
 * @version  1.0
 * @since
 * This class gets the maps permissions in the beginning
 * of the program
 */
public class Permissions implements  ActivityCompat.OnRequestPermissionsResultCallback {

    private final int MAP_PERMISSION = 999;
    private final int MIC_PERMISSION = 1;
    private final int CONTACT_PERMISSION = 2;
    private static final String TAG = Permissions.class.getSimpleName();
    private final Context c;

    public Permissions(Context c) {
        this.c = c;
    }
    // Check for permission to access Location
    public boolean checkMapsPermission(Context context) {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);

    }

    /**
     * askMapsPermission method asks user for permission to use location
     * @param activity
     */
    // Asks for permission
    public void askMapsPermission(Activity activity) {
        Log.d(TAG, "askMapsPermission()");
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MAP_PERMISSION
        );
        if(!checkMapsPermission(c)) {
            permissionsDenied(MAP_PERMISSION);
        }
    }

    /**
     * checkContactPermission method checks for permissions to use phone contacts
     * @param context
     * @return
     */
    public boolean checkContactPermission(Context context) {
        Log.d(TAG, "checkContactPermission()");
        return (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * askContactPermission method asks user for permissions to use phone contacts
     * @param activity
     */
    public void askContactPermission(Activity activity) {
        Log.d(TAG, "askContactsPermission()");
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.READ_CONTACTS},
                CONTACT_PERMISSION);
        if(!checkMicPermission(c)) {
            permissionsDenied(CONTACT_PERMISSION);
        }
    }

    /**
     * checkMicPermission method checks the permissions to use phone microphone
     * @param context
     * @return
     */
    public boolean checkMicPermission(Context context) {
        Log.d(TAG, "checkMicPermissions()");
        return (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * askMicPermissions asks the user for permissions to use the phone microphone.
     * @param activity
     */
    public void askMicPermission(Activity activity) {
        Log.d(TAG, "askMicPermission");
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.RECORD_AUDIO},
                MIC_PERMISSION);
        if(!checkMicPermission(c)) {
            permissionsDenied(MIC_PERMISSION);
        }

    }

    /**
     * onRequestPermissionsResult method handles all the permissions
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    // Verify user's response of the permission requested
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MAP_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    Log.i(TAG, "Location permission granted");
                } else {
                    // Permission denied
                    permissionsDenied(requestCode);
                }
                break;
            }
            case MIC_PERMISSION: {
                if (grantResults.length > 1
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Mic permission granted");
                } else {
                    permissionsDenied(requestCode);
                }
                break;
            }
            case CONTACT_PERMISSION: {
                if (grantResults.length > 1
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Mic permission granted");
                } else {
                    permissionsDenied(requestCode);
                }
                break;
            }
        }
    }


    // App cannot work without the permissions
    private void permissionsDenied(int requestCode) {
        Log.w(TAG, "permissionsDenied()");
        switch (requestCode) {
            case MAP_PERMISSION:
                Toast.makeText(c.getApplicationContext(),
                        "ERROR: App cannot function without User Location. " +
                                "Please update in Settings.",
                        Toast.LENGTH_LONG).show();
                break;
            case MIC_PERMISSION:
                Toast.makeText(c.getApplicationContext(),
                        "You will not be able to record your voice for notifications" +
                                " without App Mic Accessibility. Please update in Settings.",
                        Toast.LENGTH_LONG).show();
                break;
            case CONTACT_PERMISSION:
                Toast.makeText(c.getApplicationContext(),
                        "You will not be able to link contacts to each task without" +
                                "App Contact Accessibility. Please update in Settings.",
                        Toast.LENGTH_LONG).show();
                break;
        }

    }
}
