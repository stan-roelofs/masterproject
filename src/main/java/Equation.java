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

    public Term getLeft() {
        return this.left;
    }

    public Term getRight() {
        return this.right;
    }
}
