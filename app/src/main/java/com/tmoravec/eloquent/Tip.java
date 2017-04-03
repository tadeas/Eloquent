package com.tmoravec.eloquent;


/*
 * Basically a c-like struct.
 */
public class Tip {
    public String mName;
    public String mDescription;

    public Tip() {
        mName = "";
        mDescription = "";
    }

    public Tip(String name, String description) {
        reset(name, description);
    }

    public void reset(String name, String description) {
        mName = name;
        mDescription = description;
    }

    public String getName() {
        return mName;
    }

    public String getDescription() {
        return mDescription;
    }
}
