import java.util.Collection;

abstract class Term {
    protected Sort sort;

    Term(Sort sort) {
        if (sort == null) {
            throw new IllegalArgumentException("Sort must not be null");
        }
        this.sort = sort;
    }

    public abstract Term substitute(Variable var, Term substitute);
    public abstract Collection<Variable> getVariables();
    public abstract String toString();
    public abstract boolean equals(Object o);

    public Sort getSort() {
        return sort;
    }
}
