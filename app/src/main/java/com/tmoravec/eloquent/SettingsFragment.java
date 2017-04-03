package com.tmoravec.eloquent;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = "SettingsFragment";


    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }

    @Override
    public void onStart() {
        super.onStart();

        final MainActivity activity = (MainActivity) getActivity();
        activity.toggleUpButton(true);
        activity.setTitle(activity.getString(R.string.title_settings));

        Preference prefDelete = (Preference) findPreference("pref_delete_all_recordings");
        if (null == prefDelete) {
            Log.e(TAG, "Couldn't find preference pref_delete_all_recordings");
            return;
        }
        prefDelete.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

                DialogFragment fragment = new DeleteConfirmDialogFragment();
                fragment.show(activity.getSupportFragmentManager(), null);
                return true;
            }
        });
    }

    private static void deleteAllRecordings(Context context) {
        // TODO confirmation dialog
        Records records = new Records(context);
        records.deleteAllRecords();

        CharSequence text = context.getString(R.string.confirm_delete_deleted);
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

    }

    public static class DeleteConfirmDialogFragment extends DialogFragment {

        Context mContext;

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            mContext = context;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.confirm_delete_title_all)
                    .setMessage(R.string.confirm_delete_details)
                    .setPositiveButton(R.string.confirm_delete_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            deleteAllRecordings(mContext);
                        }
                    })
                    .setNegativeButton(R.string.confirm_delete_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

}
