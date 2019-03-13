import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

class Function {
    private List<Sort> inputSorts;
    private Sort outputSort;
    private String name;

    Function(String name, Sort sort) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name should not be empty");
        }

        this.name = name;
        this.outputSort = sort;
        this.inputSorts = new ArrayList<>();
    }

    Function(String name, Collection<Sort> input, Sort output) {
        this(name, output);
        this.inputSorts.addAll(input);
    }

    public String getName() {
        return name;
    }

    public Sort getOutputSort() {
        return this.outputSort;
    }

    public Collection<Sort> getInputSorts() {
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

        for (Sort inputSort : this.inputSorts) {
            for (Sort inputSort2 : func.getInputSorts()) {
                if (!(Objects.equals(inputSort, inputSort2))) {
                    return false;
                }
            }
        }

        return true;
    }
}
