import org.apache.commons.cli.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();

        // Create options
        Options options = new Options();
        options.addOption(Option.builder("i").longOpt("input").hasArg().desc("File name of input file").argName("file").required().build());
        options.addOption(Option.builder("o").longOpt("output").hasArg().desc("File name of output file").argName("file").build());

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

                    // If no output file specified, use System.out
                    OutputStream output;
                    if (!commandLine.hasOption("o")) {
                        output = System.out;
                    } else {
                        try {
                            output = new FileOutputStream(commandLine.getOptionValue("o"));
                        } catch (FileNotFoundException e) {
                            Logger.e("File not found");
                            return;
                        }
                    }

                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));

                    try {
                        Prover.induction(system, writer);
                    } catch (IOException e) {
                        Logger.e("IOException: " + e.getMessage());
                    }
                    writer.flush();
                    writer.close();
                }
            } else {
                // TODO: print usage instructions
            }
        } catch(ParseException exp) {
            Logger.e("Parsing failed.  Reason: " + exp.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
