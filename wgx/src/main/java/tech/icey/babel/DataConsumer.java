package tech.icey.babel;

public interface DataConsumer {
    void initialise(Bible bible);
    void consume(Bible bible);
}
