package coursework.functions;

import coursework.Helper;

public class Part1Function implements coursework.functions.IFunction {
    private final double minX = 6.676; //manually added, could calculate it, but it would consume resources
    private final double minY = function(minX,0); //local minima
    @Override
    public double function(double x, double a) {
        //"a" is initialized with 0 everywhere, but it should not matter what is the value as it is not used
        //It is kept for part 2
        return Helper.precise((Math.pow(x, 3) - 17 * Math.pow(x, 2) + 93 * x - 163)
                * Math.sin(20 * x));
    }

    @Override
    public double getMinimalValue() {
        return minY;
    }

    @Override
    public double getMaximalFitness(int size) {
        return Helper.precise(Math.abs(size * minY * minY));
    }

}
