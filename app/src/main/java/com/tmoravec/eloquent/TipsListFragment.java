package com.tmoravec.eloquent;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.apptentive.android.sdk.Apptentive;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class TipsListFragment extends Fragment {

    private static final String TAG = "TipsListFragment";

    public TipsListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tips_list, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        MainActivity activity = (MainActivity) getActivity();
        activity.toggleUpButton(true);
        activity.setTitle(activity.getString(R.string.title_tips));
        activity.logEvent("tips_list_displayed");

        displayTips();
    }

    private void displayTips() {
        MainActivity activity = (MainActivity) getActivity();
        if (null == activity) {
            return;
        }

        RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.tips_list_recycler_view);
        if (null == recyclerView) {
            // If we didn't find tips_list_recycler_view, a different fragment is displayed.
            // This can happen on rotate in a different fragment.
            return;
        }

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(new TipsAdapter(new Tips(activity), activity));
    }

    private static class TipsAdapter extends RecyclerView.Adapter<TipsAdapter.ViewHolder> {

        MainActivity mActivity;
        Tips mTips;

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            // This View holds other views in the layout. We can call findViewById on it.
            private View mView;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mView.setOnClickListener(this);
            }

            public View getView() {
                return mView;
            }

            @Override
            public void onClick(View view) {
                Log.i(TAG, "Clicked tip " + String.valueOf(getLayoutPosition()));
                int position = getLayoutPosition();

                Bundle args = new Bundle();
                args.putInt("position", position);
                mActivity.startFragment(new TipFragment(), args);
            }
        }

        public TipsAdapter(Tips tips, MainActivity activity) {
            mActivity = activity;
            mTips = tips;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_tips_list, parent, false);

            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Tip tip = mTips.getTip(position);

            TextView topicTV  = (TextView) holder.getView().findViewById(R.id.tips_list_name);
            topicTV.setText(tip.mName);
        }

        @Override
        public int getItemCount() {
            return mTips.size();
        }
    }

}
