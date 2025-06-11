package es.urjc.etsii.grafo.PDSP.model;


import es.urjc.etsii.grafo.solution.SolutionValidator;
import es.urjc.etsii.grafo.solution.ValidationResult;

/**
 * Validate that a solution is valid for the PDSP problem.
 * Validation is always run after the algorithms executes, and can be run in certain algorithm stages to verify
 * that the current solution is valid.
 */
public class PDSPSolutionValidator extends SolutionValidator<PDSPSolution, PDSPInstance> {

    /**
     * Validate the current solution, check that no constraint is broken and everything is fine
     *
     * @param solution Solution to validate
     * @return ValidationResult.ok() if the solution is valid, ValidationResult.fail("reason why it failed") if a solution is not valid.
     */
    @Override
    public ValidationResult validate(PDSPSolution solution) {

        if(!solution.isCovered()){
            return ValidationResult.fail("Solution is not covered, remaining nodes: " + solution.getUnobservedNodes());
        }

        return ValidationResult.ok();
    }
}
