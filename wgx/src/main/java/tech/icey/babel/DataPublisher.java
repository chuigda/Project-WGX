package tech.icey.babel;

public interface DataPublisher {
    int priority();

    void initialise(Masterpiece masterpiece);

    void publish();
}
