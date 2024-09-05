package org.my;

import static org.my.Exp4jException.*;

import org.json.simple.JSONValue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Value implements Comparable<Value> {
    public static final Value TRUE = Value.of(true);
    public static final Value FALSE = Value.of(false);
    public static final Value NULL = Value.of();
    public static final String DEFAULT_DATETIME_FMT = "yyyy-MM-dd'T'HH:mm:ss";

    public enum Type { IntegerT, DecimalT, StringT, BooleanT, VectorT, EnclosedT, NullT, MapT, ExpressionT, DataTimeT }
    private final Type type;
    private final Object objectV;
    private String fmt = null;

    private boolean isHomogeneousVector = false;
    private Type homogeneousType = null;
    private boolean multiple = false; // a possible result of JSON path access

    public static class DateWithFmt {
        private final LocalDateTime localDateTime;
        private final String fmt;

        public DateWithFmt(LocalDateTime localDateTime, String fmt) {
            this.localDateTime = localDateTime;
            this.fmt = fmt;
        }

        public LocalDateTime getLocalDateTime() {
            return localDateTime;
        }

        public String getFmt() {
            return fmt;
        }
    }

    public static Value of(Value[] value, boolean multipleValue) {
        return new Value(value, multipleValue);
    }

    public static Value of() {
        return new Value();
    }

    public static Value enclosed(Value[] value) {
        return new Value(value);
    }

    public static <T> Value of(T t) {
        if (t == null)
            return Value.NULL;

        if (t instanceof Long) {
            return new Value((Long) t);
        } else if (t instanceof Integer) {
            return new Value((Integer) t);
        } else if (t instanceof Double) {
            return new Value((Double) t);
        } else if (t instanceof Float) {
            return new Value((Float) t);
        } else if (t instanceof Boolean) {
            return new Value((Boolean) t);
        } else if (t instanceof String) {
            return new Value((String) t);
        } else if (t instanceof DateWithFmt) {
            return new Value((DateWithFmt) t);
        } else if (t instanceof Value[]) {
            return new Value((Value[]) t, false);
        } else if (t instanceof List<?>) {
            List<?> list = ((List<?>) t);
            return new Value(list.stream()
                    .map(Value::of)
                    .toArray(Value[]::new), false);
        } else if (t instanceof IExpression) {
            return new Value((IExpression) t);
        } else if (t instanceof Map<?, ?>) {
            Map<String, Value> map = new HashMap<>();
            HashMap<?, ?> jsonObject = (HashMap<?, ?>) t;
            for (Object value : jsonObject.entrySet()) {
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) value;
                map.put((String) entry.getKey(), of(entry.getValue()));
            }

            return new Value(map);
        } else if (t instanceof Value) {
            return (Value) t;
        } else {
            throw new EvaluationException(String.format("%s type not supported as Value", t.getClass().getName()));
        }
    }

    public static Value of(DateWithFmt dwf) {
        return new Value(dwf);
    }

    public String getFmt() {
        return fmt;
    }

    private Value() {
        this.type = Type.NullT;
        this.objectV = null;
        this.isHomogeneousVector = false;
        this.homogeneousType = null;
        this.multiple = false;
    }

    private Value(double value) {
        this.type = Type.DecimalT;
        this.objectV = value;
    }

    private Value(long value) {
        this.type = Type.IntegerT;
        this.objectV = value;
    }

    private Value(boolean value) {
        this.type = Type.BooleanT;
        this.objectV = value;
    }

    private Value(String value) {
        this.type = Type.StringT;
        this.objectV = value;
    }

    private Value(Value[] list, boolean multiple) {
        this.type = Type.VectorT;
        this.objectV = list;
        this.multiple = multiple;
        fillListInfo();
    }

    private Value(Value[] list) {
        this.type = Type.EnclosedT;
        this.objectV = list;
    }

    private Value(IExpression exp) {
        this.type = Type.ExpressionT;
        this.objectV = exp;
    }

    private Value(Map<?, ?> map) {
        this.type = Type.MapT;
        this.objectV = map;
    }

    private Value(DateWithFmt dateWithFmt) {
        this.type = Type.DataTimeT;
        this.objectV = dateWithFmt.getLocalDateTime();
        this.fmt = dateWithFmt.getFmt();
    }

    public boolean isMultiple() {
        return this.multiple;
    }

    public Object asRawObject() {
        switch (type) {
            case VectorT:
                Value[] vs = (Value[]) objectV;
                return Arrays.stream(vs).map(Value::asRawObject).toArray();
            case MapT:
                Map<String, Object> m = new HashMap<>();
                for (Object o: ((Map<?, ?>) objectV).keySet()) {
                    m.put((String) o, ((Value) ((Map<?, ?>) objectV).get(o)).asRawObject());
                }
                return m;
            case NullT:
            case StringT:
            case BooleanT:
            case IntegerT:
            case DecimalT:
            default:
                return objectV;
        }
    }

    private Value unwrap() {
        return type == Type.EnclosedT && ((Value[]) objectV).length == 1?
                ((Value[]) objectV)[0].unwrap() : this;
    }

    private void checkType(Value v, Type ...expectedTypes) {
        boolean checked = false;
        for (Type t: expectedTypes) {
            if (v.getType() == t) {
                checked = true;
                break;
            }
        }

        if (!checked) {
            throw new EvaluationException(String.format("cannot cast '%s' to %s",
                    v.getType().toString(), Arrays.toString(expectedTypes)));
        }
    }

    public Number asNumber() {
        Value v = unwrap();
        checkType(v, Type.DecimalT, Type.IntegerT);
        return (Number) v.objectV;
    }

    public long asLong() {
        return asNumber().longValue();
    }

    public double asDouble() {
        return asNumber().doubleValue();
    }

    public Boolean asBoolean() {
        Value v = unwrap();
        checkType(v, Type.BooleanT);
        return (Boolean) v.objectV;
    }

    public String asString() {
        Value v = unwrap();
        checkType(v, Type.StringT);
        return (String) v.objectV;
    }

    public Value[] asVector() {
        Value v = unwrap();
        checkType(v, Type.VectorT);
        return (Value[]) v.objectV;
    }

    public Map<String, Value> asMap() {
        Value v = unwrap();
        checkType(v, Type.MapT);

        Map<String, Value> m = new HashMap<>();
        Map<?, ?> o = (Map<?, ?>) v.objectV;
        assert (o != null);
        for (Object x: o.keySet()) {
            m.put((String) x, (Value) o.get(x));
        }
        return m;
    }

    public LocalDateTime asDateTime() {
        Value v = unwrap();
        checkType(v, Type.DataTimeT);
        return (LocalDateTime) v.objectV;
    }

    public IExpression asExpression() {
        Value v = unwrap();
        checkType(v, Type.ExpressionT);
        return (IExpression) v.objectV;
    }

    public Type getType() {
        return type;
    }

    public Type getUnwrappedType() {
        return unwrap().getType();
    }

    public boolean isNull() {
        return unwrap().type == Type.NullT;
    }

    public boolean isNotNull() {
        return !isNull();
    }

    public boolean isVector() {
        return unwrap().type == Type.VectorT;
    }

    public boolean isNumber() {
        Value v = unwrap();
        return v.type == Type.DecimalT || v.type == Type.IntegerT;
    }

    public boolean isDecimal() {
        return unwrap().type == Type.DecimalT ;
    }

    public boolean isInteger() {
        return unwrap().type == Type.IntegerT;
    }

    public boolean isString() {
        return unwrap().type == Type.StringT;
    }

    public boolean isBoolean() {
        return unwrap().type == Type.BooleanT;
    }

    public boolean isMap() {
        return unwrap().type == Type.MapT;
    }

    public boolean isExpression() {
        return unwrap().type == Type.ExpressionT;
    }

    public boolean isDateTime() {
        return unwrap().type == Type.DataTimeT;
    }

    public Boolean isHomogeneousVector() { return unwrap().isHomogeneousVector; }

    public Type getHomogeneousType() { return unwrap().homogeneousType; }

    @Override
    public String toString() {
        Value v = unwrap();
        if (isVector()) {
            assert (v.objectV != null);
            StringBuilder buffer = new StringBuilder();
            if (!isMultiple()) {
                buffer.append('[');
            }

            boolean first = true;
            for (Object d: (Object[]) v.objectV ) {
                if (first) {
                    first = false;
                } else {
                    buffer.append(", ");
                }

                buffer.append(d.toString());
            }

            if (!isMultiple()) {
                buffer.append(']');
            }
            return buffer.toString();
        } else if (isMap()) {
            assert (v.objectV != null);
            StringBuilder buffer = new StringBuilder();
            buffer.append('{');
            boolean first = true;
            Map<?, ?> m = (Map<?, ?>)v.objectV;
            for (Object k: m.keySet()) {
                if (first) {
                    first = false;
                } else {
                    buffer.append(", ");
                }

                buffer.append(JSONValue.toJSONString(k));
                buffer.append(": ");
                buffer.append(m.get(k).toString());
            }
            buffer.append('}');
            return buffer.toString();
        } else if (isDateTime()) {
            String rep = ((LocalDateTime) v.objectV).format(DateTimeFormatter.ofPattern(getFmt()));
            return "\"" + rep + "\"";
        } else if (isBoolean()) {
            return (Boolean) v.objectV? "true" : "false";
        } else if (isString()) {
            return JSONValue.toJSONString(v.objectV);
        } else if (v.objectV == null) {
            return "null";
        } else {
            return v.objectV.toString();
        }
    }

    public Value sort(Comparator<Value> func) {
        Value[] vs = this.asVector();
        Arrays.sort(vs, func == null? Value::compareTo : func);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        Value value = (Value) o;
        if (type != value.type)
            return false;

        if (type == Type.VectorT || type == Type.MapT) {
            if (type == Type.VectorT) {
                Value[] ov = (Value[]) value.objectV;
                for (int i = 0; i < ov.length; ++i) {
                    if (!ov[i].equals(((Value[])objectV)[i]))
                        return false;
                }
            } else {
                Map<?, ?> om = (Map<?, ?>) value.objectV;
                for (Object k: om.keySet()) {
                    if (!((Map<?, ?>) objectV).containsKey(k) || !om.get(k).equals(((Map<?, ?>) objectV).get(k)))
                        return false;
                }
            }
            return true;
        } else {
            return (objectV != null && objectV.equals(value.objectV)
                    || objectV == value.objectV);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, objectV);
    }

    @Override
    public int compareTo(Value o) {
        switch (this.unwrap().getType()) {
            case NullT:
                return 0;
            case BooleanT:
                if (this.asBoolean())
                    return o.asBoolean()? 0 : 1;
                else
                    return o.asBoolean()? -1 : 0;
            case IntegerT:
            case DecimalT:
                Number lhs = this.asNumber();
                Number rhs = o.asNumber();
                if ((lhs instanceof Integer || lhs instanceof Long)
                        && (rhs instanceof Integer || rhs instanceof Long))
                    return Long.compare(lhs.longValue(), rhs.longValue());
                else
                    return Double.compare(lhs.doubleValue(), rhs.doubleValue());
            case StringT:
                return this.asString().compareTo(o.asString());
            case DataTimeT:
                return this.asDateTime().compareTo(o.asDateTime());
            default:
                throw new EvaluationException(String.format("'%s' and '%s' is not comparable",
                        this.getType().toString(), o.getType().toString()));
        }
    }

    private boolean typeEquivalent(Type a, Type b) {
        return a == Type.NullT || b == Type.NullT || a == b
                || (a == Type.IntegerT && b == Type.DecimalT)
                || (a == Type.DecimalT && b == Type.IntegerT);
    }

    private Type compatibleType(Type a, Type b) {
        if ((a == Type.IntegerT && b == Type.DecimalT)
                || (a == Type.DecimalT && b == Type.IntegerT))
            return Type.DecimalT;
        else
            return a;
    }

    private void fillListInfo() {
        boolean homogeneous = true;
        Type type = null;
        for (Object o: (Object[]) objectV) {
            Value v = (Value) o;
            assert(v != null);
            if (type == null)
                type = v.type;
            else {
                if (homogeneous) {
                    homogeneous = typeEquivalent(type, v.type);
                    type = compatibleType(type, v.type);
                }
            }
        }

        this.isHomogeneousVector = homogeneous;
        this.homogeneousType = homogeneous? type : null;
    }
}
