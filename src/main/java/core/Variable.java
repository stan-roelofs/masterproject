package core;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Class that represents a variable
 * A variable is a Term and additionally has a name
 *
 * @author Stan Roelofs
 * @version 1.02
 */
public class Variable extends Term {
    private String name;

    /**
     * Creates a variable with sort {@code sort} and name {@code name}.
     * @param sort The sort of the new variable
     * @param name The name of the new variable
     * @throws IllegalArgumentException if {@code name} is empty or {@code sort} is null
     * @see Sort
     * @see Term#Term(Sort)
     */
    public Variable(Sort sort, String name) {
        super(sort);

        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name should not be empty");
        }
        this.name = name;
    }

    /**
     * Returns the name of this variable
     * @return this.name
     */
    public String getName() {
        return name;
    }

    @Override
    public Map<Variable, Term> getSubstitution(Term term, Map<Variable, Term> substitutions) {
        if (term == null || substitutions == null) {
            throw new IllegalArgumentException("term and substitutions must not be null");
        }

        if (substitutions.containsKey(this)) {
            if (substitutions.get(this).equals(term)) {
                return substitutions;
            }
        } else {
            if (term.getSort().equals(this.sort)) {
                substitutions.put(this, term);
                return substitutions;
            }
        }
        return null;
    }

    @Override
    public Term applySubstitution(Map<Variable, Term> substitution) {
        if (substitution == null) {
            throw new IllegalArgumentException("Substitution must not be null");
        }
        return substitution.getOrDefault(this, this);
    }

    @Override
    public Set<Term> getUniqueSubterms() {
        return new HashSet<>();
    }

    @Override
    public int subtermsAmount() {
        return 1;
    }

    @Override
    public Term substitute(Term term, Term substitute) {
        if (term == null || substitute == null) {
            throw new IllegalArgumentException("term and substitute must not be null");
        }
        if (term.equals(this)) {
            if (!this.sort.equals(substitute.getSort())) {
                throw new IllegalArgumentException("Sorts of variable and substitute do not match");
            }
            return substitute;
        } else {
            return this;
        }
    }

    @Override
    public Set<Variable> getVariables() {
        Set<Variable> result = new HashSet<>();
        result.add(this);
        return result;
    }

    @Override
    public Set<Function> getUniqueFunctions() {
        return new HashSet<>();
    }

    @Override
    public int functionsAmount() {
        return 0;
    }

    @Override
    public int variablesAmountDistinct() {
        return 1;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Variable)) {
            return false;
        }

        Variable var = (Variable) o;
        return this.isEquivalent(var) && Objects.equals(this.name, var.getName());
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.sort.hashCode();
        result = 31 * result + this.name.hashCode();
        return result;
    }

    @Override
    public boolean isEquivalent(Term other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Variable)) {
            return false;
        }

        Variable var = (Variable) other;
        return Objects.equals(this.sort, var.getSort());
    }
}
