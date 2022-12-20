package coursework;

import coursework.functions.IFunction;
import org.jgap.*;
import org.jgap.event.EventManager;
import org.jgap.impl.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BaseClass {
    protected final IFunction function;
    private final int lowerBound = 5;
    private final int upperBound = 7;
    private final int chromosomeSize = 16;
    private final int populationSize = 50;
    private final double limitOfEvolutions = 50;
    private final boolean keepPopulationSizeConstant = false;
    private final boolean weightedRoulette = false;
    private final float originalRate = 0.9f;
    private final boolean processBeforeGeneticOperations = true;
    private final boolean mutation = true;
    private final int mutationRate = 20; //it is 1 over 35
    private final boolean crossover = true;
    private final float crossoverRatePercentage = 0.8f;
    private final boolean elitism = false;
    private final boolean checkIdentical = true;

    public BaseClass(IFunction function) {
        this.function = function;
        try {
            this.init();
        } catch (InvalidConfigurationException exception) {
            System.err.println("couldn't initialize");
            throw new RuntimeException(exception);
        }
    }
    private void init() throws InvalidConfigurationException {
        //configurations
        Configuration config = new Configuration();
        //default configuration -- decided to use this instead of the DefaultConfiguration object
        //so that I have freedom over the genetic operators and the whole configuration itself
        config.setBreeder(new GABreeder());
        config.setRandomGenerator(new StockRandomGenerator());
        config.setEventManager(new EventManager());
        config.setMinimumPopSizePercent(0);
        config.setSelectFromPrevGen(1.0);
        config.setFitnessEvaluator(new DefaultFitnessEvaluator());
        config.setChromosomePool(new ChromosomePool());

        //chromosome representation
        IChromosome sampleChromosome;
        sampleChromosome = new Chromosome(config, new DoubleGene(config, lowerBound, upperBound), chromosomeSize);
        config.setSampleChromosome(sampleChromosome);

        //population (size, ...)
        config.setKeepPopulationSizeConstant(keepPopulationSizeConstant);
        config.setPopulationSize(populationSize);

        //selection procedure
        NaturalSelector selector;
        if(weightedRoulette)
            selector = new WeightedRouletteSelector(config);
        else
            selector = new BestChromosomesSelector(config, originalRate);
        config.addNaturalSelector(selector, processBeforeGeneticOperations);

        //genetic operators
            //mutation
        if(mutation) {
            MutationOperator mutationOperator = new MutationOperator(config, mutationRate);
            config.addGeneticOperator(mutationOperator);
        }
            //crossover
        if(crossover) {
            CrossoverOperator crossoverOperator = new CrossoverOperator(config, crossoverRatePercentage);
            config.addGeneticOperator(crossoverOperator);
        }

        //transmission (elitism, new population, ...)
        config.setPreservFittestIndividual(elitism); //elitism otherwise generational

        //set fitness function
        config.setFitnessFunction(new MyFitnessFunction(function));

        //initialize population -- first generation
        Genotype genotype = Genotype.randomInitialGenotype(config);

        IChromosome previous = genotype.getFittestChromosome();
        System.out.println("\n--------------------------------------------------------------\n");
        System.out.println("the first fittest is\n" + chromosomeToString(previous));
        System.out.println("population size is " + genotype.getPopulation().size());
        System.out.println("\n---------------------------------------------------------------\n");
        //termination strategies -- by limiting the number of generations
        for (int i = 1; i <= limitOfEvolutions; i++) {
            genotype.evolve();

            //printGenotype(genotype);

            IChromosome fittest = genotype.getFittestChromosome();
            //termination strategy --
            //when my fittest chromosome reaches its final potential I stop generating further
            if(fittest.getFitnessValue() >= function.getMaximalFitness(chromosomeSize)) {
                System.out.println("Found the best chromosome");
                break;
            }
            //termination strategy --
            if(checkIdenticalPopulation(genotype) && checkIdentical) {
                System.out.println("More than 90 % of the population has the same fitness value");
                break;
            }

            if(Helper.precise(previous.getFitnessValue()) >=
                    Helper.precise(fittest.getFitnessValue())) continue; //don't print
            else
                previous = (IChromosome) fittest.clone(); //replace previous

            System.out.println("--------------------------------------------------------------\n");
            System.out.println("the fittest in generation " + i + " is\n" + chromosomeToString(fittest));
            System.out.println("population size is " + genotype.getPopulation().size());
            System.out.println("---------------------------------------------------------------");
        }
    }
    private boolean checkIdenticalPopulation(Genotype genotype) {
        var chromosomes = genotype.getPopulation().getChromosomes();
        if(chromosomes.size() < 10)
            return false;
        List<Integer> integers = new ArrayList<>(chromosomes.size());
        int count = 0;
        for(int i = 0; i < chromosomes.size(); i++) {
            for (int j = i + 1; j < chromosomes.size(); j++)
                if(chromosomes.get(i).getFitnessValue() == chromosomes.get(j).getFitnessValue())
                    count++;
                else
                    break;
            integers.add(count);
            count = 0;
            if((float) (chromosomes.size() - 1 - i) / (chromosomes.size() - 1) < 9 / 10f)
                break;
        }
        return (float) integers.stream().max(Comparator.naturalOrder()).get() / (chromosomes.size() - 1) > 9 / 10f;
    }
    private void printGenotype(Genotype genotype) {
        System.out.println("\n*********************************************************\n");
        for (int j = 0; j < genotype.getPopulation().size(); j++)
            System.out.println(chromosomeToString(genotype.getPopulation().getChromosome(j)));
        System.out.println("\n*********************************************************\n");
    }
    protected static String chromosomeToString(IChromosome chromosome) {
        StringBuilder representation = new StringBuilder();
        representation.append("Size:").append(chromosome.size());
        representation.append(", Fitness value:").append(chromosome.getFitnessValue());
        representation.append(", Alleles:");
        representation.append("[");

        for(int i = 0; i < chromosome.size(); ++i) {
            if (i > 0)
                representation.append(", ");

            if (chromosome.getGene(i) == null)
                representation.append("null");
            else
                representation.append(chromosome.getGene(i).getAllele().toString());

        }
        representation.append("]");
        return representation.toString();
    }
}
