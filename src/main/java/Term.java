import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Abstract class that represents a term
 *
 * @author Stan Roelofs
 * @version 1.0
 */
abstract class Term {
    protected Sort sort;

    /**
     * Creates a new term of sort {@code sort}
     * @param sort The sort of the new term
     * @throws IllegalArgumentException if {@code sort} is null
     */
    Term(Sort sort) {
        if (sort == null) {
            throw new IllegalArgumentException("Sort must not be null");
        }
        this.sort = sort;
    }

    /**
     * Returns a substitution such that the substitution applied to this term yields {@code term}.
     * If this is not possible it returns null
     *
     * @param term The term that should be obtained by applying the substitution
     * @param substitutions The substitutions that are already defined, can be modified by this function
     * @return A substitution such that if the substitution is applied to this term it yields {@code term},
     *         or null if such a substitution is not possible
     * @see Variable
     */
    public abstract Map<Variable, Term> getSubstitution(Term term, Map<Variable, Term> substitutions);

    /**
     * Returns all subterms of this term, including the term itself
     *
     * @return a Set of terms that are subterms of this term (including this term)
     */
    public abstract Set<Term> getAllSubTerms();

    /**
     * Substitutes each occurrence of {@code term} in this term by the term {@code substitute}
     * @param term The term to be substituted
     * @param substitute The term that replaces the old term
     * @return A new Term with each occurrence of {@code term} replaced by {@code substitute}
     */
    public abstract Term substitute(Term term, Term substitute);

    /**
     * Returns all the variables that occur in this term
     * @return A collection of variables that occur in this term
     * @see Variable
     * @see Collection
     */
    public abstract Collection<Variable> getVariables();

    public abstract String toString();

    public abstract boolean equals(Object o);

    public abstract int hashCode();

    public Sort getSort() {
        return sort;
    }
}
