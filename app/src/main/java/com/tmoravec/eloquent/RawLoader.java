package com.tmoravec.eloquent;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class RawLoader {
    public ByteArrayOutputStream readFile(InputStream is) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        try {
            int ctr = is.read();
            while (-1 != ctr) {
                os.write(ctr);
                ctr = is.read();
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return os;
    }
}
