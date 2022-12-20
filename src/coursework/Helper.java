package coursework;

public class Helper {
    public static double precise(double number) {
        return Math.round(number * Math.pow(10, 3)) / Math.pow(10, 3);
    }
}
