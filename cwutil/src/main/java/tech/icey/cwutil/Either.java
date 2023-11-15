package tech.icey.cwutil;

public abstract sealed class Either<L, R> {}

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
