package core.parsing;

public class InvalidFunctionArgumentException extends Exception {
    public InvalidFunctionArgumentException(String errorMessage) {
        super(errorMessage);
    }
}
