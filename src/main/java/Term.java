import java.util.Collection;

abstract class Term {
    protected Sort sort;

    Term(Sort sort) {
        this.sort = sort;
    }

    public abstract Term substitute(Variable var, Term substitute);
    public abstract Collection<Variable> getVariables();
    public abstract String toString();

    public Sort getSort() {
        return sort;
    }
}
