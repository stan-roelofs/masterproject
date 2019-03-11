import java.util.ArrayList;
import java.util.Collection;

class Variable extends Term {
    private String name;

    Variable(Sort sort, String name) {
        super(sort);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public Term substitute(Variable var, Term substitute) {
        return substitute;
    }

    @Override
    public Collection<Variable> getVariables() {
        Collection<Variable> result = new ArrayList<>();
        result.add(this);
        return result;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
