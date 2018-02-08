package org.librairy.service.learner.tools;

import java.io.*;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class OutputWriter {

    BufferedWriter writer = null;

    public OutputWriter(String path){

        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void write(String line) throws IOException {
        write(line,true);
    }

    public void write(String line, Boolean newLine) throws IOException {
        writer.write(line);
        if (newLine) writer.write("\n");
    }

    public void close() throws IOException {
        if (writer != null) writer.close();
    }

}
