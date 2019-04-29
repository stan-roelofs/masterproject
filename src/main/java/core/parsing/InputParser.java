package core.parsing;

import core.*;

import java.util.*;

/**
 * Parses text input and returns a set of equations, functions, and a goal
 *
 * @author Stan Roelofs
 * @version 1.02
 */
public class InputParser {
    public static EquationSystem parseSystem(List<String> input) throws ParserException {

        Set<Equation> equations = new HashSet<>();
        Set<Function> C = new HashSet<>();
        Set<Function> Sigma = new HashSet<>();
        Equation goal = null;

        int lineCount = 0;
        try {
            Mode mode = Mode.SIGMA;

            for (String line : input) {
                lineCount++;
                // When empty line move to next mode
                if (line.isEmpty()) {
                    mode = mode.next();
                    continue;
                }

                // Ignore comments (lines starting with //)
                if (line.startsWith("//")) {
                    continue;
                }

                switch (mode) {
                    case SIGMA:
                        parseFunction(line, Sigma, C);
                        break;
                    case EQUATIONS:
                        equations.add(parseEquation(Sigma, line));
                        break;
                    case GOAL:
                        // Goal
                        goal = parseEquation(Sigma, line);
                        break;
                }
            }
        } catch (IllegalArgumentException | NoSortException | InvalidFunctionArgumentException e) {
            Logger.e("Error on line: " + lineCount + " " + e.getMessage());
            throw new ParserException("Error parsing line " + lineCount);
        }

        boolean duplicate = false;
        for (Function f : Sigma) {
            for (Function f2 : Sigma) {
                if (f.getName().equals(f2.getName()) && !f.equals(f2)) {
                    duplicate = true;
                }
            }
        }

        if (duplicate) {
            Logger.w("Detected two functions with equal names, potentially results in errors");
        }

        return new EquationSystem(equations, Sigma, C, goal);
    }

    /**
     * Takes a string as input and constructs a Function object from this input
     *
     * @param line the input string
     * @return A Function object based on the contents of the input string
     * @throws IllegalArgumentException if {@code line} is null or is missing information
     * @see Function
     */
    public static Function parseFunction(String line, Set<Function> Sigma, Set<Function> C) throws IllegalArgumentException {
        if (line == null || line.isEmpty() || Sigma == null || C == null) {
            throw new IllegalArgumentException("Line most not be null or empty, Sigma and C must not be null");
        }

        String[] split = line.split(" ");

        boolean inC = split[0].equals("**");

        if (inC) {
            split = Arrays.copyOfRange(split, 1, split.length);
        }

        if (split.length <= 1) {
            throw new IllegalArgumentException("Function information missing: expected <C>? <name> <sort>* <sort>");
        }
        String name = split[0];

        // Get all sorts
        Map<String, Sort> sorts = new HashMap<>();
        for (String type : split) {
            if (type.equals(name)) {
                continue;
            }
            if (sorts.containsKey(type)) {
                continue;
            }
            sorts.put(type, new Sort(type));
        }

        // Create list of input sorts to function
        List<Sort> inputs = new ArrayList<>();
        for (int i = 1; i < split.length - 1; i++) {
            inputs.add(sorts.get(split[i]));
        }

        // Take the last sort as output sort
        Sort output = sorts.get(split[split.length - 1]);

        Function f = new Function(output, inputs, name);

        Logger.d("Parsed function " + f.getName() + " : " + f.getInputSorts().toString() + " -> " + f.getOutputSort().toString());

        Sigma.add(f);

        if (inC) {
            C.add(f);
        }

        return f;
    }

    /**
     * Takes a string and a set of functions as input and constructs an Equation object from this input
     * @param functions A set of functions that are necessary to recognize function symbols in the input string
     * @param line The input string
     * @return An Equation object based on the contents of the input string and functions
     * @throws IllegalArgumentException if {@code functions} is empty or null, {@code line} is null,
     *                                  {@code line} contains less or more than one "=", or {@code line}
     *                                  is empty left or right of "="
     * @see Equation
     * @see Function
     */
    public static Equation parseEquation(Set<Function> functions, String line) throws IllegalArgumentException, InvalidFunctionArgumentException, NoSortException {
        checkParameters(functions, line);
        if (!line.contains("=")) {
            throw new IllegalArgumentException("line does not contain '=', therefore is not an equation");
        }

        String[] split = line.split("=");
        if (split.length > 2) {
            throw new IllegalArgumentException("Equation contains more than one '='");
        }

        if (split.length < 2) {
            throw new IllegalArgumentException("Left or right side is empty");
        }

        String leftString = split[0];
        String rightString = split[1];

        Term left;
        Term right;
        try {
            left = parseTerm(functions, leftString, null);
            right = parseTerm(functions, rightString, left.getSort());
        } catch (NoSortException e) {
            right = parseTerm(functions, rightString, null);
            left = parseTerm(functions, leftString, right.getSort());
        }

        Equation eq = new Equation(left, right);
        Logger.d("Parsed equation " + eq.toString());
        return eq;
    }

