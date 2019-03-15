import java.util.Collection;

/**
 * Abstract class that represents a term
 *
 * @author Stan Roelofs
 * @version 1.0
 */
abstract class Term {
    protected Sort sort;

    Term(Sort sort) {
        if (sort == null) {
            throw new IllegalArgumentException("Sort must not be null");
        }
        this.sort = sort;
    }

    /**
     * Substitutes each occurrence of {@code var} in this term by the term {@code substitute}
     * @param var The variable to be substituted
     * @param substitute The term that replaces the variable
     * @return A new Term with each occurrence of {@code var} replaced by {@code substitute}
     */
    public abstract Term substitute(Variable var, Term substitute);

    /**
     * Returns all the variables that occur in this term
     * @return A collection of variables that occur in this term
     * @see Variable
     * @see Collection
     */
    public abstract Collection<Variable> getVariables();

    public abstract String toString();

    public abstract boolean equals(Object o);

    public Sort getSort() {
        return sort;
    }
}
