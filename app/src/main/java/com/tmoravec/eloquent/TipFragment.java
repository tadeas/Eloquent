package com.tmoravec.eloquent;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.TextViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class TipFragment extends Fragment {

    private static final String TAG = "TipFragment";

    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private static Tips mTips;

    public TipFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) getActivity();
        mTips = new Tips(activity);

        return inflater.inflate(R.layout.fragment_tip, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        MainActivity activity = (MainActivity) getActivity();
        activity.toggleUpButton(true);

        mPager = (ViewPager) activity.findViewById(R.id.tip_pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager());
        mPager.setAdapter(mPagerAdapter);
//        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//                MainActivity activity = (MainActivity) getActivity();
//                if (null == activity) {
//                    return;
//                }
//
//                activity.setTitle(mTips.getTip(position).mName);
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//
//            }
//        });

        Bundle args = getArguments();
        if (args == null) {
            Log.e("TipFragment", "args null");
        } else {
            int position = args.getInt("position");
            Log.i(TAG, "Setting current item " + String.valueOf(position));
            mPager.setCurrentItem(position);
        }
    }

    // TODO onBackPressed?
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.i(TAG, "ScreenSlidePagerAdapter.getItem. position: " + String.valueOf(position));
            Bundle args = new Bundle();

            args.putInt("position", position);


            TipContentFragment fragment = new TipContentFragment();
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public int getCount() {
            return mTips.size();
        }
    }


    public static class TipContentFragment extends Fragment {

        int mPosition;

        public TipContentFragment() {
            // Required empty public constructor
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mPosition = getArguments() != null ? getArguments().getInt("position") : 1;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_tip_content, container, false);
            loadTip(view);
            return view;
        }

        private void loadTip(View parentView) {
            MainActivity activity = (MainActivity) getActivity();
            if (null == activity) {
                return;
            }

            Tip tip = mTips.getTip(mPosition);
            // Can't set title here. The Fragment view is created for hidden fragments,
            // but only one is actually shown. Creating the "hidden" one MUST NOT overwrite
            // the title...
//            activity.setTitle(tip.mName);

            TextView headerTV = (TextView) parentView.findViewById(R.id.tip_header);
            headerTV.setText(tip.mName);

            TextView descriptionTV = (TextView) parentView.findViewById(R.id.tip_content);
            descriptionTV.setText(tip.mDescription);

        }
    }

}
