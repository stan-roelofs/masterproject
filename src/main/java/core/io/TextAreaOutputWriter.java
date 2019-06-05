package core.io;

import javax.swing.*;

public class TextAreaOutputWriter extends OutputWriter {

    private JTextArea textArea;

    public TextAreaOutputWriter(JTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void write(String text) {
        if (!enabled) {
            return;
        }
        this.textArea.append(text);
    }

    @Override
    public void writeLine(String text) {
        if (!enabled) {
            return;
        }
        write(text);
        write("\n");
    }

    @Override
    public void close() {

    }
}
