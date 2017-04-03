package com.tmoravec.eloquent;


import android.app.Activity;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.RequestBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class ClypItClient {
    private static final String TAG = "ClypItClient";
    private static final String mUploadUrl = "https://upload.clyp.it/upload";

    public String upload(Activity activity, Record record, final UploadResultCallback callback) {
        // https://www.reddit.com/r/clyp/comments/4sj4t6/using_clypit_api_from_android/

        Log.i(TAG, "uploading: " + record.mTopic + ", " + record.mDuration + ", " + record.mFileName
                + ", " + record.mRecordedOn + ", " + record.mImpromptu + ", " + record.mUrl);

        String recordUrl = "";
        File file = new File(record.mFileName);

        Ion.with(activity).load(mUploadUrl)
                .setMultipartParameter("title", record.mTopic)
                .setMultipartFile("audioFile", "application/octet-stream", file)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (null != e) {
                            e.printStackTrace();
                            Log.e(TAG, e.toString());

                            callback.onUpload("");

                        } else {
                            Log.i(TAG, result.toString());
                            String url = result.get("Url").getAsString();
                            callback.onUpload(url);
                        }
                    }
                });

        return recordUrl;
    }

    private int getContentLength(File file) {
        // Assume we won't upload 2GB files.
        return (int) file.length();
    }

    private void writeStream(File file, OutputStream ostream) {
        try {
            FileInputStream istream = new FileInputStream(file);

            byte[] buffer = new byte[1024 * 4];
            int bytesRead;
            while ((bytesRead = istream.read(buffer)) != -1)
            {
                ostream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return;
        }
    }

    private String readStream(InputStream istream) {
        try {
            int n = 0;
            char[] buffer = new char[1024 * 4];
            InputStreamReader reader = new InputStreamReader(istream, "UTF8");
            StringWriter writer = new StringWriter();

            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }

            return writer.toString();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return "";
        }
    }

    private String parseResponse(String response) {
        try {
            JSONObject responseJSON = new JSONObject(response);
            return responseJSON.getString("Url");
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            return "";
        }
    }

    public interface UploadResultCallback {
        void onUpload(String url);
    }
}
