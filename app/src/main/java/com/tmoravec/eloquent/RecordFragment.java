package com.tmoravec.eloquent;


import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Space;
import android.widget.TextView;

import com.apptentive.android.sdk.Apptentive;

import java.io.File;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecordFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "RecordFragment";

    private enum RecordingState {
        READY,
        RECORDING
    };

    private RecordingState mRecordingState;
    private TimerDisplay mTimerDisplay;
    private AudioRecorder mAudioRecorder;

    private Record mRecord;
    private long mStartTime;

    private final static String[] mDurationValues = {"1", "1.5", "2", "2.5", "3", "4", "5", "7", "10", "15", "20", "25", "30", "35", "40", "45", "50", "60"};
    private int mSelectedDurationValue;

    private boolean mSaved;
    private String mTopic;

    public RecordFragment() {
        // Required empty public constructor
        mTimerDisplay = new TimerDisplay();
        mAudioRecorder = new AudioRecorder();
        mRecord = new Record();
        mRecord.mImpromptu = false;
        mSelectedDurationValue = 2;
        mSaved = false;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView. savedInstanceState=" + savedInstanceState);

        mRecordingState = RecordingState.READY;
        if (null != savedInstanceState) {
            if (savedInstanceState.getBoolean("is_recording")) {
                mRecordingState = RecordingState.RECORDING;
            }
            mSelectedDurationValue = savedInstanceState.getInt("duration_value");
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        MainActivity activity = (MainActivity) getActivity();
        activity.toggleUpButton(true);
        activity.setTitle(activity.getString(R.string.title_record));
        activity.logEvent("record_displayed");

        Bundle args = getArguments();
        if (null != args) {
            mTopic = args.getString("topic");
            EditText topicET = (EditText) activity.findViewById(R.id.record_topic_edit);
            topicET.setText(mTopic);
            mRecord.mImpromptu = true;
        }

        Button recordButton = (Button) activity.findViewById(R.id.record_button_record);
        recordButton.setOnClickListener(this);

        setupNumberPicker(activity);

        if (RecordingState.RECORDING == mRecordingState) {
            recordingStateViews(activity);
        }

        activity.requestPermissions();
    }


    private void setupNumberPicker(Activity activity) {
        NumberPicker durationPicker = (NumberPicker) activity.findViewById(R.id.record_duration_edit);

        durationPicker.setDisplayedValues(mDurationValues);
        durationPicker.setMinValue(0);
        durationPicker.setMaxValue(mDurationValues.length - 1);

        durationPicker.setValue(mSelectedDurationValue);
        durationPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mSelectedDurationValue = newVal;
            }
        });
    }

    private void recordingStateViews(MainActivity activity) {
        activity.hideKeyboard();

        TextView recording = (TextView) activity.findViewById(R.id.record_recording);
        recording.setVisibility(View.VISIBLE);

        TextView timeLeft = (TextView) activity.findViewById(R.id.record_time_left);
        timeLeft.setVisibility(View.VISIBLE);

        Button recordButton = (Button) activity.findViewById(R.id.record_button_record);
        recordButton.setText(R.string.button_stop_recording);
        recordButton.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(activity, R.drawable.ic_check), null, null, null);
    }

    @Override
    public void onClick(View view) {
        MainActivity activity = (MainActivity) getActivity();
        if (null == activity) {
            return;
        }

        Log.i(TAG, "Button clicked. RecordingState: " + mRecordingState);
        if (RecordingState.READY == mRecordingState) {

            try {
                recordingStateViews(activity);
                startTimer();
                startRecording();
            } catch (NullPointerException e) {
                Log.e(TAG, e.toString());
            }
            mRecordingState = RecordingState.RECORDING;
        } else {
            // RecordingState.RECORDING == mRecordingState
            stopTimer();
            stopRecording(activity);
            saveRecording();

            if (mRecord.mImpromptu == true) {
                activity.logEvent("impromptu_recording_finished");
            }
            activity.logEvent("recording_finished");
            activity.startFragment(new RecordingsFragment(), null);
        }
    }

    private void startTimer() {
        MainActivity activity = (MainActivity) getActivity();
        if (null == activity) {
            return;
        }

        NumberPicker durationPicker = (NumberPicker) activity.findViewById(R.id.record_duration_edit);
        String durationMin = mDurationValues[durationPicker.getValue()];
        int durationS = (int) (Float.parseFloat(durationMin) * 60);

        TextView countdownTV = (TextView) activity.findViewById(R.id.record_time_left);

        mTimerDisplay.startTimer(durationS, countdownTV, getString(R.string.seconds_remaining),
                getString(R.string.finished));
    }

    private void stopTimer() {
        if (null != mTimerDisplay) {
            mTimerDisplay.stopTimer();
        }
    }

    private String getTopic() {
        MainActivity activity = (MainActivity) getActivity();
        if (null == activity) {
            return "";
        }

        EditText topicTV = (EditText) activity.findViewById(R.id.record_topic_edit);
        String topic = (String) topicTV.getText().toString();
        if ("".equals(topic)) {
            topic = activity.getString(R.string.record_default_name);
        }
        topicTV.setInputType(InputType.TYPE_NULL);

        return topic;
    }



    private void startRecording() {
        MainActivity activity = (MainActivity) getActivity();
        if (null == activity) {
            return;
        }

        mRecord.mTopic = getTopic();

        NumberPicker durationPicker = (NumberPicker) activity.findViewById(R.id.record_duration_edit);
        int duration = durationPicker.getValue();

        long timestamp = System.currentTimeMillis()/1000;
        mRecord.mRecordedOn = timestamp;
        mStartTime = System.currentTimeMillis();

        String fileName = mRecord.mTopic + "-" + duration + "-" + timestamp + ".3gpp";

        try {
            mAudioRecorder.startRecording(fileName);
            activity.lockOrientation();
        } catch (IllegalStateException e) {
            Log.e(TAG, e.toString());
        }
    }

    private void stopRecording(MainActivity activity) {
        try {
            mRecord.mFileName = mAudioRecorder.getFullFileName();
            mRecord.mDuration = (int) (System.currentTimeMillis() - mStartTime) / 1000 / 60;
            mAudioRecorder.stopRecording();
            mRecordingState = RecordingState.READY;
            activity.unlockOrientation();
        } catch (IllegalStateException e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopTimer();

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

    private void saveRecording() {
        MainActivity activity = (MainActivity) getActivity();
        if (null == activity) {
            return;
        }

        Records records = new Records(activity);
        records.addRecord(mRecord);
        mSaved = true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        MainActivity activity = (MainActivity) getActivity();
        if (null == activity) {
            return;
        }

        outState.putInt("duration_value", mSelectedDurationValue);

        if (RecordingState.RECORDING == mRecordingState) {
            outState.putBoolean("is_recording", true);
        }
    }

}
