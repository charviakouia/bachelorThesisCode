package ivansCode.utils;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class CustomAgent {

    private static Instrumentation instrumentation;

    public static void premain(final String agentArgs, final Instrumentation inst){
        instrumentation = inst;
    }

    public static void agentmain(final String agentArguments, final Instrumentation inst){
        instrumentation = inst;
    }

    public static boolean introduceMutation(final Class<?> toBeReplacedCls, final byte[] bytes){
        final ClassDefinition definition = new ClassDefinition(toBeReplacedCls, bytes);
        try {
            instrumentation.redefineClasses(definition);
            return true;
        } catch (ClassNotFoundException | UnmodifiableClassException | VerifyError | InternalError e){
            e.printStackTrace();
            return false;
        }
    }

}
