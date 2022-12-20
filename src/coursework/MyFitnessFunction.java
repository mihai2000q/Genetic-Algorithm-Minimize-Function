package coursework;

import coursework.functions.IFunction;
import org.jgap.FitnessFunction;
import org.jgap.IChromosome;
public class MyFitnessFunction extends FitnessFunction {
    private final IFunction function;
    public MyFitnessFunction(IFunction function) {
        this.function = function;
    }
    @Override
    protected double evaluate(IChromosome chromosome) {
        double fitness = 0;
        for (int i = 0; i < chromosome.size(); i++)
            fitness += function.function((Double) chromosome.getGene(i).getAllele(), 0)
                     + function.getMinimalValue();
        return Math.abs(fitness);
    }
}
