package cli;

import core.EquationSystem;
import core.Prover;
import core.io.BufferedOutputWriter;
import core.logging.Logger;
import core.parsing.InputParser;
import core.parsing.ParserException;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Main class for CLI
 *
 * @author Stan Roelofs
 * @version 1.00
 */
public class Main {
    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();

        // Create options
        Options options = new Options();
        options.addOption(Option.builder("i").longOpt("input").hasArg().desc("File name of input file").argName("file").required().build());
        options.addOption(Option.builder("o").longOpt("output").hasArg().desc("File name of output file").argName("file").build());
        options.addOption(Option.builder("d").longOpt("depth").hasArg().desc("The maximum depth when using BFS to search for a conversion").build());
        options.addOption(Option.builder("rl").longOpt("rewriteLeft").desc("Enables rewriting in both directions, rather than only left to right").build());

        try {
            // parse the command line arguments
            CommandLine commandLine = parser.parse( options, args );

            String fileName = commandLine.getOptionValue("i");
            // Read file
            try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                List<String> input = new ArrayList<>();

                String line = br.readLine();
                while (line != null) {
                    input.add(line);
                    line = br.readLine();
                }

                EquationSystem system = null;
                try {
                    system = InputParser.parseSystem(input);
                } catch(ParserException e) {
                    Logger.e("Exception while parsing, quitting program");
                    System.exit(1);
                }

                // If no output file specified, use System.out
                OutputStream output;
                if (!commandLine.hasOption("o")) {
                    output = System.out;
                } else {
                    // Otherwise use the specified file
                    String pathToFile = commandLine.getOptionValue("o");
                    File outputFile = new File(pathToFile);

                    if (outputFile.exists() && !outputFile.isFile()) {
                        Logger.e("Specified output is not a file");
                        throw new IllegalArgumentException("Output must be a file");
                    }

                    output = new FileOutputStream(outputFile);
                }

                BufferedOutputWriter writer = new BufferedOutputWriter(new OutputStreamWriter(output));

                int searchDepth = 13;
                if (commandLine.hasOption("d")) {
                    searchDepth = Integer.parseInt(commandLine.getOptionValue("d"));
                }

                boolean rewriteLeft = false;
                if (commandLine.hasOption("rl")) {
                    rewriteLeft = true;
                }

                boolean result = Prover.inductionLemmaSearch(system, writer, searchDepth, rewriteLeft, 5, 2, 100);

                if (result) {
                    writer.write("Successfully proved goal " + system.getGoal().toString());
                } else {
                    writer.write("Failed to prove goal " + system.getGoal().toString());
                }
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch(ParseException exp) {
            Logger.e("Parsing failed.  Reason: " + exp.getMessage());
        }
    }
}