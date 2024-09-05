package org.my;

import static org.my.Exp4jException.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Operators {
    private static final Pattern DATE_TIME_OFFSET_PATTERN = Pattern.compile("(\\d+)([yMdhms])");

    private static LocalDateTime dateTimeAdd(LocalDateTime ldt, String offset, boolean add) {
        Matcher matcher = DATE_TIME_OFFSET_PATTERN.matcher(offset);
        if (matcher.matches()) {
            int n = Integer.parseInt(matcher.group(1));
            switch (matcher.group(2)) {
                case "y":
                    return ldt.plusYears(add? n : -n);
                case "M":
                    return ldt.plusMonths(add? n : -n);
                case "d":
                    return ldt.plusDays(add? n : -n);
                case "h":
                    return ldt.plusHours(add? n : -n);
                case "m":
                    return ldt.plusMinutes(add? n : -n);
                case "s":
                    return ldt.plusSeconds(add? n : -n);
            }
        }

        throw new EvaluationException(String.format("'%s' is not valid DateTime offset", offset));
    }

    private static long dateTimeDiff(LocalDateTime lhs, LocalDateTime rhs) {
        Duration duration = Duration.between(rhs, lhs); // reverse the order gives expected behaviors here
        return duration.toDays();
    }

    public static Value addition(Value lhs, Value rhs) {
        switch (lhs.getUnwrappedType()) {
            case IntegerT:
            case DecimalT:
                if (lhs.isInteger() && rhs.isInteger()) {
                    return Value.of(lhs.asNumber().longValue() + rhs.asNumber().longValue());
                } else {
                    return Value.of(lhs.asNumber().doubleValue() + rhs.asNumber().doubleValue());
                }
            case StringT:
                return Value.of(lhs.asString() + rhs.asString());
            case DataTimeT:
                return Value.of(new Value.DateWithFmt(dateTimeAdd(lhs.asDateTime(), rhs.asString(), true), lhs.getFmt()));
        }

        throw new EvaluationException(String.format("fail to add two types: %s vs %s",
                lhs.getUnwrappedType(), rhs.getUnwrappedType()));
    }

    public static Value subtraction(Value lhs, Value rhs) {
        switch (lhs.getUnwrappedType()) {
            case IntegerT:
            case DecimalT:
                if (lhs.isInteger() && rhs.isInteger()) {
                    return Value.of(lhs.asNumber().longValue() - rhs.asNumber().longValue());
                } else {
                    return Value.of(lhs.asNumber().doubleValue() - rhs.asNumber().doubleValue());
                }
            case DataTimeT:
                if (rhs.isDateTime())
                    return Value.of(dateTimeDiff(lhs.asDateTime(), rhs.asDateTime()));
                else
                    return Value.of(new Value.DateWithFmt(dateTimeAdd(lhs.asDateTime(), rhs.asString(), false), lhs.getFmt()));
        }

        throw new EvaluationException(String.format("fail to subtract two types: %s vs %s",
                lhs.getUnwrappedType(), rhs.getUnwrappedType()));
    }

    public static Value multiplication(Value lhs, Value rhs) {
        if (lhs.isInteger() && rhs.isInteger()) {
            return Value.of(lhs.asNumber().longValue() * rhs.asNumber().longValue());
        } else {
            return Value.of(lhs.asNumber().doubleValue() * rhs.asNumber().doubleValue());
        }
    }

    public static Value division(Value lhs, Value rhs) {
        return Value.of(lhs.asNumber().doubleValue() / rhs.asNumber().doubleValue());
    }

    public static Value modulo(Value lhs, Value rhs) {
        return Value.of(lhs.asNumber().doubleValue() % rhs.asNumber().doubleValue());
    }

    public static Value power(Value lhs, Value rhs) {
        return Value.of(Math.pow(lhs.asNumber().doubleValue(), rhs.asNumber().doubleValue()));
    }

    public static Value and(Value lhs, Value rhs) {
        return Value.of(lhs.asBoolean() && rhs.asBoolean());
    }

    public static Value or(Value lhs, Value rhs) {
        return Value.of(lhs.asBoolean() || rhs.asBoolean());
    }

    public static Value not(Value lhs) {
        return Value.of(!lhs.asBoolean());
    }

    public static Value compareG(Value lhs, Value rhs) {
        return Value.of(lhs.compareTo(rhs) > 0);
    }

    public static Value compareGE(Value lhs, Value rhs) {
        return Value.of(lhs.compareTo(rhs) >= 0);
    }

    public static Value compareL(Value lhs, Value rhs) {
        return Value.of(lhs.compareTo(rhs) < 0);
    }

    public static Value compareLE(Value lhs, Value rhs) {
        return Value.of(lhs.compareTo(rhs) <= 0);
    }

    public static Value compareE(Value lhs, Value rhs) {
        return Value.of(lhs.equals(rhs));
    }

    public static Value compareNE(Value lhs, Value rhs) {
        return Value.of(!lhs.equals(rhs));
    }

    public static Value compareSort(Value lhs, Value rhs) {
        return Value.of(lhs.compareTo(rhs));
    }

    public static Value dotChain(IExpressionContext ctx, Value lhs, Value rhs) {
        return null;
    }

    public static Value assign(Value lhs, Value rhs) {
        return rhs;
    }

    public static Value minus(Value lhs) {
        return Value.of(multiplication(lhs, Value.of(-1)));
    }

    public static Value concat(Value lhs, Value rhs) {
        Value[] nv = new Value[lhs.asVector().length + rhs.asVector().length];
        System.arraycopy(lhs.asVector(), 0, nv, 0, lhs.asVector().length);
        System.arraycopy(rhs.asVector(), 0, nv, lhs.asVector().length, rhs.asVector().length);
        return Value.of(nv);
    }

    public static Value plus(Value lhs) {
        return Value.of(lhs.asNumber());
    }
}
