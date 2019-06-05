package core.io;

import java.io.IOException;
import java.util.Collection;

public abstract class OutputWriter {

    protected boolean enabled = true;

    public abstract void write(String text) throws IOException;

    public abstract void writeLine(String text) throws IOException;

    public void writeLines(String[] lines) throws IOException {
        for (String line : lines) {
            this.writeLine(line);
        }
    }

    public void writeLines(Collection<String> lines) throws IOException {
        for (String line : lines) {
            this.writeLine(line);
        }
    }

    public abstract void close() throws IOException;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
