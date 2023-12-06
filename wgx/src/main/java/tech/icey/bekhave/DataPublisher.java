package tech.icey.bekhave;

public interface DataPublisher<T> {
    String dataName();

    T publishInitialData();

    boolean updateData();
}
