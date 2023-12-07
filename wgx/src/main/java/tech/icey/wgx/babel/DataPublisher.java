package tech.icey.wgx.babel;

public interface DataPublisher {
    int priority();

    void initialise(Masterpiece masterpiece);

    void publish();
}
