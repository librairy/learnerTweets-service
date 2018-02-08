package org.librairy.service.learner.tools;

import java.io.*;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class CorpusReader {

    public BufferedReader getReader() {
        return reader;
    }

    BufferedReader reader = null;

    public CorpusReader(String path){
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String readLine() throws IOException {
        return reader.readLine();
    }

    public void close() throws IOException {
        if (reader != null) reader.close();
    }
}
