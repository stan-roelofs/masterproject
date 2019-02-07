import org.apache.commons.cli.*;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();
        Option file = Option.builder("i").longOpt("input").hasArg().desc("File name of input file").argName("file").required().build();

        options.addOption(file);

        try {
            // parse the command line arguments
            CommandLine line = parser.parse( options, args );

            if (line.hasOption("f")) {
                File input = new File(line.getOptionValue("f"));
            }
        }
        catch(ParseException exp) {
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
        }
    }
}
