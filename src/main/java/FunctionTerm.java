import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

class FunctionTerm extends Term {
    private Collection<Term> subterms;
    private Function function;

    FunctionTerm(Function function) {
        super(function.getOutputSort());
        this.function = function;
        this.subterms = new ArrayList<>();
    }

    FunctionTerm(Function function, Collection<Term> subTerms) {
        this(function);

        if (subTerms == null || subTerms.size() != function.getInputSorts().size()) {
            throw new IllegalArgumentException("Number of arguments does not match the number of function inputs");
        }

        this.subterms.addAll(subTerms);
    }

    public Function getFunction() {
        return this.function;
    }

    public Collection<Term> getSubterms() {
        return this.subterms;
    }

    @Override
    public Term substitute(Variable var, Term substitute) {
        Collection<Term> newTerms = new ArrayList<>();
        for (Term term : subterms) {
            newTerms.add(term.substitute(var, substitute));
        }
        return new FunctionTerm(this.function, newTerms);
    }

    @Override
    public Collection<Variable> getVariables() {
        Collection<Variable> result = new ArrayList<>(this.subterms.size());
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

        if (!(Objects.equals(this.sort, term.sort))) {
            return false;
        }

        if (!(Objects.equals(this.function, term.getFunction()))) {
            return false;
        }

        if (!(Objects.equals(this.subterms.size(), term.getSubterms().size()))) {
            return false;
        }

        for (Term subterm : subterms) {
            for (Term subTerm2 : term.getSubterms()) {
                if (!(Objects.equals(subterm, subTerm2))) {
                    return false;
                }
            }
        }

        return true;
    }
}
