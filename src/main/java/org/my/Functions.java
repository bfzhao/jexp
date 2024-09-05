package org.my;

import static org.my.SimpleContext.CUR_VAR_NAME;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Functions {
    public static Object abs(Value[] v) {
        if (v[0].isInteger())
            return Math.abs(v[0].asLong());
        else
            return Math.abs(v[0].asDouble());
    }

    public static Value betweenDate(Value[] v) {
        String unit = v.length > 2 && Objects.nonNull(v[2])?
                v[2].asString() : "DAYS";
        return betweenDate(v[0], v[1], unit);
    }

    public static String replaceAll(Value[] v) {
        return v[0].asString().replaceAll(v[1].asString(), v[2].asString());
    }

    public static Value uniq(IExpressionContext ctx, Value[] xs) {
        if (!xs[0].isHomogeneousVector())
            throw new Exp4jException.EvaluationException("Homogeneous Vector required");

        if (xs.length > 1 && xs[1] != null) {
            List<Value> rs = new ArrayList<>();
            Value last = null;
            for (Value v: xs[0].asVector()) {
                if (last == null) {
                    rs.add(v);
                    last = v;
                } else {
                    IExpressionContext context = ctx.makeCopy();
                    context.updateVariable("a", last);
                    context.updateVariable("b", v);
                    long r = evalExpression(xs[1], context).asLong();
                    if (r == 0) {
                        rs.set(rs.size() - 1, v);
                        last = v;
                    } else if (r > 0) {
                        rs.add(v);
                        last = v;
                    }
                    // ignore when r == 0: the value is same to last one
                }
            }
            return Value.of(rs);
        } else {
            return Value.of(Arrays.stream(xs[0].asVector())
                    .distinct()
                    .toArray(Value[]::new));
        }
    }

    public static Value sort(IExpressionContext ctx, Value[] xs) {
        if (!xs[0].isHomogeneousVector())
            throw new Exp4jException.EvaluationException("Homogeneous Vector required");

        if (xs.length > 1 && xs[1] != null) {
            return xs[0].sort((a, b) -> {
                IExpressionContext context = ctx.makeCopy();
                context.updateVariable("a", a);
                context.updateVariable("b", b);
                return (int) evalExpression(xs[1], context).asLong();
            });
        } else {
            return xs[0].sort(Value::compareTo);
        }
    }

    public static Value round(Value[] v) {
        int scale = v.length > 1 && Objects.nonNull(v[1])?
                v[1].asNumber().intValue() : 2;
        if (scale < 0) {
            throw new Exp4jException.EvaluationException("scale of round() must not be negative");
        }

        String mode = v.length > 2 && Objects.nonNull(v[2])?
                v[2].asString() : RoundingMode.HALF_UP.name();
        RoundingMode roundingMode = roundingModeMap.get(mode);
        if (Objects.isNull(roundingMode)) {
            throw new Exp4jException.EvaluationException("invalid rounding mode: " + mode);
        }

        BigDecimal decimal = new BigDecimal(v[0].toString()).setScale(scale, roundingMode);
        return Value.of(decimal.doubleValue());
    }

    public static Value reduce(IExpressionContext ctx, Value[] xs) {
        Value[] list = xs[0].asVector();
        Value id = xs[1];
        Value op = xs[2];

        IExpressionContext context = ctx.makeCopy();
        Value r = id;
        for (Value value : list) {
            context.updateVariable("_", value);
            r = evalExpression(op, context);
        }
        return Value.of(r);
    }

    public static Value[] values(Value[] value) {
        return value[0].asMap().values().toArray(new Value[0]);
    }

    public static List<Value> keys(Value[] value) {
        return value[0].asMap().keySet().stream().map(Value::of).collect(Collectors.toList());
    }

    public static Object join(Value[] xs) {
        Value left = xs[0];
        Value right = xs[1];
        List<Value> vs = joinImpl(left.asVector(), right.asVector(), xs[2].asString(), xs[3].asString());
        return Value.of(vs);
    }

    public static List<Value> take(Value[] args) {
        Value v0 = args[0];
        Value v1 = args[1];
        Value[] is = v0.asVector();
        long n = v1.asLong();
        List<Value> r = new ArrayList<>();
        for (int i = 0; i < is.length; i++, n--) {
            if (n <= 0)
                break;
            r.add(is[i]);
        }
        return r;
    }

    public static boolean regMatch(Value[] args) {
        Value v0 = args[0];
        Value v1 = args[1];

        if (v0.isNull()) {
            return false;
        }

        String regex = v1.asString().replaceAll("\\\\\\\\", "\\\\");
        try {
            return v0.asString().matches(regex);
        } catch (PatternSyntaxException e) {
            throw new Exp4jException.EvaluationException("invalid regex: " + regex);
        }
    }

    public static Value map(IExpressionContext ctx, Value[] args) {
        Value v0 = args[0];
        Value v1 = args[1];
        Value[] list = v0.asVector();
        Value[] r = new Value[list.length];
        IExpressionContext context = ctx.makeCopy();
        for (int i = 0; i < list.length; i++) {
            context.updateVariable(CUR_VAR_NAME, list[i]);
            r[i] = evalExpression(v1, context);
        }
        return Value.of(r);
    }

    public static Value[] symDiff(Value[] args) {
        Value v0 = args[0];
        Value v1 = args[1];
        List<?> list0 = Arrays.asList(v0.asVector());
        List<?> list1 = Arrays.asList(v1.asVector());
        return Stream.concat(Arrays.stream(v0.asVector()), Arrays.stream(v1.asVector()))
                .distinct()
                .filter(e -> !list0.contains(e) || !list1.contains(e))
                .toArray(Value[]::new);
    }

    public static Value[] diff(Value[] args) {
        Value v0 = args[0];
        Value v1 = args[1];
        return Arrays
                .stream(v0.asVector())
                .filter(e -> !Arrays.asList(v1.asVector()).contains(e))
                .toArray(Value[]::new);
    }

    public static Value[] intersect(Value[] args) {
        Value v0 = args[0];
        Value v1 = args[1];
        return Arrays
                .stream(v0.asVector())
                .filter(Arrays.asList(v1.asVector())::contains)
                .toArray(Value[]::new);
    }

    public static Value[] union(Value[] args) {
        Value v0 = args[0];
        Value v1 = args[1];
        return Stream
                .concat(Arrays.stream(v0.asVector()),Arrays.stream(v1.asVector()))
                .distinct()
                .toArray(Value[]::new);
    }

    public static boolean contains(Value[] args) {
        Value v0 = args[0];
        Value v1 = args[1];
        if (v1.isVector()) {
            List<?> list = Arrays.asList(v0.asVector());
            return Arrays.stream(v1.asVector()).allMatch(list::contains);
        } else {
            return Arrays.asList(v0.asVector()).contains(v1);
        }
    }

    public static Value jsonGet(IExpressionContext ctx, Value[] args) {
        Value v0 = args[0];
        Value v1 = args[1];
        IExpressionContext context = ctx.makeCopy();
        context.updateVariable(CUR_VAR_NAME, v0);
        IExpression exp = Entry.buildExpressionBuilder(v1.asString()).build();
        return exp.evaluate(context);
    }

    public static Value filter(IExpressionContext ctx, Value[] args) {
        Value v0 = args[0];
        Value v1 = args[1];
        Value[] orig = v0.asVector();
        IExpressionContext context = ctx.makeCopy();
        Value[] rest = Arrays.stream(orig).filter(v -> {
            context.updateVariable(CUR_VAR_NAME, v);
            Value r = evalExpression(v1, context);
            return r.asBoolean();
        }).toArray(Value[]::new);
        return Value.of(rest, v0.isMultiple());
    }

    public static Value sum(Value[] args) {
        Value v = args[0];
        if (v.asVector().length == 0)
            return Value.of(0);

        if (!v.isHomogeneousVector())
            throw new Exp4jException.EvaluationException("Homogeneous Vector required");

        if (v.getHomogeneousType() == Value.Type.DecimalT) {
            return Value.of(Arrays.stream(v.asVector()).filter(Value::isNotNull).mapToDouble(Value::asDouble).sum());
        } else if (v.getHomogeneousType() == Value.Type.IntegerT) {
            return Value.of(Arrays.stream(v.asVector()).filter(Value::isNotNull).mapToLong(Value::asLong).sum());
        } else {
            throw new Exp4jException.EvaluationException("number vector required");
        }
    }

    public static double avg(Value[] args) {
        Value v = args[0];
        if (v.asVector().length == 0) {
            throw new Exp4jException.EvaluationException("non-empty vector required");
        }
        double sum = sum(args).asDouble();
        return sum / v.asVector().length;
    }

    public static Value min(Value[] args) {
        Value v = args[0];
        if (!v.isHomogeneousVector())
            throw new Exp4jException.EvaluationException("Homogeneous Vector required");

        if (v.getHomogeneousType() == null)
            throw new Exp4jException.EvaluationException("non-empty vector required");

        if (v.getHomogeneousType() == Value.Type.IntegerT) {
            return Value.of(Arrays.stream(v.asVector()).mapToLong(Value::asLong).min().orElse(0));
        } else if (v.getHomogeneousType() == Value.Type.DecimalT) {
            return Value.of(Arrays.stream(v.asVector()).mapToDouble(Value::asDouble).min().orElse(0));
        } else{
            throw new Exp4jException.EvaluationException("number vector required");
        }
    }

    public static Value max(Value[] args) {
        Value v = args[0];
        if (!v.isHomogeneousVector())
            throw new Exp4jException.EvaluationException("Homogeneous Vector required");

        if (v.getHomogeneousType() == null)
            throw new Exp4jException.EvaluationException("non-empty vector required");

        if (v.getHomogeneousType() == Value.Type.IntegerT) {
            return Value.of(Arrays.stream(v.asVector()).mapToLong(Value::asLong).max().orElse(0));
        } else if (v.getHomogeneousType() == Value.Type.DecimalT) {
            return Value.of(Arrays.stream(v.asVector()).mapToDouble(Value::asDouble).max().orElse(0));
        } else {
            throw new Exp4jException.EvaluationException("number vector required");
        }
    }

    public static List<Value> joinImpl(Value[] left, Value[] right, String leftKey, String rightKey) {
        List<Value> result = new ArrayList<>();
        for (Value v1 : left) {
            Map<String, Value> lMap = v1.asMap();
            for (Value v2 : right) {
                Map<String, Value> rMap = v2.asMap();
                if (lMap.get(leftKey).equals(rMap.get(rightKey))) {
                    Map<String, Value> joinedMap = new HashMap<>();
                    joinedMap.putAll(lMap);
                    joinedMap.putAll(rMap);
                    result.add(Value.of(joinedMap));
                }
            }
        }

        return result;
    }

    public static Number toNumber(Value[] args) {
        Value value = args[0];
        switch (value.getType()) {
            case IntegerT:
            case DecimalT:
                return value.asNumber();
            case BooleanT:
                return value.asBoolean()? 1 : 0;
            case StringT:
                try {
                    String raw = value.asString();
                    String withoutComma = raw.replaceAll(",", "");
                    return Double.parseDouble(withoutComma);
                } catch (Exception e) {
                    throw new Exp4jException.EvaluationException(String.format("fail to cast string to number: %s", e.getMessage()));
                }
            default:
                throw new Exp4jException.EvaluationException(String.format("fail to cast to number, source type is %s", value.getType().toString()));
        }
    }

    public static boolean toBoolean(Value[] args) {
        Value value = args[0];
        switch (value.getType()) {
            case IntegerT:
                return value.asLong() != 0;
            case DecimalT:
                return value.asDouble() != 0;
            case BooleanT:
                return value.asBoolean();
            case StringT:
                String literal = value.asString();
                if (literal.equals("true"))
                    return true;
                else if (literal.equals("false"))
                    return false;
                else
                    throw new Exp4jException.EvaluationException(String.format("fail to cast string to boolean: %s", literal));
            default:
                throw new Exp4jException.EvaluationException(String.format("fail to cast to boolean, source type is %s", value.getType().toString()));
        }
    }

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    final private static Map<String, DateTimeFormatter> defaultFmts = new HashMap<>();
    final private static Map<String, RoundingMode> roundingModeMap = new HashMap<>();
    static {
        defaultFmts.put("yyyy-MM-dd'T'HH:mm", new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd'T'HH:mm")
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .parseDefaulting(ChronoField.MILLI_OF_SECOND, 0)
                .toFormatter());
        defaultFmts.put("yyyy-MM-dd'T'HH:mm:ss", new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                .parseDefaulting(ChronoField.MILLI_OF_SECOND, 0)
                .toFormatter());
        defaultFmts.put("yyyy-MM-dd HH:mm:ss", new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd HH:mm:ss")
                .parseDefaulting(ChronoField.MILLI_OF_SECOND, 0)
                .toFormatter());
        defaultFmts.put("yyyy/MM/dd HH:mm:ss", new DateTimeFormatterBuilder()
                .appendPattern("yyyy/MM/dd HH:mm:ss")
                .parseDefaulting(ChronoField.MILLI_OF_SECOND, 0)
                .toFormatter());
        defaultFmts.put("yyyy-MM-dd", new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd")
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .parseDefaulting(ChronoField.MILLI_OF_SECOND, 0)
                .toFormatter());
        defaultFmts.put("yyyy/MM/dd", new DateTimeFormatterBuilder()
                .appendPattern("yyyy/MM/dd")
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .parseDefaulting(ChronoField.MILLI_OF_SECOND, 0)
                .toFormatter());
        defaultFmts.put("yyyy.MM.dd", new DateTimeFormatterBuilder()
                .appendPattern("yyyy.MM.dd")
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .parseDefaulting(ChronoField.MILLI_OF_SECOND, 0)
                .toFormatter());

        roundingModeMap.put(RoundingMode.UP.name(), RoundingMode.UP);
        roundingModeMap.put(RoundingMode.DOWN.name(), RoundingMode.DOWN);
        roundingModeMap.put(RoundingMode.HALF_UP.name(), RoundingMode.HALF_UP);
        roundingModeMap.put(RoundingMode.HALF_DOWN.name(), RoundingMode.HALF_DOWN);
    }

    public static Value.DateWithFmt date(Value[] args) {
        Value value = args[0];
        String literal = value.asString();
        for (String fmt: defaultFmts.keySet()) {
            try {
                return new Value.DateWithFmt(LocalDateTime.parse(literal, defaultFmts.get(fmt)), fmt);
            } catch (DateTimeParseException e) {
                // ignore, try another fmt
            }
        }

        throw new Exp4jException.EvaluationException("fail to cast to DateTime: not matched");
    }

    public static Value.DateWithFmt fmtDate(Value[] args) {
        Value value = args[0];
        Value format = args[1];
        String literal = value.asString();
        String fmt = format.asString();

        try {
            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                    .appendPattern(fmt)
                    .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
                    .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .parseDefaulting(ChronoField.MILLI_OF_SECOND, 0)
                    .toFormatter();
            return new Value.DateWithFmt(LocalDateTime.parse(literal, formatter), fmt);
        } catch (DateTimeParseException e) {
            throw new Exp4jException.EvaluationException(String.format("fail to cast to DateTime: %s", literal));
        }
    }

    public static Value fmtDateStr(Value[] args) {
        Value value = args[0];
        Value format = args[1];
        LocalDateTime dateTime = value.asDateTime();
        String fmt = format.asString();

        try {
            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                    .appendPattern(fmt)
                    .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
                    .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .parseDefaulting(ChronoField.MILLI_OF_SECOND, 0)
                    .toFormatter();
            return Value.of(dateTime.format(formatter));
        } catch (Exception e) {
            throw new Exp4jException.EvaluationException(String.format("fail to format DateTime to String: %s", e.getMessage()));
        }
    }

    public static Value betweenDate(Value value, Value value2, String unit) {
        LocalDateTime dateTime = value.asDateTime();
        LocalDateTime dateTime2 = value2.asDateTime();

        try {
            ChronoUnit chronoUnit = ChronoUnit.valueOf(unit);
            long until = dateTime2.until(dateTime, chronoUnit);
            return Value.of(until);
        } catch (Exception e) {
            throw new Exp4jException.EvaluationException(String.format("fail to format cal betweenDate: %s", e.getMessage()));
        }
    }

    private static List<IExpression> getExpList(Value value) {
        List<IExpression> exps = new ArrayList<>();
        if (value.isVector()) {
            Value[] vs = value.asVector();
            for (Value v: vs) {
                exps.add(v.asExpression());
            }
        } else {
            exps.add(value.asExpression());
        }
        return exps;
    }

    private static Value evalExpression(Value value, IExpressionContext ctx) {
        List<IExpression> exps = getExpList(value);

        Value r = null;
        for (IExpression e: exps) {
            r = e.evaluate(ctx);
        }
        return r;
    }
}
