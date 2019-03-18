import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();

        // Create options
        Options options = new Options();
        options.addOption(Option.builder("i").longOpt("input").hasArg().desc("File name of input file").argName("file").required().build());

        try {
            // parse the command line arguments
            CommandLine commandLine = parser.parse( options, args );

            if (commandLine.hasOption("i")) {
                String fileName = commandLine.getOptionValue("i");

                // Read file
                try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                    List<String> input = new ArrayList<>();

                    String line = br.readLine();
                    while (line != null) {
                        input.add(line);
                        line = br.readLine();
                    }

                    EquationSystem system = InputParser.parseSystem(input);
                    system.print();
                    Prover.induction(system);
                }
            }
        }
        catch(ParseException exp) {
            Logger.e( "Parsing failed.  Reason: " + exp.getMessage() );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
