package com.tmoravec.eloquent;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.apptentive.android.sdk.Apptentive;

import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 */
public class TopicsFragment extends Fragment {


    private static final String TAG = "RecordFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_topics, container, false);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menu.findItem(R.id.menu_topics_random).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        MainActivity activity = (MainActivity) getActivity();
        if (null == activity) {
            return false;
        }

        switch (item.getItemId()) {
            case R.id.menu_topics_random:
                selectRandom(activity);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        MainActivity activity = (MainActivity) getActivity();
        activity.toggleUpButton(true);
        activity.setTitle(activity.getString(R.string.title_topics));
        activity.logEvent("topics_displayed");

        displayTopics();
    }

    private void displayTopics() {
        MainActivity activity = (MainActivity) getActivity();
        if (null == activity) {
            return;
        }

        RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.topics_recycler_view);
        if (null == recyclerView) {
            // If we didn't find topics_recycler_view, a different fragment is displayed.
            // This can happen on rotate in a different fragment.
            return;
        }

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(new TopicsAdapter(new Topics(activity), activity));
    }

    private void selectRandom(MainActivity activity) {
        Topics topics = new Topics(activity);
        Random r = new Random();
        int index = r.nextInt(topics.getSize());

        String topic = topics.getTopic(index);
        Bundle args = new Bundle();
        args.putString("topic", topic);
        activity.startFragment(new RecordFragment(), args);
    }

    private static class TopicsAdapter extends RecyclerView.Adapter<TopicsAdapter.ViewHolder> {

        MainActivity mActivity;
        Topics mTopics;

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
                Log.i(TAG, "Clicked topic " + String.valueOf(getLayoutPosition()));
                int position = getLayoutPosition();
                String topic = mTopics.getTopic(position);

                Bundle args = new Bundle();
                args.putString("topic", topic);
                mActivity.startFragment(new RecordFragment(), args);
            }
        }

        public TopicsAdapter(Topics topics, MainActivity activity) {
            mActivity = activity;
            mTopics = topics;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_topics_list, parent, false);

            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String topic = mTopics.getTopic(position);

            TextView topicTV  = (TextView) holder.getView().findViewById(R.id.topics_list_name);
            topicTV.setText(topic);
        }

        @Override
        public int getItemCount() {
            return mTopics.getSize();
        }


    }
}