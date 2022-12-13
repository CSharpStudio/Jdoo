package org.jdoo.utils;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipTest {
    @Test
    public void Run() {
        Map<String, String> data = new HashMap<>();
        data.put("models/Model.java", "public class Model { }");
        data.put("views/model_view.xml", "<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        ZipOutputStream zos = null;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("d:/test.zip");
            zos = new ZipOutputStream(fileOutputStream);
            byte[] buf = new byte[1024];
            for (Entry<String, String> entry : data.entrySet()) {
                zos.putNextEntry(new ZipEntry(entry.getKey()));
                int len;
                InputStream in = new ByteArrayInputStream(entry.getValue().getBytes(StandardCharsets.UTF_8));
                while ((len = in.read(buf)) != -1) {
                    zos.write(buf, 0, len);
                }
                // Complete the entry
                zos.closeEntry();
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    e.getStackTrace();
                }
            }
        }
    }
}
