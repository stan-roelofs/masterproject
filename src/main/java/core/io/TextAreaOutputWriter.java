package core.io;

import javax.swing.*;
import java.io.IOException;

public class TextAreaOutputWriter extends OutputWriter {

    private JTextArea textArea;

    public TextAreaOutputWriter(JTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void write(String text) throws IOException {
        this.textArea.append(text);
    }

    @Override
    public void writeLine(String text) throws IOException {
        write(text);
        write("\n");
    }

    @Override
    public void close() throws IOException {

    }
}
