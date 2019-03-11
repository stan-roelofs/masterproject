import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class Function {
    List<Sort> inputSorts = new ArrayList<>();
    Sort outputSort;
    private String name;

    Function(Sort sort) {

    }

    Function(Collection<Sort> sorts) {

    }

    FunctionTerm apply(Collection<FunctionTerm> inputTerms) {

        return null;
    }

    public String getName() {
        return name;
    }
}
