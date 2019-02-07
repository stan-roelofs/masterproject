import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();
        Option file = Option.builder("i").longOpt("input").hasArg().desc("File name of input file").argName("file").required().build();

        options.addOption(file);

        try {
            // parse the command line arguments
            CommandLine commandLine = parser.parse( options, args );

            if (commandLine.hasOption("i")) {
                String fileName = commandLine.getOptionValue("i");

                // Read file
                try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                    StringBuilder sb = new StringBuilder();
                    String line = br.readLine();

                    while (line != null) {
                        sb.append(line);
                        sb.append(System.lineSeparator());
                        line = br.readLine();
                    }
                    String everything = sb.toString();

                    System.out.println(everything);
                }
            }
        }
        catch(ParseException exp) {
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
