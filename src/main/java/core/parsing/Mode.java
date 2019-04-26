package core.parsing;

enum Mode {
    SIGMA, EQUATIONS, GOAL;

    private static Mode[] vals = values();

    public Mode next() {
        return vals[(this.ordinal() + 1) % vals.length];
    }
}