package com.tmoravec.eloquent;


import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.TextView;

public class TimerDisplay {

    private static final String TAG = "TimerDisplay";

    private CountDownTimer mTimer;
    private TextView mCountdownTV;
    private String mFinished;

    public void startTimer(int durationS, final TextView countdownTV, final String secondsRemaining,
                           final String finished) {
        mFinished = finished;

        mCountdownTV = countdownTV;

        String text = String.valueOf(durationS) + " " + secondsRemaining;
        mCountdownTV.setText(text);

        mTimer = new CountDownTimer(durationS * 1000, 1000) {
            @Override
            public void onTick(long sUntilFinished) {
                int leftSTotal = (int) sUntilFinished / 1000 + 1;

                int leftM = 0;
                int leftS = leftSTotal;
                if (leftS >= 60) {
                    leftM = leftSTotal/ 60;
                    leftS = leftSTotal % 60;
                }

                String text;
                if (leftM > 0) {
                    text = String.valueOf(leftM) + "m " + String.valueOf(leftS) + secondsRemaining;
                } else {
                    text = String.valueOf(leftSTotal) + secondsRemaining;
                }

                countdownTV.setText(text);
            }

            @Override
            public void onFinish() {
                onTimerFinished();
            }
        };
        mTimer.start();
    }

    private void onTimerFinished() {
        Log.i(TAG, "Timer finished.");
        mCountdownTV.setText(mFinished);

        // Make some noise
        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000);
    }

    public void stopTimer() {
        if (null != mTimer) {
            mTimer.cancel();
        }
    }
}
