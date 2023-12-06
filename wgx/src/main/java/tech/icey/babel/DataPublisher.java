package tech.icey.babel;

public interface DataPublisher<T> {
    default Class getDataType() {
        return getClass().getGenericInterfaces()[0].getClass();
    }

    int priority();

    String dataName();

    T publishInitialData();

    boolean updateData();
}
