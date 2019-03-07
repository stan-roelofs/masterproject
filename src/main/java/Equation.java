class Equation {
    private Term left;
    private Term right;

    Equation(Term l, Term r) {
        if (l.sort != r.sort) {
            throw new IllegalArgumentException("Sort of the two terms should be the same");
        }

        this.left = l;
        this.right = r;
    }

    Term getLeft() {
        return this.left;
    }

    Term getRight() {
        return this.right;
    }

    public Sort getSort() {
        return left.sort;
    }

    Equation substitute(Variable var, Term sub) {
        return new Equation(left.substitute(var, sub), right.substitute(var, sub));
    }
}
