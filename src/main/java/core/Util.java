package core;

import java.io.BufferedWriter;
import java.io.IOException;

public final class Util {

    /**
     * Private constructor to prevent instantiation of this class
     */
    private Util() {

    }

    public static void writeLine(BufferedWriter writer, String string) throws IOException {
        writer.write(string);
        writer.newLine();
    }
}
