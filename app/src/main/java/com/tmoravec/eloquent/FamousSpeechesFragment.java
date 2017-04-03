package com.tmoravec.eloquent;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.apptentive.android.sdk.Apptentive;


/**
 * A simple {@link Fragment} subclass.
 */
public class FamousSpeechesFragment extends Fragment {

    private static final String TAG = "FamousSpeechesFragment";

    public FamousSpeechesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_famous_speeches, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        MainActivity activity = (MainActivity) getActivity();
        activity.toggleUpButton(true);
        activity.setTitle(activity.getString(R.string.title_famous_speeches));
        activity.logEvent("famous_speeches_displayed");
        displaySpeeches();
    }

    private void displaySpeeches() {
        MainActivity activity = (MainActivity) getActivity();
        if (null == activity) {
            return;
        }

        RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.famous_speeches_recycler_view);
        if (null == recyclerView) {
            // If we didn't find tips_list_recycler_view, a different fragment is displayed.
            // This can happen on rotate in a different fragment.
            return;
        }

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(new FamousSpeechesAdapter(new FamousSpeeches(activity), activity));
    }

    private static class FamousSpeechesAdapter extends RecyclerView.Adapter<FamousSpeechesAdapter.ViewHolder> {

        MainActivity mActivity;
        FamousSpeeches mFamousSpeeches;

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
                Log.i(TAG, "Clicked speech " + String.valueOf(getLayoutPosition()));
                int position = getLayoutPosition();

                Bundle args = new Bundle();
                args.putInt("position", position);
                mActivity.startFragment(new FamousSpeechRecordFragment(), args);
            }
        }

        public FamousSpeechesAdapter(FamousSpeeches famousSpeeches, MainActivity activity) {
            mActivity = activity;
            mFamousSpeeches = famousSpeeches;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_famous_speeches, parent, false);

            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            FamousSpeech speech = mFamousSpeeches.getSpeech(position);

            TextView titleTV  = (TextView) holder.getView().findViewById(R.id.famous_speeches_title);
            titleTV.setText(speech.mTitle);

            TextView authorTV  = (TextView) holder.getView().findViewById(R.id.famous_speeches_author);
            authorTV.setText(speech.mAuthor);
        }

        @Override
        public int getItemCount() {
            return mFamousSpeeches.size();
        }
    }
}
