package com.tmoravec.eloquent;


import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import java.io.File;


/**
 * A simple {@link Fragment} subclass.
 */
public class FamousSpeechRecordFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "FamousSpeechRecordF";

    FamousSpeech mSpeech;

    private enum RecordingState {
        READY,
        RECORDING
    };

    private RecordingState mRecordingState;
    private AudioRecorder mAudioRecorder;
    private boolean mSaved;
    private long mStartTime;

    private Record mRecord;

    public FamousSpeechRecordFragment() {
        // Required empty public constructor
        mAudioRecorder = new AudioRecorder();
        mRecord = new Record();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRecordingState = RecordingState.READY;
        if (null != savedInstanceState) {
            if (savedInstanceState.getBoolean("is_recording")) {
                mRecordingState = RecordingState.RECORDING;
            }
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_famous_speech_record, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        MainActivity activity = (MainActivity) getActivity();
        activity.toggleUpButton(true);

        Bundle args = getArguments();
        if (null == args) {
            // Something went wrong
            return;
        }

        FamousSpeeches speeches = new FamousSpeeches(activity);
        mSpeech = speeches.getSpeech(args.getInt("position"));
        activity.setTitle(mSpeech.mTitle);

        TextView content = (TextView) activity.findViewById(R.id.famous_speech_record_content);
        content.setText(mSpeech.mContent);

        Button recordButton = (Button) activity.findViewById(R.id.famous_speech_record_button_record);
        recordButton.setOnClickListener(this);
    }

    public void onClick(View view) {
        MainActivity activity = (MainActivity) getActivity();
        if (null == activity) {
            return;
        }

        Log.i(TAG, "Button clicked. RecordingState: " + mRecordingState);

        if (RecordingState.READY == mRecordingState) {

            try {
                recordingStateViews(activity);
                startRecording();
            } catch (NullPointerException e) {
                Log.e(TAG, e.toString());
            }
            mRecordingState = RecordingState.RECORDING;
        } else {
            // RecordingState.RECORDING == mRecordingState
            stopRecording(activity);
            saveRecording();
            activity.logEvent("famous_speech_recording_finished");
            activity.startFragment(new RecordingsFragment(), null);
        }
    }

    private void saveRecording() {
        MainActivity activity = (MainActivity) getActivity();
        if (null == activity) {
            return;
        }

        Records records = new Records(activity);
        records.addRecord(mRecord);
        mSaved = true;
    }

    private void recordingStateViews(Activity activity) {
        Button recordButton = (Button) activity.findViewById(R.id.famous_speech_record_button_record);
        recordButton.setText(R.string.button_stop_recording);
        recordButton.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(activity, R.drawable.ic_check), null, null, null);
    }

    private void lockOrientation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();

        switch(rotation) {
            case Surface.ROTATION_180:
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                break;
            case Surface.ROTATION_270:
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                break;
            case  Surface.ROTATION_0:
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case Surface.ROTATION_90:
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
        }
    }

    private void startRecording() {
        MainActivity activity = (MainActivity) getActivity();
        if (null == activity) {
            return;
        }

        mRecord.mTopic = mSpeech.mTitle;

        mRecord.mDuration = 0;

        long timestamp = System.currentTimeMillis()/1000;
        mRecord.mRecordedOn = timestamp;

        String fileName = mRecord.mTopic + "-" + mRecord.mDuration + "-" + timestamp + ".3gpp";

        try {
            mAudioRecorder.startRecording(fileName);
            lockOrientation(activity);
            mStartTime = System.currentTimeMillis();
        } catch (IllegalStateException e) {
            Log.e(TAG, e.toString());
        }
    }

    private void stopRecording(Activity activity) {
        try {
            mRecord.mFileName = mAudioRecorder.getFullFileName();
            mRecord.mDuration = (int) (System.currentTimeMillis() - mStartTime) / 1000 / 60;
            mAudioRecorder.stopRecording();
            mRecordingState = RecordingState.READY;
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } catch (IllegalStateException e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        MainActivity activity = (MainActivity) getActivity();
        if (null == activity) {
            return;
        }
        stopRecording(activity);

        // Delete the file we didn't save.
        if (!mSaved) {
            try {
                String fileName = mAudioRecorder.getFullFileName();
                if (null != fileName && !fileName.equals("")) {
                    File file = new File(fileName);
                    if (!file.delete()) {
                        Log.e(TAG, "Failed to delete file " + mAudioRecorder.getFullFileName());
                    }
                }
            } catch (IllegalStateException e) {
                // We didn't even start to record.
            }

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        MainActivity activity = (MainActivity) getActivity();
        if (null == activity) {
            return;
        }

        if (RecordingState.RECORDING == mRecordingState) {
            outState.putBoolean("is_recording", true);
        }
    }

}
