package org.my;

import java.io.*;

public class TestUtil {

    public String loadFromFile(String fileName) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        assert (is != null);
        InputStreamReader ir = new InputStreamReader(is);
        BufferedReader rr = new BufferedReader(ir);
        StringBuilder buffer = new StringBuilder();

        String line;
        while ((line = rr.readLine()) != null) {
            buffer.append(line);
        }

        return buffer.toString();
    }
}
