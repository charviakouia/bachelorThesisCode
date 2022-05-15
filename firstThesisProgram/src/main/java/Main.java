
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;

public class Main {

    public static void main(String[] args){

        JUnitCore jUnitCore = new JUnitCore();
        jUnitCore.addListener(new TextListener(System.out));
        jUnitCore.run(ExampleClassTest.class);

    }

}
