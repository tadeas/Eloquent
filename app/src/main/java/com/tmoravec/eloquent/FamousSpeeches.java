package com.tmoravec.eloquent;

import android.support.v7.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

class FamousSpeeches extends RawLoader {

    private ArrayList<FamousSpeech> mSpeeches;

    public FamousSpeeches(AppCompatActivity activity) {
        mSpeeches = loadSpeeches(activity);
    }

    public FamousSpeech getSpeech(int index) {
        return mSpeeches.get(index);
    }

    public int size() {
        return mSpeeches.size();
    }

    private ArrayList<FamousSpeech> loadSpeeches(AppCompatActivity activity) {
        InputStream is = activity.getResources().openRawResource(R.raw.famous_speeches);
        ByteArrayOutputStream os = readFile(is);

        return parseJsonSpeeches(os);
    }

    private ArrayList<FamousSpeech> parseJsonSpeeches(ByteArrayOutputStream os) {
        ArrayList<FamousSpeech> resultSpeeches = new ArrayList<>();
        try {
            JSONArray topArray = new JSONArray(os.toString());

            for (int i=0; i<topArray.length(); ++i) {
                JSONObject speechJSON = topArray.getJSONObject(i);
                FamousSpeech speech = new FamousSpeech(speechJSON.getString("title"),
                        speechJSON.getString("author"), speechJSON.getString("content"));
                resultSpeeches.add(speech);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return resultSpeeches;
    }
}