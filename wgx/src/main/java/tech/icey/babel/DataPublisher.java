package tech.icey.babel;

public interface DataPublisher {
    int priority();

    void initialise(Bible bible);
}
