package de.lisa.backend.common.numbers;

import java.math.BigDecimal;

public interface INumberConverter {
    @SuppressWarnings("unchecked")
    default <N extends Number> N convertValue(int value, Class<N> type) {
        if (type.equals(Integer.class) || type.equals(int.class)) {
            return (N) Integer.valueOf(value);
        } else if (type.equals(Long.class) || type.equals(long.class)) {
            return (N) Long.valueOf(value);
        } else if (type.equals(BigDecimal.class)) {
            return (N) BigDecimal.valueOf(value);
        } else if (type.equals(Double.class) || type.equals(double.class)) {
            return (N) Double.valueOf((double) value);
        } else if (type.equals(Float.class) || type.equals(float.class)) {
            return (N) Float.valueOf((float) value);
        } else if (type.equals(Short.class) || type.equals(short.class)) {
            return (N) Short.valueOf((short) value);
        }

        throw new IllegalArgumentException("Unsupported number type: " + type.getName());
    }
}
