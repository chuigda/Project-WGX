package tech.icey.wgx.babel;

public interface DataConsumer {
    void initialise(Masterpiece masterpiece);
    void consume(Masterpiece masterpiece);
}
