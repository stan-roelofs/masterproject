import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Variable)) {
            return false;
        }

        Variable var = (Variable) o;
        return Objects.equals(this.sort, var.getSort()) && Objects.equals(this.name, var.getName());
    }
}
