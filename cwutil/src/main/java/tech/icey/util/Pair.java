package tech.icey.util;

import java.util.Objects;

public record Pair<T1, T2>(T1 first, T2 second) {
    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pair<?, ?> other) {
            return Objects.equals(first, other.first) &&
                   Objects.equals(second, other.second);
        }
        return false;
    }
}
