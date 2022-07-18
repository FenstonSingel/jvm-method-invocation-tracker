package net.fennmata.fcbp.java.invoketracker.testbed;

class Superclass {

    public Superclass() {

    }

    @SuppressWarnings("unused")
    public Superclass(boolean b) {

    }

    @SuppressWarnings("unused")
    public Superclass(Superclass orig) {

    }

}

public class SuperclassConstructorExamples extends Superclass {

    @SuppressWarnings("unused")
    public SuperclassConstructorExamples(int i) {

    }

    public SuperclassConstructorExamples() {
        super();
    }

    public SuperclassConstructorExamples(boolean b) {
        super(b);
    }

    public SuperclassConstructorExamples(SuperclassConstructorExamples orig) {
        super(orig);
    }

    public static void main(String[] args) {
        var orig = new SuperclassConstructorExamples(42);
        new SuperclassConstructorExamples();
        new SuperclassConstructorExamples(true);
        new SuperclassConstructorExamples(orig);
    }

}
