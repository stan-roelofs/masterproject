import java.util.Collection;

class InputParser {
    static void parse(Collection<String> input) {

        parseC(input);

        for (String line : input) {

            if (line.contains("=")) {
                Equation eq = parseEquation(line);
            }
        }
    }

    private static void parseC(Collection<String> input) {

    }

    private static Function parseFunction(String term) throws IllegalArgumentException {
        // TODO: check parameter

        return null;
    }

    private static Equation parseEquation(String line) throws IllegalArgumentException {
        if (line == null) {
            throw new IllegalArgumentException("line must not be null");
        }
        if (!line.contains("=")) {
            throw new IllegalArgumentException("line does not contain '=', therefore is not an equation");
        }

        String[] split = line.split("=");
        if (split.length > 2) {
            throw new IllegalArgumentException("Equation contains more than one '='");
        }

        String leftString = split[0];
        String rightString = split[1];

        if (leftString.isEmpty() || rightString.isEmpty()) {
            throw new IllegalArgumentException("Left or right cannot be empty");
        }

        Term left = parseTerm(leftString);
        Term right = parseTerm(rightString);

        return new Equation(left, right);
    }

    private static Term parseTerm(String line) {
        if (line == null) {
            throw new IllegalArgumentException("line must not be null");
        }

        return null;
    }
}
