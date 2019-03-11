import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

class FunctionTerm extends Term {
    private Collection<Term> subterms;
    private Function function;

    FunctionTerm(Sort sort, Function function, Collection<Term> subTerms) {
        super(sort);
        this.function = function;
        this.subterms = new ArrayList<>();
        if (subTerms != null) {
            this.subterms.addAll(subTerms);
        }
    }

    @Override
    public Term substitute(Variable var, Term substitute) {
        Collection<Term> newTerms = new ArrayList<>();
        for (Term term : subterms) {
            newTerms.add(term.substitute(var, substitute));
        }
        return new FunctionTerm(this.sort, this.function, newTerms);
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
}
