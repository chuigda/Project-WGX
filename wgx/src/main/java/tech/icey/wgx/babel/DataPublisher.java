package tech.icey.wgx.babel;

public interface DataPublisher {
    int publisherInitPriority();

    void initialise(Masterpiece masterpiece);

    void publish();
}
