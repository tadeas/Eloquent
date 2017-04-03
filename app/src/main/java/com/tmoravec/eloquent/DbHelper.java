package com.tmoravec.eloquent;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "Eloquent.db";

    private static final String CREATE_RECORDS = "CREATE TABLE records(_id integer primary key, recordedOn integer not null, topic text, duration integer, fileName text, impromptu integer, url text);";

    private static final String UPDATE_2 = "ALTER TABLE records ADD COLUMN url text;";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_RECORDS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (1 == oldVersion && 2 == newVersion) {
            db.execSQL(UPDATE_2);
        }
    }

    // Voodoo to make "connection object was leaked" warnings disappear.
    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }
}
