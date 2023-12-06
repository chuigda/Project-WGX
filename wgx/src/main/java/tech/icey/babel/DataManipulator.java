package tech.icey.babel;

public interface DataManipulator {
    int priority();

    void initialise(Bible bible);

    void manipulate(Bible bible);
}
