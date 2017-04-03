package com.tmoravec.eloquent;


import android.support.v7.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class Tips extends RawLoader {

    private ArrayList<Tip> mTips;

    public Tips(AppCompatActivity activity) {
        mTips = loadTips(activity);
    }

    public Tip getTip(int index) {
        return mTips.get(index);
    }

    public int size() {
        return mTips.size();
    }

    private ArrayList<Tip> loadTips(AppCompatActivity activity) {
        InputStream is = activity.getResources().openRawResource(R.raw.tips);
        ByteArrayOutputStream os = readFile(is);

        return parseJsonTips(os);
    }

    private ArrayList<Tip> parseJsonTips(ByteArrayOutputStream os) {
        ArrayList<Tip> resultTips = new ArrayList<>();
        try {
            JSONArray topArray = new JSONArray(os.toString());

            for (int i=0; i<topArray.length(); ++i) {
                JSONObject tipJSON = topArray.getJSONObject(i);
                Tip tip = new Tip(tipJSON.getString("name"), tipJSON.getString("description"));
                resultTips.add(tip);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return resultTips;
    }
}
