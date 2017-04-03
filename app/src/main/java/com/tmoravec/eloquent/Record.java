package com.tmoravec.eloquent;


/*
 * Basically a c-like struct.
 */
public class Record {
    public String mTopic;
    public String mFileName;
    public boolean mImpromptu;
    public long mRecordedOn;
    public int mDuration;
    public String mUrl;

    public Record() {

    }

    public Record(String topic, int duration, String fileName, boolean impromptu, long recordedOn, String url) {
        reset(topic, duration, fileName, impromptu, recordedOn, url);
    }

    public void reset(String topic, int duration, String fileName, boolean impromptu, long recordedOn, String url) {
        mTopic = topic;
        mDuration = duration;
        mFileName = fileName;
        mImpromptu = impromptu;
        mRecordedOn = recordedOn;
        mUrl = url;
    }
}
