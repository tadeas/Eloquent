package com.tmoravec.eloquent;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.apptentive.android.sdk.Apptentive;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Locale;

import nl.changer.audiowife.AudioWife;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecordingDetailsFragment extends Fragment {

    private static final String TAG = "RecordingDetailsFragmen";

    private Record mRecord;

    public RecordingDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        if (mRecord.mUrl == null || mRecord.mUrl.equals("")) {
            menu.findItem(R.id.menu_recording_details_share).setVisible(true);
        }
        menu.findItem(R.id.menu_recording_details_delete).setVisible(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recording_details, container, false);

        // TODO: Landscape?
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        MainActivity activity = (MainActivity) getActivity();
        activity.toggleUpButton(true);
        activity.setTitle(activity.getString(R.string.title_recording));
        activity.lockOrientation();
        activity.logEvent("recording_details_displayed");

        Bundle args = getArguments();
        long recordedOn = args.getLong("recordedOn");
        Log.i(TAG, "recordedOn: " + recordedOn);
        Records records = new Records(activity);
        mRecord = records.getRecord(recordedOn);

        setupTextViews(activity);
        setupPlayer(activity);

        if (mRecord.mUrl != null && !mRecord.mUrl.equals("")) {
            displayLink(mRecord.mUrl);
        }
    }

    private void setupTextViews(Activity activity) {
        TextView topicTV = (TextView) activity.findViewById(R.id.recording_details_topic);
        topicTV.setText(mRecord.mTopic);

        String duration = String.valueOf(mRecord.mDuration) + " " + activity.getString(R.string.minutes);
        TextView durationTV = (TextView) activity.findViewById(R.id.recording_details_duration);
        durationTV.setText(duration);

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(mRecord.mRecordedOn * 1000);
        String recordedOnString = DateFormat.format("dd-MM-yyyy hh:mm", cal).toString();
        TextView recordedOnTV = (TextView) activity.findViewById(R.id.recording_details_recordedOn);
        recordedOnTV.setText(recordedOnString);
    }

    private void setupPlayer(final Activity activity) {
        ImageView play = (ImageView) activity.findViewById(R.id.recording_details_audio_player_play);
        ImageView pause = (ImageView) activity.findViewById(R.id.recording_details_audio_player_pause);
        SeekBar seekBar = (SeekBar) activity.findViewById(R.id.recording_details_audio_player_media_seekbar);

        if (null == mRecord.mFileName || mRecord.mFileName.equals("")) {
            return;
        }
        File file = new File(mRecord.mFileName);


        final Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "audio/*");
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            // We can use system audio player
            LinearLayout playerLayout = (LinearLayout) activity.findViewById(R.id.player_layout);
            playerLayout.setVisibility(View.GONE);

            ImageView systemPlayer = (ImageView) activity.findViewById(R.id.recording_details_system_player);
            systemPlayer.setVisibility(View.VISIBLE);


            systemPlayer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.startActivity(intent);
                }
            });
        } else {
            Uri uri = Uri.fromFile(file);

            AudioWife.getInstance().init(activity, uri)
                    .setPlayView(play)
                    .setPauseView(pause)
                    .setSeekBar(seekBar);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        MainActivity activity = (MainActivity) getActivity();
        if (null == activity) {
            return false;
        }

        switch (item.getItemId()) {
            case R.id.menu_recording_details_share:
                showUploadDialog();
                return true;
            case R.id.menu_recording_details_delete:
                showDeleteDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showUploadDialog() {
        MainActivity activity = (MainActivity) getActivity();
        if (null == activity) {
            return;
        }

        UploadConfirmationDialog fragment = new UploadConfirmationDialog();
        fragment.setRecordingDetailsFragment(this);
        fragment.show(activity.getSupportFragmentManager(), activity.getString(R.string.confirm_upload_title));
    }

    private void uploadToClyp() {
        MainActivity activity = (MainActivity) getActivity();
        if (null == activity) {
            return;
        }

        ProgressBar progressBar = (ProgressBar) activity.findViewById(R.id.recording_details_uploading_progressbar);
        progressBar.setVisibility(View.VISIBLE);

        ClypItClient client = new ClypItClient();
        client.upload(activity, mRecord, new ClypItClient.UploadResultCallback() {
            @Override
            public void onUpload(String url) {
                onUploadFinished(url);
            }
        });
    }

    private void onUploadFinished(String url) {
        MainActivity activity = (MainActivity) getActivity();
        if (null == activity) {
            return;
        }

        ProgressBar progressBar = (ProgressBar) activity.findViewById(R.id.recording_details_uploading_progressbar);
        progressBar.setVisibility(View.GONE);

        if (url.equals("")) {
            // failed.
            Toast toast = Toast.makeText(activity, activity.getString(R.string.recording_details_upload_failed), Toast.LENGTH_LONG);
            toast.show();

            return;
        }

        mRecord.mUrl = url;

        Records records = new Records(activity);
        records.saveRecord(mRecord);

        displayLink(url);
        activity.logEvent("upload_finished");
    }

    private void displayLink(String url) {
        final MainActivity activity = (MainActivity) getActivity();
        if (null == activity) {
            return;
        }

        TextView textView = (TextView) activity.findViewById(R.id.recording_details_link);
        textView.setText(url);
        textView.setVisibility(View.VISIBLE);
    }

    private void showDeleteDialog() {
        MainActivity activity = (MainActivity) getActivity();
        if (null == activity) {
            return;
        }

        DeleteConfirmationDialog fragment = new DeleteConfirmationDialog();
        fragment.setRecordingDetailsFragment(this);
        fragment.show(activity.getSupportFragmentManager(), activity.getString(R.string.confirm_delete_title));
    }

    private void deleteRecording() {
        Log.w(TAG, "deleteRecording: " + mRecord.mTopic);

        MainActivity activity = (MainActivity) getActivity();
        if (null == activity) {
            return;
        }

        Records records = new Records(activity);
        records.deleteRecord(mRecord);

        File file = new File(mRecord.mFileName);
        boolean result = file.delete();
        if (result) {
            Log.w(TAG, "deleteClicked: Successfully deleted file " + mRecord.mFileName);
        } else {
            Log.e(TAG, "deleteClicked: Failed to delete file " + mRecord.mFileName);
        }



        Toast toast = Toast.makeText(activity, activity.getString(R.string.recording_details_deleted), Toast.LENGTH_LONG);
        toast.show();

        activity.onBackPressed();
    }

    public static class DeleteConfirmationDialog extends DialogFragment {

        WeakReference<RecordingDetailsFragment> mRecordingDetailsFragment;

        public void setRecordingDetailsFragment(RecordingDetailsFragment recordingDetailsFragment) {
            mRecordingDetailsFragment = new WeakReference<>(recordingDetailsFragment);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.confirm_delete_title)
                    .setMessage(R.string.confirm_delete_details)
                    .setPositiveButton(R.string.confirm_delete_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            RecordingDetailsFragment recordingDetailsFragment = mRecordingDetailsFragment.get();
                            if (null != recordingDetailsFragment) {
                                recordingDetailsFragment.deleteRecording();
                            }
                        }
                    })
                    .setNegativeButton(R.string.confirm_delete_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            return builder.create();
        }
    }

    public static class UploadConfirmationDialog extends DialogFragment {
        WeakReference<RecordingDetailsFragment> mRecordingDetailsFragment;

        public void setRecordingDetailsFragment(RecordingDetailsFragment recordingDetailsFragment) {
            mRecordingDetailsFragment = new WeakReference<>(recordingDetailsFragment);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.confirm_upload_title)
                    .setMessage(R.string.confirm_upload_details)
                    .setPositiveButton(R.string.confirm_upload_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            RecordingDetailsFragment recordingDetailsFragment = mRecordingDetailsFragment.get();
                            if (null != recordingDetailsFragment) {
                                recordingDetailsFragment.uploadToClyp();
                            }
                        }
                    })
                    .setNegativeButton(R.string.confirm_upload_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            return builder.create();
        }
    }

    @Override
    public void onStop() {
        MainActivity activity = (MainActivity) getActivity();
        if (null != activity) {
            activity.unlockOrientation();
        }

        super.onStop();
    }
}
