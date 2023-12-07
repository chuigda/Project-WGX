package tech.icey.util;

import java.util.Objects;

public class Tuple4<T1, T2, T3, T4> {
    public T1 first;
    public T2 second;
    public T3 third;
    public T4 fourth;

    public Tuple4(T1 first, T2 second, T3 third, T4 fourth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ", " + third + ", " + fourth + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tuple4<?, ?, ?, ?> other) {
            return Objects.equals(first, other.first) &&
                    Objects.equals(second, other.second) &&
                    Objects.equals(third, other.third) &&
                    Objects.equals(fourth, other.fourth);
        }
        return false;
    }
}
