package tech.icey.bekhave;

public interface DataConsumer<T> {
    default Class getDataType() {
        return getClass().getGenericInterfaces()[0].getClass();
    }

    int priority();

    String dataName();

    void consumeData(T data);
}
