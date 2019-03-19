import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Class that represents a variable
 * A variable is a Term and additionally has a name
 *
 * @author Stan Roelofs
 * @version 1.0
 */
class Variable extends Term {
    private String name;

    Variable(Sort sort, String name) {
        super(sort);

        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name should not be empty");
        }
        this.name = name;
    }

    String getName() {
        return name;
    }

    @Override
    public boolean instanceOf(Term term, Map<Variable, Term> substitutions) {
        if (substitutions.containsKey(this)) {
            return substitutions.get(this).equals(term);
        }
        if (term.getSort().equals(this.sort)) {
            substitutions.put(this, term);
            return true;
        }
        return false;
    }

    @Override
    public Term substitute(Variable var, Term substitute) {
        if (var.equals(this)) {
            if (!this.sort.equals(substitute.getSort())) {
                throw new IllegalArgumentException("Sorts of variable and substitute do not match");
            }
            return substitute;
        } else {
            return this;
        }
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

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.sort.hashCode();
        result = 31 * result + this.name.hashCode();
        return result;
    }
}
