package util;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class ObjectBuilder<T> {
    private final Supplier<T> supplier;

    public ObjectBuilder(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public static <T> ObjectBuilder<T> of(Supplier<T> supplier) {
        return new ObjectBuilder<>(supplier);
    }

    public <P> ObjectBuilder<T> with(BiConsumer<T, P> consumer, P value) {
        return new ObjectBuilder<>(() -> {
            T object = supplier.get();
            consumer.accept(object, value);
            return object;
        });
    }

    public T build() {
        return supplier.get();
    }
}
