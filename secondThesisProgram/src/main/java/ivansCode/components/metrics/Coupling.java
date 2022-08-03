package ivansCode.components.metrics;

import java.util.Set;

public final class Coupling {

    private Coupling(){}

    public static <T> int distance(Set<T> a, Set<T> b){

        Set<T> fstSet = (a.size() < b.size() ? a : b);
        int onlyFst = 0;
        Set<T> sndSet = (a.size() < b.size() ? b : a);
        int onlySnd = 0;
        int union = 0;

        for (T currentElement : fstSet){
            if (fstSet.contains(currentElement) && sndSet.contains(currentElement)){
                union++;
            } else if (fstSet.contains(currentElement)){
                onlyFst++;
            } else if (sndSet.contains(currentElement)) {
                onlySnd++;
            }
        }

        onlySnd = fstSet.size() + sndSet.size() - onlyFst - 2 * union;

        return onlyFst + onlySnd;

    }

}
