package tech.icey.babel;

public interface DataManipulator {
    int priority();

    void initialise(Masterpiece masterpiece);

    void manipulate(Masterpiece masterpiece);
}
