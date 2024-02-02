package io.github.aratakileo.emogg.util;

public class IdentityWrapper<T> {
    public final T value;

    public IdentityWrapper(T value) {
        this.value = value;
    }

    protected T getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IdentityWrapper<?> other)
            return other.getValue() == getValue();
        return false;
    }

    @Override
    public String toString() {
        return getValue().toString();
    }

    // The value field in the original class doesn't matter too much,
    // since this class isn't meant to be instantiated all the time.
    public static class Mutable<MT> extends IdentityWrapper<MT> {
        public MT mutValue = null;

        public Mutable(MT value) {
            super(null);
            this.mutValue = value;
        }

        public Mutable() {
            super(null);
        }

        @Override
        protected MT getValue() {
            return mutValue;
        }
    }
}
