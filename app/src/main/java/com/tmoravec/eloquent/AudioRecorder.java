package com.tmoravec.eloquent;


import android.app.Activity;
import android.media.MediaRecorder;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;


// Use DI for this class. MainActivity should hold it's instance and pass it around as needed.
public class AudioRecorder {
    private static final String TAG = "AudioRecorder";

    // TODO: Make this configurable eventually.
    private static final String mDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS).getAbsolutePath()
                    + File.separator;

    private String mFullFileName;
    private MediaRecorder mRecorder = null;

    private enum State {
        READY,
        RECORDING
    }

    private State mState;

    public AudioRecorder() {
        mState = State.READY;
    }

    private String sanitizeFileName(String original) {
        return original.replaceAll("[^a-zA-Z0-9_\\.]", "_");
    }

    public void startRecording(String fileName) throws IllegalStateException {
        if (State.READY != mState) {
            Log.e(TAG, "Attempted to start recording while not in READY state.");
            throw new IllegalStateException();
        }

        mRecorder = new MediaRecorder();
        mFullFileName = mDirectory + sanitizeFileName(fileName);

        try {
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setOutputFile(mFullFileName);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            try {
                mRecorder.prepare();
            } catch (IOException e) {
                Log.e(TAG, "prepare() failed");
                return;
            }

            mState = State.RECORDING;
            mRecorder.start();
        } catch (RuntimeException e) {
            Log.e(TAG, "RuntimeException caught. Recording doesn't work in emulator");
            Log.e(TAG, e.toString());
            stopRecording();
        }
    }

    public void stopRecording() throws IllegalStateException {
        if (State.RECORDING != mState) {
            Log.e(TAG, "Attempted to stop recording while not in RECORDING state.");
            throw new IllegalStateException();
        }

        try {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;

            mState = State.READY;
        } catch (IllegalStateException e) {
            Log.e(TAG, "IllegalStateException caught. Recording doesn't work in emulator");
            Log.e(TAG, e.toString());
        }
    }

    // While not in RECORDING state, the fileName can be bogus.
    public String getFullFileName() throws IllegalStateException {
        return mFullFileName;
    }
}
