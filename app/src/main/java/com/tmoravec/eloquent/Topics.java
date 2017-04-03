package com.tmoravec.eloquent;



import android.support.v7.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Topics extends RawLoader {

    private ArrayList<String> mTopics;

    public Topics(AppCompatActivity activity) {
        mTopics = loadTopics(activity);
    }

    public String getTopic(int index) {
        return mTopics.get(index);
    }

    public ArrayList<String> getAllTopics() {
        return mTopics;
    }

    public int getSize() {
        return mTopics.size();
    }

    private ArrayList<String> loadTopics(AppCompatActivity activity) {
        InputStream is = activity.getResources().openRawResource(R.raw.topics);
        ByteArrayOutputStream os = readFile(is);

        return parseJsonTopics(os);
    }



    private ArrayList<String> parseJsonTopics(ByteArrayOutputStream os) {
        ArrayList<String> resultTopics = new ArrayList<>();

        try {
            JSONArray topArray = new JSONArray(os.toString());

            for (int i=0; i<topArray.length(); ++i) {
                resultTopics.add(topArray.getString(i));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return resultTopics;
    }
}
