package ivansCode.utils;

import java.util.List;

public final class BasicMath {

    private BasicMath(){}

    public static double getArithmeticAverage(List<Number> list){
        int numElements = list.size();
        double total = 0;
        for (Number currentNum : list){
            total += currentNum.doubleValue();
        }
        return total / numElements;
    }

}
