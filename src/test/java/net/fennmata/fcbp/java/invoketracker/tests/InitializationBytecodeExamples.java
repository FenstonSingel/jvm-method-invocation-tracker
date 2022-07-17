package net.fennmata.fcbp.java.invoketracker.tests;

@SuppressWarnings({"unused", "ClassInitializerMayBeStatic"})
public class InitializationBytecodeExamples {

    private InitializationBytecodeExamples() {
        this(false);
    }

    private InitializationBytecodeExamples(boolean b) {
        constructorVariable = b;
    }

    @SuppressWarnings("CopyConstructorMissesField")
    private InitializationBytecodeExamples(InitializationBytecodeExamples orig) {
        constructorVariable = orig.constructorVariable;
    }

    @SuppressWarnings("FieldMayBeFinal")
    private int instanceVariableInitializer = 2;
    private final int finalInstanceVariableInitializer = 4;


    {
        float instanceVariable = 8;
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final boolean constructorVariable;

    @SuppressWarnings("FieldMayBeFinal")
    private static long classVariableInitializer = 10;
    private static final long finalClassVariableInitializer = 12;

    static {
        double classVariable = 16;
    }

    public static void main(String[] args) {
        int ph = 42;
    }

}
