package com.danielvilha.javaworkmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //region Variables
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_CODE_IMAGE = 100;
    private static final int REQUEST_CODE_PERMISSIONS = 101;

    private static final String KEY_PERMISSIONS_REQUEST_COUNT = "KEY_PERMISSIONS_REQUEST_COUNT";
    private static final int MAX_NUMBER_REQUEST_PERMISSIONS = 2;

    private static final List<String> sPermissions = Arrays.asList(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    );

    private int mPermissionRequestCount;
    //endregion

    //region onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mPermissionRequestCount =
                    savedInstanceState.getInt(KEY_PERMISSIONS_REQUEST_COUNT, 0);
        }

        // Make sure the app has correct permissions to run
        requestPermissionsIfNecessary();

        // Create request to get image from filesystem when button clicked
        findViewById(R.id.selectImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chooseIntent = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                MainActivity.this.startActivityForResult(chooseIntent, REQUEST_CODE_IMAGE);
            }
        });
    }
    //endregion

    /** Save the permission request count on a rotate **/
    //region onSaveInstanceState
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_PERMISSIONS_REQUEST_COUNT, mPermissionRequestCount);
    }
    //endregion

    /**
     * Request permissions twice - if the user denies twice then show a toast about how to update
     * the permission for storage. Also disable the button if we don't have access to pictures on
     * the device.
     */
    //region requestPermissionsIfNecessary
    private void requestPermissionsIfNecessary() {
        if (!checkAllPermissions()) {
            if (mPermissionRequestCount < MAX_NUMBER_REQUEST_PERMISSIONS) {
                mPermissionRequestCount += 1;
                ActivityCompat.requestPermissions(
                        this,
                        sPermissions.toArray(new String[0]),
                        REQUEST_CODE_PERMISSIONS);
            } else {
                Toast.makeText(this, R.string.set_permissions_in_settings, Toast.LENGTH_LONG).show();
                findViewById(R.id.selectImage).setEnabled(false);
            }
        }
    }
    //endregion

    //region checkAllPermissions
    private boolean checkAllPermissions() {
        boolean hasPermissions = true;
        for (String permission : sPermissions) {
            hasPermissions &= ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
        }

        return hasPermissions;
    }
    //endregion

    /** Permission Checking **/
    //region onRequestPermissionsResult
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            requestPermissionsIfNecessary(); // no-op if permissions are granted already.
        }
    }
    //endregion

    /** Image Selection **/
    //region onActivityResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch(requestCode) {
                case REQUEST_CODE_IMAGE:
                    handleImageRequestResult(data);
                    break;
                default:
                    Log.d(TAG, "Unknown request code.");
            }
        } else {
            Log.e(TAG, String.format("Unexpected Result code %s", resultCode));
        }
    }
    //endregion

    //region handleImageRequestResult
    private void handleImageRequestResult(Intent data) {
        Uri imageUri = null;
        if (data.getClipData() != null) {
            imageUri = data.getClipData().getItemAt(0).getUri();
        } else if (data.getData() != null) {
            imageUri = data.getData();
        }

        if (imageUri == null) {
            Log.e(TAG, "Invalid input image Uri.");
            return;
        }

        Intent filterIntent = new Intent(this, BlurActivity.class);
        filterIntent.putExtra(Constants.KEY_IMAGE_URI, imageUri.toString());
        startActivity(filterIntent);
    }
    //endregion
}
