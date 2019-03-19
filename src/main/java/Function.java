import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class that represents a function
 * A function has a name, 0 or more input sorts, and one output sort
 *
 * @author Stan Roelofs
 * @version 1.0
 */
class Function {
    private List<Sort> inputSorts;
    private Sort outputSort;
    private String name;

    /**
     * Creates a function without any input sorts (a constant)
     * @param name The name of the function
     * @param sort The sort of the function
     * @throws IllegalArgumentException If {@code name} is empty or null, or {@code sort} is null
     */
    Function(String name, Sort sort) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name should not be empty or null");
        }
        if (sort == null) {
            throw new IllegalArgumentException("Sort must not be null");
        }

        this.name = name;
        this.outputSort = sort;
        this.inputSorts = new ArrayList<>();
    }

    /**
     * Creates a function
     * @param name The name of the function
     * @param input A list of input sorts
     * @param output An output sort
     */
    Function(String name, List<Sort> input, Sort output) {
        this(name, output);
        this.inputSorts.addAll(input);
    }

    String getName() {
        return name;
    }

    Sort getOutputSort() {
        return this.outputSort;
    }

    List<Sort> getInputSorts() {
        return this.inputSorts;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Function)) {
            return false;
        }

        Function func = (Function) o;

        if (!(Objects.equals(this.name, func.getName()))) {
            return false;
        }

        if (!(Objects.equals(this.outputSort, func.getOutputSort()))) {
            return false;
        }

        if (!(Objects.equals(this.inputSorts.size(), func.getInputSorts().size()))) {
            return false;
        }

        for (int i = 0; i < inputSorts.size(); i++) {
            if (!(Objects.equals(inputSorts.get(i), func.getInputSorts().get(i)))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.name.hashCode();
        result = 31 * result + this.outputSort.hashCode();

        for (Sort sort : inputSorts) {
            result = 31 * result + sort.hashCode();
        }

        return result;
    }
}
