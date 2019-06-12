package core;

import java.util.*;

/**
 * Class that represents a term constructed by applying a function to zero or more
 * arguments
 *
 * @author Stan Roelofs
 * @version 1.02
 */
public class FunctionTerm extends Term {
    private List<Term> subterms;
    private Function function;

    /**
     * Creates a new FunctionTerm from a constant
     * @param function A function with no inputs (a constant)
     * @throws IllegalArgumentException if {@code function} is not a constant or its sort is null
     * @see Term#Term(Sort)
     * @see Function
     */
    public FunctionTerm(Function function) {
        super(function.getOutputSort());

        if (function.getInputSorts().size() != 0) {
            throw new IllegalArgumentException("No subterms given for function with more than 0 inputs");
        }
        this.function = function;
        this.subterms = new ArrayList<>();
    }

    /**
     * Creates a new FunctionTerm from a function and a list of arguments
     * @param function A function
     * @param subTerms A list of subterms that are used as arguments for {@code function}
     * @throws IllegalArgumentException if the sort of {@code function} is null,
     *                                  {@code subTerms} is null or the number of elements
     *                                  does not match the number of inputs of {@code function}
     *                                  or the sorts of the elements in {@code subTerms} do not
     *                                  match the sorts of the inputs of {@code function}
     * @see Term#Term(Sort)
     * @see Term
     * @see Function
     */
    public FunctionTerm(Function function, List<Term> subTerms) {
        super(function.getOutputSort());
        this.function = function;
        this.subterms = new ArrayList<>();

        if (subTerms == null || subTerms.size() != function.getInputSorts().size()) {
            throw new IllegalArgumentException("Number of arguments does not match the number of function inputs of function" + function.toString());
        }

        for (int i = 0; i < subTerms.size(); i++) {
            if (!function.getInputSorts().get(i).equals(subTerms.get(i).getSort())) {
                throw new IllegalArgumentException("Input sort at position " + i + " does not match subterm sort of function " + function.toString());
            }
        }

        this.subterms.addAll(subTerms);
    }

    /**
     * Returns the function used to construct this term
     * @return this.term
     */
    public Function getFunction() {
        return this.function;
    }

    /**
     * Returns the arguments used to construct this term using this.function
     * @return this.subterms
     */
    public List<Term> getSubTerms() {
        return this.subterms;
    }

    @Override
    public Set<Term> getAllSubTerms() {
        Set<Term> result = new HashSet<>(this.subterms);
        result.add(this);

        for (Term subterm : this.subterms) {
            result.addAll(subterm.getAllSubTerms());
        }

        return result;
    }

    @Override
    public Map<Variable, Term> getSubstitution(Term term, Map<Variable, Term> substitutions) {
        if (term == null || substitutions == null) {
            throw new IllegalArgumentException("term and substitutions must not be null");
        }

        if (term instanceof Variable) {
            return null;
        } else if (term instanceof FunctionTerm) {
            FunctionTerm fterm = (FunctionTerm) term;

            // Function must be equal
            if (this.function.equals(fterm.getFunction())) {
                // All subterms of fterm must be an instance of all subterms of this.subterms

                for (int i = 0; i < this.subterms.size(); i++) {
                    if (this.subterms.get(i).getSubstitution(fterm.getSubTerms().get(i), substitutions) == null) {
                        return null;
                    }
                }

                return substitutions;
            } else {
                return null;
            }
        }
        return null;
    }

    @Override
    public Term applySubstitution(Map<Variable, Term> substitution) {
        if (substitution == null) {
            throw new IllegalArgumentException("Substitution must not be null");
        }

        List<Term> newSubterms = new ArrayList<>();
        for (Term subterm : this.subterms) {
            newSubterms.add(subterm.applySubstitution(substitution));
        }
        return new FunctionTerm(this.function, newSubterms);
    }

    @Override
    public Term substitute(Term term, Term substitute) {
        if (term == null || substitute == null) {
            throw new IllegalArgumentException("term and substitute must not be null");
        }
        if (this.equals(term)) {
            if (!this.sort.equals(substitute.getSort())) {
                throw new IllegalArgumentException("Sorts of term and substitute do not match");
            }
            return substitute;
        } else {
            List<Term> newSubterms = new ArrayList<>();
            for (Term subterm : this.subterms) {
                newSubterms.add(subterm.substitute(term, substitute));
            }
            return new FunctionTerm(this.function, newSubterms);
        }
    }

    @Override
    public Set<Variable> getVariables() {
        Set<Variable> result = new HashSet<>(this.subterms.size());

        for (Term term : subterms) {
            result.addAll(term.getVariables());
        }

        return result;
    }

    @Override
    public Set<Function> getUniqueFunctions() {
        Set<Function> result = new HashSet<>();
        result.add(this.function);

        for (Term subterm : this.subterms) {
            result.addAll(subterm.getUniqueFunctions());
        }

        return result;
    }

    @Override
    public int functionsAmount() {
        int result = 1;

        for (Term subterm : this.subterms) {
            result += subterm.functionsAmount();
        }

        return result;
    }

    @Override
    public int variablesAmountDistinct() {
        return this.getVariables().size();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(function.getName());

        if (subterms.size() > 0) {
            builder.append("(");
        }

        for (Iterator<Term> iterator = subterms.iterator(); iterator.hasNext();) {
            Term term = iterator.next();
            builder.append(term.toString());
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }

        if (subterms.size() > 0) {
            builder.append(")");
        }

        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof FunctionTerm)) {
            return false;
        }

        FunctionTerm term = (FunctionTerm) o;

        if (!(Objects.equals(this.sort, term.getSort()))) {
            return false;
        }

        if (!(Objects.equals(this.function, term.getFunction()))) {
            return false;
        }

        if (!(Objects.equals(this.subterms.size(), term.getSubTerms().size()))) {
            return false;
        }

        for (int i = 0; i < subterms.size(); i++) {
            if (!Objects.equals(subterms.get(i), term.getSubTerms().get(i))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.sort.hashCode();
        result = 31 * result + this.function.hashCode();

        for (Term subterm : subterms) {
            result = 31 * result + subterm.hashCode();
        }

        return result;
    }

    @Override
    public boolean isEquivalent(Term other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof FunctionTerm)) {
            return false;
        }

        FunctionTerm term = (FunctionTerm) other;

        if (!(Objects.equals(this.sort, term.getSort()))) {
            return false;
        }

        if (!(Objects.equals(this.function, term.getFunction()))) {
            return false;
        }

        if (!(Objects.equals(this.subterms.size(), term.getSubTerms().size()))) {
            return false;
        }

        if (this.getSubstitution(other, new HashMap<>()) == null) {
            return false;
        }

        if (other.getSubstitution(this, new HashMap<>()) == null) {
            return false;
        }

        return true;
    }
}
