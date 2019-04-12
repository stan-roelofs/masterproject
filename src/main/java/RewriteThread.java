import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
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

    RewriteThread(int thread, Set<Term> leftTerms, Set<Term> rightTerms, Set<Equation> equations, Map<Term, Term> leftSteps, Map<Term, Term> rightSteps) {
        this.leftTerms = leftTerms;
        this.rightTerms = rightTerms;
        this.equations = equations;
        this.leftSteps = leftSteps;
        this.rightSteps = rightSteps;
        this.threadNumber = thread;
    }

    @Override
    public void run() {
        resultLeft.addAll(Prover.rewriteAll(leftTerms, equations, leftSteps));
        resultRight.addAll(Prover.rewriteAll(rightTerms, equations, rightSteps));

        Logger.d("Thread " + threadNumber + " done");
    }

    public Set<Term> getResultLeft() {
        return resultLeft;
    }

    public Set<Term> getResultRight() {
        return resultRight;
    }
}