import java.util.*;

class FunctionTerm extends Term {
    private List<Term> subterms;
    private Function function;

    FunctionTerm(Function function) {
        super(function.getOutputSort());

        if (function.getInputSorts().size() != 0) {
            throw new IllegalArgumentException("No subterms given for function with more than 0 inputs");
        }
        this.function = function;
        this.subterms = new ArrayList<>();
    }

    FunctionTerm(Function function, List<Term> subTerms) {
        super(function.getOutputSort());
        this.function = function;
        this.subterms = new ArrayList<>();

        if (subTerms == null || subTerms.size() != function.getInputSorts().size()) {
            throw new IllegalArgumentException("Number of arguments does not match the number of function inputs");
        }

        for (int i = 0; i < subTerms.size(); i++) {
            if (!function.getInputSorts().get(i).equals(subTerms.get(i).getSort())) {
                throw new IllegalArgumentException("Input sort at position " + i + " does not match subterm sort");
            }
        }

        this.subterms.addAll(subTerms);
    }

    Function getFunction() {
        return this.function;
    }

    List<Term> getSubterms() {
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
        if (term instanceof Variable) {
            return null;
        } else if (term instanceof FunctionTerm) {
            FunctionTerm fterm = (FunctionTerm) term;

            // Function must be equal
            if (this.function.equals(fterm.getFunction())) {
                // All subterms of fterm must be an instance of all subterms of this.subterms

                for (int i = 0; i < this.subterms.size(); i++) {
                    if (this.subterms.get(i).getSubstitution(fterm.getSubterms().get(i), substitutions) == null) {
                        return null;
                    }
                }

                return substitutions;
            } else {
                return null;
            }
        }
        return substitutions;
    }

    @Override
    public boolean instanceOf(Term term, Map<Variable, Term> substitutions) {
        if (term instanceof Variable) {
            return false;
        } else if (term instanceof FunctionTerm) {
            FunctionTerm fterm = (FunctionTerm) term;

            // Function must be equal
            if (this.function.equals(fterm.getFunction())) {
                // All subterms of fterm must be an instance of all subterms of this.subterms
                for (int i = 0; i < this.subterms.size(); i++) {
                    if (!(this.subterms.get(i).instanceOf(fterm.getSubterms().get(i), substitutions))) {
                        return false;
                    }
                }

                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    @Override
    public Term substitute(Term term, Term substitute) {
        if (this.equals(term)) {
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

        if (!(Objects.equals(this.subterms.size(), term.getSubterms().size()))) {
            return false;
        }

        for (int i = 0; i < subterms.size(); i++) {
            if (!Objects.equals(subterms.get(i), term.getSubterms().get(i))) {
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
}
