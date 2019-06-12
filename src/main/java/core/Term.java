package core;

import java.util.Map;
import java.util.Set;

/**
 * Abstract class that represents a term
 *
 * @author Stan Roelofs
 * @version 1.02
 */
public abstract class Term {
    protected Sort sort;

    /**
     * Creates a new term of sort {@code sort}
     * @param sort The sort of the new term
     * @throws IllegalArgumentException if {@code sort} is null
     */
    public Term(Sort sort) {
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
     * Applies a substitution (a mapping from variables to terms) on this term.
     * Hence, after execution every variable in this term is replaced by substitution.get(variable)
     *
     * @param substitution A substitution which replaces variables by terms
     * @return A new term such that all variables in {@code substitution} im this term
     *          are replaced by substitution.get(variable)
     * @see Variable
     */
    public abstract Term applySubstitution(Map<Variable, Term> substitution);

    /**
     * Returns all subterms of this term, including the term itself
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
     * @return A set of variables that occur in this term
     * @see Variable
     */
    public abstract Set<Variable> getVariables();

    /**
     * Returns all functions that occur in this term
     * @return A set of functions that occur in this term
     * @see Function
     */
    public abstract Set<Function> getUniqueFunctions();

    /**
     * Returns the number of functions that occur in this term
     * @return The total number of functions that occur in this term
     * @see Function
     */
    public abstract int functionsAmount();

    /**
     * Returns the number of distinct variables that occur in this term
     * @return The number of distinct variables that occur in this term and its subterms
     * @see Variable
     * @deprecated
     */
    public abstract int variablesAmountDistinct();

    public abstract String toString();

    public abstract boolean equals(Object o);

    public abstract int hashCode();

    public Sort getSort() {
        return sort;
    }

    public abstract boolean isEquivalent(Term other);
}
