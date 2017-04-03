package com.tmoravec.eloquent;

import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

public class Records {

    private static final String TAG = "Records";

    private DbHelper mDbHelper;

    public Records(Context context) {
        mDbHelper = new DbHelper(context.getApplicationContext());
    }

    public int getSize() {
        String SQL = "SELECT COUNT(_id) FROM records";
        Cursor cursor = mDbHelper.getReadableDatabase().rawQuery(SQL, new String[]{});

        cursor.moveToFirst();
        int size = cursor.getInt(0);

        cursor.close();
        return size;
    }

    public ArrayList<Integer> getRecordsList() {
        String SQL = "SELECT recordedOn FROM records ORDER BY recordedOn DESC";
        Cursor cursor = mDbHelper.getReadableDatabase().rawQuery(SQL, new String[]{});

        ArrayList<Integer> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            result.add(cursor.getInt(0));
        }
        cursor.close();

        return result;
    }

    public Record getRecord(long recordedOn) {
        String SQL = "SELECT topic, duration, fileName, impromptu, recordedOn, url FROM records WHERE recordedOn = ?";
        Cursor cursor = mDbHelper.getReadableDatabase().rawQuery(SQL, new String[]{String.valueOf(recordedOn)});

        cursor.moveToFirst();
        Record record = new Record();
        try {
            record = new Record(
                    cursor.getString(0),
                    cursor.getInt(1),
                    cursor.getString(2),
                    cursor.getInt(3) != 0,
                    cursor.getInt(4),
                    cursor.getString(5)
            );
        } catch (CursorIndexOutOfBoundsException e) {
            Log.e(TAG, "CursorIndexOutOfBoundsException. " + e.toString());
        }

        cursor.close();
        return record;
    }

    public void addRecord(Record record) {
        Log.i(TAG, "addRecord: " + record.mTopic + ", " + record.mDuration + ", " + record.mFileName
                + ", " + record.mRecordedOn + ", " + record.mImpromptu + ", " + record.mUrl);
        String SQL = "INSERT INTO records(topic, duration, fileName, impromptu, recordedOn, url) VALUES(?, ?, ?, ?, ?, ?)";
        String[] args = {
                record.mTopic,
                String.valueOf(record.mDuration),
                record.mFileName,
                record.mImpromptu ? "1" : "0",
                String.valueOf(record.mRecordedOn),
                record.mUrl
        };
        mDbHelper.getWritableDatabase().execSQL(SQL, args);
    }

    public void deleteRecord(Record record) {
        Log.i(TAG, "deleteRecord: " + record.mTopic + ", " + record.mDuration + ", " + record.mFileName
                + ", " + record.mRecordedOn + ", " + record.mImpromptu + ", " + record.mUrl);
        String SQL = "DELETE FROM records WHERE recordedOn = ?";
        String[] args = {String.valueOf(record.mRecordedOn)};
        mDbHelper.getWritableDatabase().execSQL(SQL, args);

        try {
            File file = new File(record.mFileName);
            if (!file.delete()) {
                Log.e(TAG, "Failed to delete file " + record.mFileName);
            }
        } catch (NullPointerException e) {
            Log.e(TAG, e.toString());
            return;
        }
    }

    public void deleteAllRecords() {
        ArrayList<Integer> recordsList = getRecordsList();
        Record record;
        for (int recordedOn : recordsList) {
            record = getRecord(recordedOn);
            deleteRecord(record);
        }
    }

    public void saveRecord(Record record) {
        Log.i(TAG, "deleteRecord: " + record.mTopic + ", " + record.mDuration + ", " + record.mFileName
                + ", " + record.mRecordedOn + ", " + record.mImpromptu + ", " + record.mUrl);
        String SQL = "UPDATE records SET topic = ?, " +
                                        "duration = ?, " +
                                        "fileName = ?, " +
                                        "impromptu = ?, " +
                                        "url = ?" +
                     "WHERE recordedOn = ?";
        String[] args = {
                record.mTopic,
                String.valueOf(record.mDuration),
                record.mFileName,
                record.mImpromptu ? "1" : "0",
                record.mUrl,
                String.valueOf(record.mRecordedOn)
        };
        mDbHelper.getWritableDatabase().execSQL(SQL, args);
    }
}
