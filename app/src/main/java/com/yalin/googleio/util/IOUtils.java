package com.yalin.googleio.util;

import com.google.common.base.Charsets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 作者：YaLin
 * 日期：2016/4/19.
 */
public class IOUtils {

    public static void writeToFile(String data, File file) throws IOException {
        writeToFile(data.getBytes(Charsets.UTF_8), file);
    }

    public static void writeToFile(byte[] data, File file) throws IOException {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            os.write(data);
            os.flush();
            os.getFD().sync();
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }
}