    // TODO: clean up this mess..
    /**
     * Takes a string and a set of functions as input and constructs a Term object from this input
     * @param functions A set of functions that are necessary to recognize function symbols in the input string
     * @param line The input string
     * @param varSort The expected sort of the variable if {@code line} represents a variable
     * @return A term object that represents the same term as {@code line}
     * @throws IllegalArgumentException if {@code functions} is empty or null,
     * @see Function
     * @see Term
     * @see Variable
     * @see FunctionTerm
     */
    public static Term parseTerm(Set<Function> functions, String line, Sort varSort) throws InvalidFunctionArgumentException, NoSortException {
        checkParameters(functions, line);

        String normalized = line.replaceAll(" ", "");

        StringBuilder symbol = new StringBuilder();
        StringBuilder term = new StringBuilder();

        boolean parseTerm = false;
        for (char c : normalized.toCharArray()) {
            if (c == '(') {
                parseTerm = true;
            }
            if (!parseTerm) {
                symbol.append(c);
            } else {
                term.append(c);
            }
        }

        String subterms = term.toString();

        // Remove outer ( and ) from subterms
        if (subterms.length() > 0) {
            if (subterms.charAt(subterms.length() - 1) != ')') {
                throw new IllegalArgumentException("Invalid character found while parsing" +
                        " term: " + subterms.charAt(subterms.length() - 1) + ", expected )");
            }
            subterms = subterms.substring(1, subterms.length() - 1);
        }

        // The parsed outermost function symbol
        String function = symbol.toString();

        Set<Function> fs = new HashSet<>();
        for (Function f : functions) {
            if (function.equals(f.getName())) {
                fs.add(f);
            }
        }

        if (fs.size() > 1) {
            if (varSort == null) {
                Logger.w("returning");
                throw new NoSortException("dada");
            } else {
                // Try to match that function symbol to one of the parsed functions
                for (Function f : functions) {
                    if (function.equals(f.getName()) && f.getOutputSort().equals(varSort)) {
                        int numArguments = f.getInputSorts().size();
                        if (numArguments == 0) {
                            Logger.d("Parsed FunctionTerm " + f.toString());
                            return new FunctionTerm(f);
                        } else {
                            List<Term> subtermsList = new ArrayList<>();
                            List<String> subtermsStrings = new ArrayList<>();

                            int depth = 0;
                            StringBuilder current = new StringBuilder();
                            for (char c : subterms.toCharArray()) {
                                if (c == '(') {
                                    depth++;
                                }
                                if (c == ')') {
                                    depth--;
                                }
                                if (c == ',' && depth == 0) {
                                    subtermsStrings.add(current.toString());
                                    current = new StringBuilder();
                                } else {
                                    current.append(c);
                                }
                            }
                            subtermsStrings.add(current.toString());

                            if (subtermsStrings.size() != numArguments) {
                                throw new InvalidFunctionArgumentException("Invalid number of function arguments. Actual: " + subtermsStrings.size() + ", expected: " + numArguments);
                            }

                            int input = 0;
                            for (String subtermString : subtermsStrings) {
                                subtermsList.add(parseTerm(functions, subtermString, f.getInputSorts().get(input)));
                                input++;
                            }

                            FunctionTerm ft = new FunctionTerm(f, subtermsList);
                            Logger.d("Parsed FunctionTerm " + ft.toString());
                            return ft;
                        }
                    }
                }
            }
        }

        // Try to match that function symbol to one of the parsed functions
        for (Function f : functions) {
            if (function.equals(f.getName())) {
                int numArguments = f.getInputSorts().size();
                if (numArguments == 0) {
                    Logger.d("Parsed FunctionTerm " + f.toString());
                    return new FunctionTerm(f);
                } else {
                    List<Term> subtermsList = new ArrayList<>();
                    List<String> subtermsStrings = new ArrayList<>();

                    int depth = 0;
                    StringBuilder current = new StringBuilder();
                    for (char c : subterms.toCharArray()) {
                        if (c == '(') {
                            depth++;
                        }
                        if (c == ')') {
                            depth--;
                        }
                        if (c == ',' && depth == 0) {
                            subtermsStrings.add(current.toString());
                            current = new StringBuilder();
                        } else {
                            current.append(c);
                        }
                    }
                    subtermsStrings.add(current.toString());

                    if (subtermsStrings.size() != numArguments) {
                        throw new InvalidFunctionArgumentException("Invalid number of function arguments. Actual: " + subtermsStrings.size() + ", expected: " + numArguments);
                    }

                    int input = 0;
                    for (String subtermString : subtermsStrings) {
                        subtermsList.add(parseTerm(functions, subtermString, f.getInputSorts().get(input)));
                        input++;
                    }

                    FunctionTerm ft = new FunctionTerm(f, subtermsList);
                    Logger.d("Parsed FunctionTerm " + ft.toString());
                    return ft;
                }
            }
        }

        if (varSort == null) {
            throw new NoSortException("Term is not a function and no sort is given for variable");
        }

        // If none of the functions match, the symbol has to be a variable
        Variable v = new Variable(varSort, function);
        Logger.d("Parsed Variable " + v.toString());
        return v;
    }

    private static void checkParameters(Set<Function> functions, String line) {
        if (functions == null) {
            throw new IllegalArgumentException("functions must not be empty or null");
        }
        if (line == null || line.isEmpty()) {
            throw new IllegalArgumentException("line must not be null or empty");
        }
    }
}



