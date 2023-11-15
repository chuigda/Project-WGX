package tech.icey.cwutil;

import java.util.Objects;

public class Tuple3<T1, T2, T3> {
    public T1 first;
    public T2 second;
    public T3 third;

    public Tuple3(T1 first, T2 second, T3 third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ", " + third + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tuple3<?, ?, ?> other) {
            return Objects.equals(first, other.first) &&
                    Objects.equals(second, other.second) &&
                    Objects.equals(third, other.third);
        }
        return false;
    }
}
