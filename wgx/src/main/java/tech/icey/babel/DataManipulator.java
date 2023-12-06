package tech.icey.babel;

public interface DataManipulator<T> {
    default Class getDataType() {
        return getClass().getGenericInterfaces()[0].getClass();
    }

    int priority();

    String dataName();

    boolean manipulateData(T data);
}
