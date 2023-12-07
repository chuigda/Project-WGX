package tech.icey.rfc6455;

import tech.icey.util.Either;

public interface RFC6455Callback {
    void onData(Either<byte[], String> data);

    default void onError(Exception e) {}
}
