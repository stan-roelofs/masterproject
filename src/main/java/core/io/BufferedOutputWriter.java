package core.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class BufferedOutputWriter extends OutputWriter {

    private BufferedWriter writer;

    public BufferedOutputWriter(OutputStreamWriter outputStreamWriter) {
        this.writer = new BufferedWriter(outputStreamWriter);
    }

    @Override
    public void write(String line) throws IOException {
        if (!enabled) {
            return;
        }
        writer.write(line);
    }

    @Override
    public void writeLine(String line) throws IOException {
        if (!enabled) {
            return;
        }
        writer.write(line);
        writer.newLine();
    }

    @Override
    public void close() throws IOException {
        writer.flush();
        writer.close();
    }
}
