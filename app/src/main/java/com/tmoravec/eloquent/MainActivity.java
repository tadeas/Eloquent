package com.tmoravec.eloquent;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.module.messagecenter.UnreadMessagesListener;
import com.google.firebase.analytics.FirebaseAnalytics;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int PERMISSION_RECORD_AUDIO = 1000;
    private static final int PERMISSION_STORAGE = 1001;

    private FirebaseAnalytics mFirebaseAnalytics;
    private UnreadMessagesListener mUnreadMessagesListener;
    private int mUnreadMessagesCount;

    public MainActivity() {
        mUnreadMessagesCount = 0;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "MainActivity onCreate");
        super.onCreate(savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        setContentView(R.layout.activity_main);

        mUnreadMessagesListener = new UnreadMessagesListener() {
            @Override
            public void onUnreadMessageCountChanged(int unreadMessages) {
                SharedPreferences preferences = getPreferences(MODE_PRIVATE);
                int unreadCount = preferences.getInt("unread_messages_count", 0);
                unreadCount += unreadMessages;
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("unread_messages_count", unreadCount);
                editor.apply();
            }
        };
        Apptentive.addUnreadMessagesListener(mUnreadMessagesListener);

        if (null == savedInstanceState) {
            startFragment(new MainScreenFragment(), null, false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Apptentive.onStart(this);
    }

    public void startFragment(Fragment fragment, Bundle args) {
        startFragment(fragment, args, true);
    }

    public void startFragment(Fragment fragment, Bundle args, boolean addToBackStack) {
        fragment.setArguments(args);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_main, fragment);

        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        }

        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        if(fragmentManager.getBackStackEntryCount() != 0) {
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    public void setTitle(String title) {
        ActionBar ab = getSupportActionBar();
        if (ab == null) {
            Log.w("MainActivity", "setTitle: ActionBar null");
            return;
        }

        Log.i("MainActivity", "setTitle: " + title);
        ab.setTitle(title);
    }

    public void toggleUpButton(boolean show) {
        ActionBar ab = getSupportActionBar();
        if (ab == null) {
            Log.w("MainActivity", "toggleUpButton: ActionBar null");
            return;
        }

        if (show) {
            ab.setDisplayHomeAsUpEnabled(true);
        } else {
            ab.setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_recordings:
                startFragment(new RecordingsFragment(), null);
                return true;
            case R.id.menu_settings:
                startFragment(new SettingsFragment(), null);
                return true;
            case android.R.id.home:
                hideKeyboard();
                this.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void lockOrientation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();

        switch(rotation) {
            case Surface.ROTATION_180:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                break;
            case Surface.ROTATION_270:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                break;
            case  Surface.ROTATION_0:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case Surface.ROTATION_90:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
        }
    }

    public void unlockOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    // Request RECORD_AUDIO permission. If we are granted it, in onRequestPermissionResult method,
    // ask for WRITE_EXTERNAL_STORAGE too.
    public void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_RECORD_AUDIO);
        }
    }

    public void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_RECORD_AUDIO:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestStoragePermission();
                } else {
                    Toast toast = Toast.makeText(this, R.string.permission_audio_not_granted, Toast.LENGTH_LONG);
                    toast.show();
                }
                break;
            case PERMISSION_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // OK.
                } else {
                    Toast toast = Toast.makeText(this, R.string.permission_storage_not_granted, Toast.LENGTH_LONG);
                    toast.show();
                }
                break;
        }
    }

    public void logEvent(String id) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Apptentive.onStop(this);
    }
}
