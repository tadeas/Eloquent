package com.tmoravec.eloquent;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.apptentive.android.sdk.Apptentive;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainScreenFragment extends Fragment implements View.OnClickListener{


    public MainScreenFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_screen, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        MainActivity activity = (MainActivity) getActivity();
        if (null == activity) {
            return;
        }

        activity.toggleUpButton(false);
        activity.setTitle(activity.getString(R.string.app_name));
        activity.logEvent("main_screen_displayed");

        activity.findViewById(R.id.main_screen_record).setOnClickListener(this);
        activity.findViewById(R.id.main_screen_tips).setOnClickListener(this);
        activity.findViewById(R.id.main_screen_impromptu).setOnClickListener(this);
        activity.findViewById(R.id.main_screen_famous).setOnClickListener(this);
        activity.findViewById(R.id.main_screen_feedback).setOnClickListener(this);
        activity.findViewById(R.id.main_screen_rate).setOnClickListener(this);

        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
        int unreadMessagesCount = preferences.getInt("unread_messages_count", 0);
        if (unreadMessagesCount > 0) {
            //display unread messages count
            Button feedbackButton = (Button) activity.findViewById(R.id.main_screen_feedback_button);
            String text = activity.getString(R.string.main_screen_feedback_button) + " (1)";
            feedbackButton.setText(text);
        }


    }

    @Override
    public void onClick(View view) {
        MainActivity activity = (MainActivity) getActivity();
        if (null == activity) {
            return;
        }
        Fragment fragment = null;
        switch (view.getId()) {
            case R.id.main_screen_record:
                fragment = new RecordFragment();
                break;
            case R.id.main_screen_tips:
                fragment = new TipsListFragment();
                break;
            case R.id.main_screen_impromptu:
                fragment = new TopicsFragment();
                break;
            case R.id.main_screen_famous:
                fragment = new FamousSpeechesFragment();
                break;
            case R.id.main_screen_feedback:
                SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("unread_messages_count", 0);
                editor.apply();

                Button feedbackButton = (Button) activity.findViewById(R.id.main_screen_feedback_button);
                String text = activity.getString(R.string.main_screen_feedback_button);
                feedbackButton.setText(text);

                activity.logEvent("feedback_clicked");
                Apptentive.showMessageCenter(activity);
                break;
            case R.id.main_screen_rate:
                openGplay(activity);
            default:
                return;
        }
        if (null != fragment) {
            activity.startFragment(fragment, null);
        }
    }

    private void openGplay(Activity activity) {
        final Uri uri = Uri.parse("market://details?id=" + activity.getApplicationContext().getPackageName());
        final Intent rateAppIntent = new Intent(Intent.ACTION_VIEW, uri);

        if (activity.getPackageManager().queryIntentActivities(rateAppIntent, 0).size() > 0)
        {
            startActivity(rateAppIntent);
        }
    }

}
