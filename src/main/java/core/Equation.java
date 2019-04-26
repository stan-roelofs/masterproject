package core;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Class that represents an equation
 * An equation is constructed from two terms of equal sort
 *
 * @author Stan Roelofs
 * @version 1.01
 */
public class Equation {
    private Term left;
    private Term right;

    /**
     * Constructs an equation from two terms
     * @param l The first term
     * @param r The second term
     * @throws IllegalArgumentException if one of the parameters is null or their sorts do not match
     * @see Term
     */
    public Equation(Term l, Term r) {
        if (l == null || r == null) {
            throw new IllegalArgumentException("l or r must not be null");
        }
        if (!(l.sort.equals(r.sort))) {
            throw new IllegalArgumentException("Sort of the two terms should be the same");
        }

        this.left = l;
        this.right = r;
    }

    /**
     * Returns left term of this equation
     * @return this.left
     */
    public Term getLeft() {
        return this.left;
    }

    /**
     * Returns right term of this equation
     * @return this.right
     */
    public Term getRight() {
        return this.right;
    }

    /**
     * Returns the sort of this equation
     * @return this.left.sort
     * @see Sort
     */
    public Sort getSort() {
        return left.sort;
    }

    /**
     * Substitutes each occurrence of {@code var} in this equation by the term {@code sub}
     * @param term The variable to be substituted
     * @param sub The term that replaces the variable
     * @return A new Equation with each occurrence of {@code var} replaced by {@code sub}
     * @throws IllegalArgumentException if any of the parameters is null
     * @see Variable
     * @see Term
     */
    public Equation substitute(Term term, Term sub) {
        if (term == null || sub == null) {
            throw new IllegalArgumentException("var and sub must not be null");
        }
        return new Equation(left.substitute(term, sub), right.substitute(term, sub));
    }

    /**
     * Returns all functions that occur in this equation
     * @return A set of functions that occur in this equation
     * @see Function
     */
    public Set<Function> getFunctions() {
        Set<Function> result = new HashSet<>();
        result.addAll(left.getFunctions());
        result.addAll(right.getFunctions());
        return result;
    }

    @Override
    public String toString() {
        return left.toString() + " = " + right.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Equation)) {
            return false;
        }

        Equation eq = (Equation) o;

        return Objects.equals(left, eq.getLeft()) && Objects.equals(right, eq.getRight());
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + left.hashCode();
        result = 31 * result + right.hashCode();
        return result;
    }
}
