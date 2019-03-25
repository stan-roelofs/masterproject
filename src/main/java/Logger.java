/**
 * A simple logging class.
 * A logging call is only logged if the current logging level is greater than or equal to the level of the call
 *
 * @author Stan Roelofs
 * @version 1.0
 */

public class Logger {

    private static Level level = Level.INFO;

    public static void w(String msg) {
        if (level.ordinal() <= Level.WARNING.ordinal()) {
            System.out.println("WARNING: " + msg);
        }
    }

    public static void i(String msg) {
        if (level.ordinal() <= Level.INFO.ordinal()) {
            System.out.println("INFO: " + msg);
        }
    }

    public static void d(String msg) {
        if (level.ordinal() <= Level.DEBUG.ordinal()) {
            System.out.println("DEBUG: " + msg);
        }
    }

    public static void e(String msg) {
        if (level.ordinal() <= Level.ERROR.ordinal()) {
            System.err.println("ERROR: " + msg);
        }
    }
}

/**
 * Represents different logging levels in increasing order of importance.
 */
enum Level {
    DEBUG, INFO, WARNING, ERROR
}