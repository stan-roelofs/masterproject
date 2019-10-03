package core;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class that implements the Runnable interface for rewriting of terms, such that this can be done in thread(s)
 *
 * @author Stan Roelofs
 * @version 1.0
 */
class RewriteThread implements Runnable {

    private Set<Term> resultLeft = new HashSet<>();
    private Set<Term> resultRight = new HashSet<>();

    private Set<Term> leftTerms;
    private Set<Term> rightTerms;
    private Set<Equation> equations;
    private Map<Term, Term> leftSteps;
    private Map<Term, Term> rightSteps;
    private int threadNumber;
    private boolean rewriteLeft;
    private boolean rewriteRight;

    RewriteThread(int thread, Set<Term> leftTerms, Set<Term> rightTerms, Set<Equation> equations,
                  Map<Term, Term> leftSteps, Map<Term, Term> rightSteps, boolean rewriteRight, boolean rewriteLeft) {
        this.leftTerms = leftTerms;
        this.rightTerms = rightTerms;
        this.equations = equations;
        this.leftSteps = leftSteps;
        this.rightSteps = rightSteps;
        this.threadNumber = thread;
        this.rewriteLeft = rewriteLeft;
        this.rewriteRight = rewriteRight;
    }

    @Override
    public void run() {
        resultLeft.addAll(Prover.rewriteAll(leftTerms, equations, leftSteps, rewriteRight, rewriteLeft));
        resultRight.addAll(Prover.rewriteAll(rightTerms, equations, rightSteps, rewriteRight, rewriteLeft));

        //Logger.d("Thread " + threadNumber + " done");
    }

    Set<Term> getResultLeft() {
        return resultLeft;
    }

    Set<Term> getResultRight() {
        return resultRight;
    }
}