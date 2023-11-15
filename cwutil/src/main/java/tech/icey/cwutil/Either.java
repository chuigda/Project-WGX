package tech.icey.cwutil;

public abstract sealed class Either<L, R> {
    public static <L, R> Either<L, R> left(L value) {
        return new Left<>(value);
    }

    public static <L, R> Either<L, R> right(R value) {
        return new Right<>(value);
    }
}

final class Left<L, R> extends Either<L, R> {
    public final L value;

    public Left(L value) {
        this.value = value;
    }
}

final class Right<L, R> extends Either<L, R> {
    public final R value;

    public Right(R value) {
        this.value = value;
    }
}
