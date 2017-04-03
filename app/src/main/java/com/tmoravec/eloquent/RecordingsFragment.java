package com.tmoravec.eloquent;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.apptentive.android.sdk.Apptentive;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import nl.changer.audiowife.AudioWife;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecordingsFragment extends Fragment {

    private static final String TAG = "RecordingsFragment";

    public RecordingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recordings, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        MainActivity activity = (MainActivity) getActivity();
        activity.toggleUpButton(true);
        activity.setTitle(activity.getString(R.string.title_recordings));
        activity.logEvent("recordings_displayed");

        Records records = new Records(activity);
        if (0 < records.getSize()) {
            TextView no_recordings = (TextView) activity.findViewById(R.id.recording_no_recordings);
            no_recordings.setVisibility(View.GONE);

            RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recordings_recycler_view);
            recyclerView.setVisibility(View.VISIBLE);

            displayRecordings();
        }
    }

    private void displayRecordings() {
        MainActivity activity = (MainActivity) getActivity();
        if (null == activity) {
            return;
        }

        RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recordings_recycler_view);
        if (null == recyclerView) {
            // If we didn't find tips_list_recycler_view, a different fragment is displayed.
            // This can happen on rotate in a different fragment.
            return;
        }

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(new RecordingsAdapter(activity));
    }

    private static class RecordingsAdapter extends RecyclerView.Adapter<RecordingsAdapter.ViewHolder> {

        MainActivity mActivity;
        Records mRecords;
        ArrayList<Integer> mAllRecords;

        private static View sSelectedView;
        private static int sSelectedPosition;
        private static ActionMode sActionMode;

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
            // This View holds other views in the layout. We can call findViewById on it.
            private View mView;


            public ViewHolder(View view) {
                super(view);
                mView = view;
                mView.setOnClickListener(this);
                mView.setOnLongClickListener(this);
            }

            public View getView() {
                return mView;
            }

            @Override
            public void onClick(View view) {
                Log.i(TAG, "Clicked tip " + String.valueOf(getLayoutPosition()));
                int position = getLayoutPosition();
                int recordedOn = mAllRecords.get(position);
                Record record = mRecords.getRecord(recordedOn);

                //startPlayer(record.mFileName);

                Bundle args = new Bundle();
                Log.i(TAG, "recordedOn: " + recordedOn);
                args.putLong("recordedOn", recordedOn);
                mActivity.startFragment(new RecordingDetailsFragment(), args);
            }

            @Override
            public boolean onLongClick(View view) {
                int position = getLayoutPosition();
                Log.i(TAG, "Long clicked tip " + String.valueOf(position));

                if (null != sSelectedView) {
                    sSelectedView.setSelected(false);
                }
                view.setSelected(true);
                sSelectedView = view;
                sSelectedPosition = position;

                if (null == sActionMode) {
                    showActionMode(position);
                }

                return true;
            }

            private void startPlayer(String fileName) {
                if (null == fileName || fileName.equals("")) {
                    return;
                }
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                File file = new File(fileName);
                intent.setDataAndType(Uri.fromFile(file), "audio/*");
                if (intent.resolveActivity(mActivity.getPackageManager()) != null) {
                    mActivity.startActivity(intent);
                } else {

                    startAudioPlayer(fileName);
                }
            }

            private void startAudioPlayer(String fileName) {
                Bundle args = new Bundle();
                args.putString("fileName", fileName);

                AudioPlayerDialog dialog = new AudioPlayerDialog();
                dialog.setArguments(args);

                dialog.show(mActivity.getSupportFragmentManager(), null);
            }

            private void showActionMode(int position) {
                sActionMode = mActivity.startActionMode(new ActionMode.Callback() {
                    @Override
                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        //mode.setTitle(sSelectedItemText);

                        MenuInflater inflater = mode.getMenuInflater();
                        inflater.inflate(R.menu.actionmode_manage_recordings, menu);
                        return true;
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        return false;
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.manage_topics_action_delete:
                                deleteClicked();
                                return true;
                            default:
                                onHideActionMode();
                                return false;
                        }
                    }

                    @Override
                    public void onDestroyActionMode(ActionMode mode) {
                        onHideActionMode();
                    }
                });
            }

            private void deleteClicked() {
                Log.i(TAG, "Delete clicked. position: " + String.valueOf(sSelectedPosition));

                int recordedOn = mAllRecords.get(sSelectedPosition);
                Record record = mRecords.getRecord(recordedOn);

                Records records = new Records(mActivity);
                records.deleteRecord(record);

                File file = new File(record.mFileName);
                boolean result = file.delete();
                if (result) {
                    Log.w(TAG, "deleteClicked: Successfully deleted file " + record.mFileName);
                } else {
                    Log.e(TAG, "deleteClicked: Failed to delete file " + record.mFileName);
                }

                refresh();
            }

            private void onHideActionMode() {
                if (null != sSelectedView) {
                    sSelectedView.setSelected(false);
                }

                if (null != sActionMode) {
                    sActionMode = null;
                }
            }
        }

        public RecordingsAdapter(MainActivity activity) {
            mActivity = activity;
            refresh();
        }

        public void refresh() {
            mRecords = new Records(mActivity);
            mAllRecords = mRecords.getRecordsList();
            ArrayList<Integer> allRecordsSanitized = new ArrayList<>();

            // Sanity checks if the records are OK
            for (int recordedOn : mAllRecords) {
                Record record = mRecords.getRecord(recordedOn);

                if (null == record.mFileName || record.mFileName.equals("")) {
                    continue;
                }

                if (null == record.mTopic || record.mTopic.equals("")) {
                    continue;
                }

                allRecordsSanitized.add(recordedOn);
            }

            mAllRecords = allRecordsSanitized;

            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_recordings, parent, false);

            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            int recordedOn = mAllRecords.get(position);
            Record record = mRecords.getRecord(recordedOn);
            Log.i(TAG, record.mTopic + ", " + record.mFileName + ", " + String.valueOf(record.mImpromptu) +
                    ", " + String.valueOf(record.mRecordedOn) + ", " + String.valueOf(record.mDuration));

            String topic = record.mTopic;
            TextView topicTV  = (TextView) holder.getView().findViewById(R.id.recording_topic);
            topicTV.setText(topic);

            // TODO put this in a method, fix 1 minuteS.
            String duration = String.valueOf(record.mDuration) + " " + mActivity.getString(R.string.minutes);
            TextView durationTV = (TextView) holder.getView().findViewById(R.id.recording_length);
            durationTV.setText(duration);

            // TODO put this in a method. Format according to locale.
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(record.mRecordedOn * 1000);
            String recordedOnString = DateFormat.format("dd-MM-yyyy hh:mm", cal).toString();
            TextView recordedOnTV = (TextView) holder.getView().findViewById(R.id.recording_recorded_on);
            recordedOnTV.setText(recordedOnString);
        }

        @Override
        public int getItemCount() {
            return mAllRecords.size();
        }
    }

    public static class AudioPlayerDialog extends DialogFragment {

        private ImageView mPlay;
        private ImageView mPause;
        private SeekBar mSeekBar;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            Bundle args = getArguments();
            String fileName = args.getString("fileName");

            Activity activity = getActivity();
            if (null == activity) {
                return null;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = activity.getLayoutInflater();

            View dialogView = inflater.inflate(R.layout.dialog_audio_player, null);
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();

            Uri uri = Uri.fromFile(new File(fileName));

            mPlay = (ImageView) dialogView.findViewById(R.id.audio_player_play);
            mPause = (ImageView) dialogView.findViewById(R.id.audio_player_pause);
            mSeekBar = (SeekBar) dialogView.findViewById(R.id.audio_player_media_seekbar);

            AudioWife.getInstance().init(activity, uri)
                    .setPlayView(mPlay)
                    .setPauseView(mPause)
                    .setSeekBar(mSeekBar);

            AudioWife.getInstance().play();

            return dialog;
        }

        @Override
        public void onDismiss(final DialogInterface dialog) {
            super.onDismiss(dialog);

            AudioWife.getInstance().release();
        }
    }
}
