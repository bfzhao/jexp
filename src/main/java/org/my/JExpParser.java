package org.my;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.my.SimpleContext.CUR_VAR_NAME;

public class JExpParser {
    static class SimpleExpression implements IExpression {
        final JExpParser.Node node;

        public SimpleExpression(JExpParser.Node node) {
            this.node = node;
        }

        @Override
        public Future<Value> evaluateAsync(ExecutorService executor, IExpressionContext context) {
            return executor.submit(() -> evaluate(context));
        }

        @Override
        public Value evaluate(IExpressionContext context) {
            return node.eval((SimpleContext) context);
        }

        @Override
        public Value evaluate() {
            return evaluate(new SimpleContext());
        }

        public String dump() {
            return node.dump();
        }
    }

    static class SimpleBuilder implements IExpressionBuilder {
        private final JExpParser parser;

        public SimpleBuilder(String expression) {
            parser = new JExpParser(expression.toCharArray(), 0, false);
        }

        public SimpleBuilder(String expression, boolean optimize) {
            parser = new JExpParser(expression.toCharArray(), 0, optimize);
        }

        @Override
        public IExpression build() {
            List<IExpression> exps = buildAll();
            return exps.isEmpty() ? null : exps.get(0);
        }

        @Override
        public List<IExpression> buildAll() {
            return parser.parse(false).stream().map(SimpleExpression::new).collect(Collectors.toList());
        }
    }

    static class Pair<T, K> {
        final T first;
        final K second;
        Pair(T t, K k) {
            this.first = t;
            this.second = k;
        }

        static <T, K> Pair<T, K> of(T t, K k) {
            return new Pair<>(t, k);
        }
    }

    static class Extra {
        public static Value fact(Value v0) {
            return Value.of(fact(v0.asLong()));
        }

        private static long fact(long n) {
            if (n < 0)
                throw new Exp4jException.EvaluationException("negative number for fact");
            return n == 1 ? 1 : n * fact(n - 1);
        }

        public static Value add(Value[] vs) {
            long n = 0;
            for (Value v : vs) {
                n += v.asLong();
            }
            return Value.of(n);
        }
    }

    // Syntax structure
    interface Node {
        String dump();

        Value eval(SimpleContext ctx);
    }

    static class SimpleValueNode implements Node {
        final Value value;

        private SimpleValueNode(Value value) {
            this.value = value;
        }

        @Override
        public Value eval(SimpleContext ctx) {
            return value;
        }

        @Override
        public String dump() {
            return value.toString();
        }
    }

    static class ListValueNode implements Node {
        final List<Node> nodes;

        private ListValueNode(List<Node> nodes) {
            this.nodes = nodes;
        }

        @Override
        public Value eval(SimpleContext ctx) {
            return Value.of(nodes.stream().map(x -> x.eval(ctx)).toArray(Value[]::new));
        }

        @Override
        public String dump() {
            return "[" + nodes.stream().map(Node::dump).collect(Collectors.joining(",")) + "]";
        }
    }

    static class MapValueNode implements Node {
        final Map<String, Node> nodeMap;

        private MapValueNode(Map<String, Node> nodeMap) {
            this.nodeMap = nodeMap;
        }

        @Override
        public Value eval(SimpleContext ctx) {
            return Value.of(nodeMap.keySet().stream().collect(Collectors.toMap(k -> k, k -> nodeMap.get(k).eval(ctx))));
        }

        @Override
        public String dump() {
            return "{" + nodeMap.keySet().stream().map(x -> "\"" + x + "\":" + nodeMap.get(x).dump()).collect(Collectors.joining(",")) + "}";
        }
    }

    static class ExpValueNode implements Node {
        static class JExp extends SimpleExpression {
            final ExpValueNode node;

            JExp(ExpValueNode node) {
                super(node);
                this.node = node;
            }

            @Override
            public Value evaluate(IExpressionContext context) {
                return Arrays.stream(node.nodes)
                        .map(n -> n.eval((SimpleContext) context))
                        .collect(Collectors.toList())
                        .get(node.nodes.length - 1);
            }
        }

        final Node[] nodes;

        private ExpValueNode(Node[] nodeMap) {
            this.nodes = nodeMap;
        }

        @Override
        public Value eval(SimpleContext ctx) {
            return Value.of(new JExp(this));
        }

        @Override
        public String dump() {
            return "@{" + Arrays.stream(nodes).map(Node::dump).collect(Collectors.joining(";")) + "}";
        }
    }

    static class TemplateValueNode implements Node {
        final List<Node> nodes;

        private TemplateValueNode(List<Node> nodes) {
            this.nodes = nodes;
        }

        @Override
        public Value eval(SimpleContext ctx) {
            StringBuilder sb = new StringBuilder();
            for (Node n: nodes) {
                Value v = n.eval(ctx);
                if (v.isExpression()) {
                    v = v.asExpression().evaluate(ctx);
                }
                sb.append(v.isString()? v.asString() : v.toString());
            }
            return Value.of(sb.toString());
        }

        @Override
        public String dump() {
            return "`" + nodes.stream().map(Node::dump).collect(Collectors.joining("")) + "`";
        }
    }

    static class JsonPathValueNode implements Node {
        static class PathAccessor {
            private final boolean root;
            private final String property;
            private final Integer index;
            private final ExpValueNode filter;

            PathAccessor() { this(true, null, null, null); }
            PathAccessor(String property) { this(false, property, null, null); }
            PathAccessor(Integer index, ExpValueNode filter) { this(false, null, index, filter); }
            PathAccessor(boolean root, String property, Integer index, ExpValueNode filter) {
                this.root = root;
                this.property = property;
                this.index = index;
                this.filter = filter;
            }

            public String dump() {
                if (property != null) {
                    String norm = property
                            .replaceAll("\t", "\\t")
                            .replaceAll("\n", "\\n")
                            .replaceAll("\r", "\\r")
                            .replaceAll("\"", "\\\"");
                    return ".\"" + norm + '"';
                } else {
                    if (index != null)
                        return '[' + String.valueOf(index) + ']';
                    else if (filter != null)
                        return '[' + filter.toString() + ']';
                    else
                        return "[]";
                }
            }

            public boolean eval(SimpleContext ctx, List<Value> target) {
                // the context of the JSON path
                boolean multiValue = false;
                List<Value> tmp = new ArrayList<>();
                for (Value o : target) {
                    if (o.isNull()) {
                        tmp.add(Value.NULL);
                    } else if (root) {
                        tmp.add(o);
                    } else if (property != null) {
                        tmp.add(o.asMap().getOrDefault(property, Value.NULL));
                    } else {
                        Value[] array = o.asVector();
                        if (index != null) {
                            int idx = index;
                            if (idx < 0) {
                                idx = array.length + idx;
                            }
                            // zero based integer index
                            if (idx >= 0 && idx < array.length) {
                                tmp.add(array[idx]);
                            } else {
                                tmp.add(Value.NULL);
                            }
                        } else if (filter != null) {
                            // predicate based filter
                            SimpleContext context = new SimpleContext(ctx);
                            for (Value v : array) {
                                context.updateVariable(CUR_VAR_NAME, v);
                                Value exp = filter.eval(ctx);
                                if (exp.asExpression().evaluate(context).asBoolean()) {
                                    tmp.add(v);
                                }
                            }
                            multiValue = true;
                        } else {
                            // full access
                            tmp.addAll(Arrays.asList(array));
                            multiValue = true;
                        }
                    }
                }
                target.clear();
                target.addAll(tmp);
                return multiValue;
            }
        }

        final Node object;
        final List<PathAccessor> nodes;

        public JsonPathValueNode(Node object, List<PathAccessor> nodes) {
            this.object = object;
            this.nodes = nodes;
        }

        @Override
        public Value eval(SimpleContext ctx) {
            List<Value> tmp = new ArrayList<>();
            tmp.add(ctx.getVariable(object == null? CUR_VAR_NAME : object.eval(ctx).asString()));

            boolean multiValue = false;
            for (PathAccessor n: nodes) {
                if (n.eval(ctx, tmp) && !multiValue)
                    multiValue = true;
            }

            if (multiValue) {
                return Value.of(tmp.toArray(new Value[0]), true);
            } else {
                assert (tmp.size() == 1);
                return tmp.get(0);
            }
        }

        @Override
        public String dump() {
            StringBuilder rep = new StringBuilder("${");
            if (object != null)
                rep.append(object.dump());
            for (PathAccessor n: nodes) {
                rep.append(n.dump());
            }
            rep.append("}");
            return rep.toString();
        }
    }

    static class OpNode implements Node {
        Operator operator;
        private final Node[] nodes;

        public OpNode(Node lhs, Operator operator, Node rhs) {
            assert operator.operands == 2;
            this.operator = operator;
            this.nodes = new Node[2];
            this.nodes[0] = lhs;
            this.nodes[1] = rhs;
        }

        public OpNode(Operator operator, Node lhs) {
            assert operator.operands == 1;
            this.operator = operator;
            this.nodes = new Node[1];
            this.nodes[0] = lhs;
        }

        private Value apply(IExpressionContext ctx, Value v0, Value v1) {
            return callWrapper(ctx, v0, v1);
        }

        private Value callWrapper(IExpressionContext ctx, Value... vs) {
            return this.operator.operation != null ?
                    operator.operation.apply(vs) :
                    operator.operationWithContext.apply(ctx, vs);
        }

        @Override
        public Value eval(SimpleContext ctx) {
            if (this.operator.operands == 1) {
                Value x = nodes[0].eval(ctx);
                return operator.scalable && x.isVector() ?
                            Value.of(Arrays.stream(x.asVector())
                                    .map(i -> apply(ctx, i, null))
                                    .toArray(Value[]::new))
                            : apply(ctx, x, null);
            } else {
                switch (operator.op) {
                    case "=": {
                        if (nodes[0] instanceof NameNode) {
                            Value r = apply(ctx, nodes[0].eval(ctx), nodes[1].eval(ctx));
                            ctx.updateVariable(((NameNode) nodes[0]).name, r);
                            return r;
                        } else {
                            throw new Exp4jException.EvaluationException("try to assign value to non-variable name");
                        }
                    }
                    case ".": {
                        if (nodes[1] instanceof FuncNode) {
                            FuncNode fn = (FuncNode) nodes[1];
                            List<Node> fullArgs = new ArrayList<>();
                            fullArgs.add(nodes[0]);
                            fullArgs.addAll(Arrays.asList(fn.nodes));
                            return fn.eval(ctx, fullArgs.toArray(new Node[0]));
                        } else {
                            throw new Exp4jException.EvaluationException("second arg of '.' operator must be a function");
                        }
                    }
                    default: {
                        Value x = nodes[0].eval(ctx);
                        Value y = nodes[1].eval(ctx);
                        if (operator.scalable) {
                            if (x.isVector() && !y.isVector()) {
                                return Value.of(Arrays
                                        .stream(x.asVector())
                                        .map(i -> apply(ctx, i, y))
                                        .map(Value::of)
                                        .toArray(Value[]::new));
                            } else if (!x.isVector() && y.isVector()) {
                                return Value.of(Arrays
                                        .stream(y.asVector())
                                        .map(i -> apply(ctx, x, i))
                                        .map(Value::of)
                                        .toArray(Value[]::new));
                            } else if (x.isVector() && y.isVector()) {
                                if (x.asVector().length != y.asVector().length)
                                    throw new Exp4jException.EvaluationException("the length of vectors of cross broadcast must be same");
                                else {
                                    return Value.of(IntStream.range(0, x.asVector().length)
                                            .mapToObj(i -> apply(ctx, x.asVector()[i], y.asVector()[i]))
                                            .map(Value::of)
                                            .toArray(Value[]::new));
                                }
                            }
                        }

                        // not scalable or x and y are not vector both
                        return apply(ctx, x, y);
                    }
                }
            }
        }

        @Override
        public String dump() {
            if (this.operator.operands == 1) {
                if (operator.suffix == Operator.Suffix.PREFIX)
                    return "(" + operator.op + nodes[0].dump() + ")";
                else
                    return "(" + nodes[0].dump() + operator.op + ")";
            } else {
                return "(" + nodes[0].dump() + operator.op + nodes[1].dump() + ")";
            }
        }
    }

    static class NameNode implements Node {
        final String name;
        final Node[] nodes;

        NameNode(String name) {
            this(name, null);
        }

        NameNode(String name, Node[] nodes) {
            this.name = name;
            this.nodes = nodes;
        }

        @Override
        public String dump() {
            if (nodes == null) {
                return name;
            } else {
                return name + "(" + Arrays.stream(nodes).map(Node::dump).collect(Collectors.joining(",")) + ")";
            }
        }

        @Override
        public Value eval(SimpleContext ctx) {
            if (nodes != null) {
                List<Value> args = Arrays.stream(nodes).map(v -> v.eval(ctx)).collect(Collectors.toList());
                return evalCustomFunc(ctx, ((ExpValueNode.JExp) ctx.getVariable(name).asExpression()).node, args);
            } else {
                return ctx.getVariable(name);
            }
        }

        private static Value evalCustomFunc(SimpleContext ctx, ExpValueNode ev, List<Value> args) {
            if (args.size() > 1) {
                throw new Exp4jException.EvaluationException("only 1 args supported");
            }

            SimpleContext sc = new SimpleContext(ctx);
            for (Value m : args) {
                sc.updateVariable("_", m);
            }

            Value r = Value.NULL;
            for (Node n : ev.nodes) {
                r = n.eval(sc);
            }
            return r;
        }
    }

    static class FuncNode implements Node {
        final JExpFunction<?> func;
        final Node[] nodes;

        FuncNode(JExpFunction<?> func, Node[] nodes) {
            this.func = func;
            this.nodes = nodes;
        }

        @Override
        public String dump() {
            return func.name + "(" + Arrays.stream(nodes).map(Node::dump).collect(Collectors.joining(",")) + ")";
        }

        @Override
        public Value eval(SimpleContext ctx) {
            return eval(ctx, nodes);
        }

        public Value eval(SimpleContext ctx, Node[] nodes) {
            if (func.args != -1 && (nodes.length > func.args + func.optArgs || nodes.length < func.args)) {
                throw new Exp4jException.EvaluationException("invalid argument count");
            }

            if (func.func != null)
                return bindAll(func.func, func.scalable, ctx, nodes);
            else
                return bindAll(x -> func.funcWithCtx.apply(ctx, x), func.scalable, ctx, nodes);
        }

        public static Value bindAll(Function<Value[], ?> function, boolean scalable, SimpleContext ctx, Node... nodes) {
            Value[] args = new Value[nodes.length];
            int i = 0;
            for (Node n : nodes) {
                args[i++] = n.eval(ctx);
            }

            if (scalable && args.length > 0 && args[0].isVector()) {
                Value[] vs = args[0].asVector();
                Value[] r = new Value[vs.length];
                i = 0;
                for (Value v : vs) {
                    args[0] = v;
                    r[i++] = Value.of(function.apply(args));
                }

                return Value.of(r);
            } else {
                return Value.of(function.apply(args));
            }
        }
    }

    enum Type { Number, Punctuation, Boolean, Null, String, Template, Name, EOL }

    static class Token {
        private final String term;
        private final Type type;
        private final int pos;
        private final Value value;

        public Token(String term, Type type, int pos, Value value) {
            this.term = term;
            this.type = type;
            this.pos = pos;
            this.value = value;
        }

        boolean isTerm(String term) {
            return this.term.equals(term);
        }
    }

    static class Operator {
        enum Associative {LEFT, RIGHT}

        enum Suffix { PREFIX, POSTFIX, INFIX }

        final String op;
        final int operands;
        final Associative associative;
        final Suffix suffix;
        final boolean scalable;
        final Function<Value[], Value> operation;
        final BiFunction<IExpressionContext, Value[], Value> operationWithContext;
        final int refer;

        Operator(String op, int operands, Associative associative, Suffix suffix, boolean scalable, Function<Value[], Value> operation, int refer) {
            this(op, operands, associative, suffix, scalable, operation, null, refer);
        }

        Operator(String op, int operands, Associative associative, Suffix suffix, boolean scalable, BiFunction<IExpressionContext, Value[], Value> operationWithContext, int refer) {
            this(op, operands, associative, suffix, scalable, null, operationWithContext, refer);
        }

        private Operator(String op, int operands, Associative associative, Suffix suffix, boolean scalable, Function<Value[], Value> operation, BiFunction<IExpressionContext, Value[], Value> operationWithContext, int refer) {
            this.op = op;
            this.operands = operands;
            this.associative = associative;
            this.suffix = suffix;
            this.scalable = scalable;
            this.operation = operation;
            this.operationWithContext = operationWithContext;
            this.refer = refer;

            if (operands == 1) {
                assert suffix == Suffix.PREFIX && associative == Associative.RIGHT
                        || suffix == Suffix.POSTFIX && associative == Associative.LEFT;
            } else {
                assert suffix == Suffix.INFIX;
            }
        }
    }

    static class JExpFunction<R> {
        final String name;
        final int args;
        final int optArgs;
        final boolean scalable;
        final Function<Value[], R> func;
        final BiFunction<IExpressionContext, Value[], R> funcWithCtx;

        JExpFunction(String name, int args, int optArgs, boolean scalable, Function<Value[], R> func) {
            this(name, args, optArgs, scalable, func, null);
        }

        JExpFunction(String name, int args, int optArgs, boolean scalable, BiFunction<IExpressionContext, Value[], R> funcWithCtx) {
            this(name, args, optArgs, scalable, null, funcWithCtx);
        }

        private JExpFunction(String name, int args, int optArgs, boolean scalable, Function<Value[], R> func, BiFunction<IExpressionContext, Value[], R> funcWithCtx) {
            this.name = name;
            this.args = args;
            this.optArgs = optArgs;
            this.scalable = scalable;
            this.func = func;
            this.funcWithCtx = funcWithCtx;
        }
    }

    // region define all build-in functions
    private static final JExpFunction<?>[] functions = {
            new JExpFunction<>("sin", 1, 0, true, vs -> Math.sin(vs[0].asDouble())),
            new JExpFunction<>("cos", 1, 0, true, vs -> Math.cos(vs[0].asDouble())),
            new JExpFunction<>("tan", 1, 0, true, vs -> Math.tan(vs[0].asDouble())),
            new JExpFunction<>("cot", 1, 0, true, vs -> 1d / Math.tan(vs[0].asDouble())),
            new JExpFunction<>("log", 1, 0, true, vs -> Math.log(vs[0].asDouble())),
            new JExpFunction<>("log2", 1, 0, true, vs -> Math.log(vs[0].asDouble()) / Math.log(2d)),
            new JExpFunction<>("log10", 1, 0, true, vs -> Math.log10(vs[0].asDouble())),
            new JExpFunction<>("log1p", 1, 0, true, vs -> Math.log1p(vs[0].asDouble())),
            new JExpFunction<>("abs", 1, 0, true, Functions::abs),
            new JExpFunction<>("acos", 1, 0, true, vs -> Math.acos(vs[0].asDouble())),
            new JExpFunction<>("asin", 1, 0, true, vs -> Math.asin(vs[0].asDouble())),
            new JExpFunction<>("atan", 1, 0, true, vs -> Math.atan(vs[0].asDouble())),
            new JExpFunction<>("cbrt", 1, 0, true, vs -> Math.cbrt(vs[0].asDouble())),
            new JExpFunction<>("floor", 1, 0, true, vs -> Math.floor(vs[0].asDouble())),
            new JExpFunction<>("sinh", 1, 0, true, vs -> Math.sinh(vs[0].asDouble())),
            new JExpFunction<>("sqrt", 1, 0, true, vs -> Math.sqrt(vs[0].asDouble())),
            new JExpFunction<>("tanh", 1, 0, true, vs -> Math.tanh(vs[0].asDouble())),
            new JExpFunction<>("cosh", 1, 0, true, vs -> Math.cosh(vs[0].asDouble())),
            new JExpFunction<>("ceil", 1, 0, true, vs -> Math.ceil(vs[0].asDouble())),
            new JExpFunction<>("pow", 2, 0, true, vs -> Math.pow(vs[0].asDouble(), vs[1].asDouble())),
            new JExpFunction<>("exp", 1, 0, true, vs -> Math.exp(vs[0].asDouble())),
            new JExpFunction<>("expm1", 1, 0, true, vs -> Math.expm1(vs[0].asDouble())),
            new JExpFunction<>("signum", 1, 0, true, vs -> Math.signum(vs[0].asNumber().doubleValue())),
            new JExpFunction<>("csc", 1, 0, true, vs -> 1d / Math.sin(vs[0].asDouble())),
            new JExpFunction<>("sec", 1, 0, true, vs -> 1d / Math.cos(vs[0].asDouble())),
            new JExpFunction<>("csch", 1, 0, true, vs -> 1d / Math.sinh(vs[0].asDouble())),
            new JExpFunction<>("sech", 1, 0, true, vs -> 1d / Math.cosh(vs[0].asDouble())),
            new JExpFunction<>("coth", 1, 0, true, vs -> Math.cosh(vs[0].asDouble()) / Math.sinh(vs[0].asDouble())),
            new JExpFunction<>("logb", 2, 0, true, vs -> Math.log(vs[1].asDouble()) / Math.log(vs[0].asDouble())),
            new JExpFunction<>("toRadian", 1, 0, true, vs -> Math.toRadians(vs[0].asDouble())),
            new JExpFunction<>("toDegree", 1, 0, true, vs -> Math.toDegrees(vs[0].asDouble())),
            new JExpFunction<>("max", 1, 0, false, Functions::max),
            new JExpFunction<>("min", 1, 0, false, Functions::min),
            new JExpFunction<>("avg", 1, 0, false, Functions::avg),
            new JExpFunction<>("sum", 1, 0, false, Functions::sum),
            new JExpFunction<>("toString", 1, 0, true, v -> v[0].toString()),
            new JExpFunction<>("toNumber", 1, 0, true, Functions::toNumber),
            new JExpFunction<>("toBoolean", 1, 0, true, Functions::toBoolean),
            new JExpFunction<>("now", 0, 0, false, x -> new Value.DateWithFmt(Functions.now(), Value.DEFAULT_DATETIME_FMT)),
            new JExpFunction<>("rand", 0, 0, true, x -> Math.random()),
            new JExpFunction<>("toDate", 1, 0, true, Functions::date),
            new JExpFunction<>("toDateFmt", 2, 0, true, Functions::fmtDate),
            new JExpFunction<>("formatDate", 2, 0, true, Functions::fmtDateStr),
            new JExpFunction<>("length", 1, 0, false, v -> v[0].asVector().length),
            new JExpFunction<>("choice", 3, 0, false, v -> v[0].asBoolean() ? v[1] : v[2]),
            new JExpFunction<>("filter", 2, 0, false, Functions::filter),
            new JExpFunction<>("jsonGet", 2, 0, false, Functions::jsonGet),
            new JExpFunction<>("count", 1, 0, false, v -> v[0].asVector().length),
            new JExpFunction<>("contains", 2, 0, false, Functions::contains),
            new JExpFunction<>("union", 2, 0, false, Functions::union),
            new JExpFunction<>("intersect", 2, 0, false, Functions::intersect),
            new JExpFunction<>("diff", 2, 0, false, Functions::diff),
            new JExpFunction<>("symDiff", 2, 0, false, Functions::symDiff),
            new JExpFunction<>("concat", -1, 0, false, v -> v),
            new JExpFunction<>("map", 2, 0, false, Functions::map),
            new JExpFunction<>("reduce", 3, 0, false, Functions::reduce),   // FIXME
            new JExpFunction<>("regMatch", 2, 0, true, Functions::regMatch),
            new JExpFunction<>("take", 2, 0, false, Functions::take),
            new JExpFunction<>("keys", 1, 0, true, Functions::keys),
            new JExpFunction<>("values", 1, 0, true, Functions::values),
            new JExpFunction<>("join", 4, 0, false, Functions::join),
            new JExpFunction<>("round", 1, 2, true, Functions::round),
            new JExpFunction<>("sort", 1, 1, false, Functions::sort),
            new JExpFunction<>("uniq", 1, 1, false, Functions::uniq),
            new JExpFunction<>("betweenDate", 2, 1, true, Functions::betweenDate),
            new JExpFunction<>("replaceAll", 3, 0, true, Functions::replaceAll),
            new JExpFunction<>("add", 5, 0, false, Extra::add),
    };

    private static final Map<String, JExpFunction<?>> funcs = new HashMap<>();

    static {
        Arrays.stream(functions).forEach(x -> funcs.put(x.name, x));
    }

    private static JExpFunction<?> getFunction(Token token) {
        return funcs.get(token.term);
    }
    // endregion

    // region define the operators and precedence
    final static Operator[][] operators = {
            // all operators are grouped into some groups in which they have the same precedence.
            // the groups are sorted by their precedence, the higher the latter.
            {
                new Operator("=", 2, Operator.Associative.RIGHT, Operator.Suffix.INFIX, false, vs -> Operators.assign(vs[0], vs[1]), 0),
            },
            {
                new Operator("||", 2, Operator.Associative.RIGHT, Operator.Suffix.INFIX, true, vs -> Operators.or(vs[0], vs[1]), 0),
            },
            {
                new Operator("&&", 2, Operator.Associative.RIGHT, Operator.Suffix.INFIX, true, vs -> Operators.and(vs[0], vs[1]), 0),
            },
            {
                new Operator("!", 1, Operator.Associative.RIGHT, Operator.Suffix.PREFIX, true, vs -> Operators.not(vs[0]), 0),
            },
            {
                new Operator(">", 2, Operator.Associative.RIGHT, Operator.Suffix.INFIX, true, vs -> Operators.compareG(vs[0], vs[1]), 0),
                new Operator(">=", 2, Operator.Associative.RIGHT, Operator.Suffix.INFIX, true, vs -> Operators.compareGE(vs[0], vs[1]), 0),
                new Operator("<", 2, Operator.Associative.RIGHT, Operator.Suffix.INFIX, true, vs -> Operators.compareL(vs[0], vs[1]), 0),
                new Operator("<=", 2, Operator.Associative.RIGHT, Operator.Suffix.INFIX, true, vs -> Operators.compareLE(vs[0], vs[1]), 0),
                new Operator("==", 2, Operator.Associative.RIGHT, Operator.Suffix.INFIX, true, vs -> Operators.compareE(vs[0], vs[1]), 0),
                new Operator("!=", 2, Operator.Associative.RIGHT, Operator.Suffix.INFIX, true, vs -> Operators.compareNE(vs[0], vs[1]), 0),
                new Operator("<=>", 2, Operator.Associative.RIGHT, Operator.Suffix.INFIX, true, vs -> Operators.compareSort(vs[0], vs[1]), 0),
            },
            {
                new Operator("+", 2, Operator.Associative.LEFT, Operator.Suffix.INFIX, true, vs -> Operators.addition(vs[0], vs[1]), 0),
                new Operator("-", 2, Operator.Associative.LEFT, Operator.Suffix.INFIX, true, vs -> Operators.subtraction(vs[0], vs[1]), 0),
                new Operator("++", 2, Operator.Associative.LEFT, Operator.Suffix.INFIX, false, vs -> Operators.concat(vs[0], vs[1]), 0),
            },
            {
                new Operator("*", 2, Operator.Associative.LEFT, Operator.Suffix.INFIX, true, vs -> Operators.multiplication(vs[0], vs[1]), 0),
                new Operator("/", 2, Operator.Associative.LEFT, Operator.Suffix.INFIX, true, vs -> Operators.division(vs[0], vs[1]), 0),
                new Operator("%", 2, Operator.Associative.LEFT, Operator.Suffix.INFIX, true, vs -> Operators.modulo(vs[0], vs[1]), 0),
            },
            {
                new Operator("+", 1, Operator.Associative.RIGHT, Operator.Suffix.PREFIX, true, vs -> Operators.plus(vs[0]), 0),
                new Operator("-", 1, Operator.Associative.RIGHT, Operator.Suffix.PREFIX, true, vs -> Operators.minus(vs[0]), 0),
            },
            {
                new Operator("^", 2, Operator.Associative.RIGHT, Operator.Suffix.INFIX, true, vs -> Operators.power(vs[0], vs[1]), -1),
                new Operator("!", 1, Operator.Associative.LEFT, Operator.Suffix.POSTFIX, true, vs -> Extra.fact(vs[0]), -1),
            },
            {
                new Operator(".", 2, Operator.Associative.LEFT, Operator.Suffix.INFIX, false, (ctx, vs) -> Operators.dotChain(ctx, vs[0], vs[1]), 0),
            },
    };

    private static final Map<String, Operator> ops = new HashMap<>();

    static {
        for (int l = 0; l < operators.length; ++l) {
            for (int n = 0; n < operators[l].length; ++n) {
                Operator op = operators[l][n];
                ops.put(op.op + l + op.operands, op);
            }
        }
    }

    private static Operator getOperator(Token token, int level, int operands) {
        return ops.get(token.term + level + operands);
    }

    private static Pair<Operator, Integer> getOperatorFwd(Token token, int level, int operands) {
        for (int l = level; l < operators.length; ++l) {
            Operator op = ops.get(token.term + l + operands);
            if (op != null) {
                return Pair.of(op, l);
            }
        }
        return Pair.of(null, operators.length);
    }
    // endregion

    private boolean isHighest(int precedence) {
        return precedence == operators.length;
    }

    private boolean hasNext() {
        return tokenEmitter.hasNext();
    }

    private Token peek() {
        if (hasNext()) {
            return tokenEmitter.peek();
        } else {
            throw new Exp4jException.ParseException("unexpected EOF");
        }
    }

    private void next() {
        tokenEmitter.next();
    }

    private Token consume() {
        return consume(null);
    }

    private Token consume(String term) {
        Token t = peek();
        next();
        if (term != null && !t.isTerm(term)) {
            throw new Exp4jException.ParseException(String.format("expect '%s' at %d", term, t.pos));
        }
        return t;
    }

    private <T> List<T> parseEnclosedValue(String left, String right, Function<Integer, T> parse, String sep) {
        consume(left);
        List<T> tokens = new ArrayList<>();

        boolean closed = false;
        while (hasNext()) {
            Token n = peek();
            if (n.term.equals(right)) {
                next();
                return tokens;
            }

            T t = parse.apply(0); //parseRightAssociative(0);
            tokens.add(t);

            n = peek();
            if (n.term.equals(right)) {
                next();
                closed = true;
                break;
            } else if (n.term.equals(sep)) {
                next();
            } else {
                throw new Exp4jException.ParseException(String.format("unexpected '%s' at pos %d", n.term, n.pos));
            }
        }

        if (!closed) {
            throw new Exp4jException.ParseException("unexpected EOF");
        }

        return tokens;
    }

    private Pair<String, Node> parseKeyValue(int n) {
        Token t = consume();
        if (t.type == Type.String) {
            String key = t.term.substring(1, t.term.length() - 1);
            consume(":");
            return Pair.of(key, parseRightAssociativeOpt(n));
        } else {
            throw new Exp4jException.ParseException(String.format("unexpected '%s' at pos %d", t.term, t.pos));
        }
    }

    private String parseJsonName() {
        if (hasNext()) {
            Token t = peek();
            if (t.type == Type.Name || t.type == Type.String) {
                next();
                return t.type == Type.Name? t.term : t.value.asString();
            }
        }

        return null;
    }

    private Node parseJsonPath() {
        // Json Access Syntax
        //  $(.(name)|[index|filter])+
        //  $.    : to access the root, may be a scalar, a vector or a map
        //  $.XX  : to access the properties XX of root, assume root have XX or null
        //  $.[]  : to access the array of from root. assume root is array or null
        //  $.[i] : to access the element of array from root. assume root is array and index is in the range or null
        //  $.[filter]: to access the matched element of array from root. assume root is array or null
        //
        //  $.XX[i].YY.ZZ[filter]: get the filtered element list of path XX[i].YY.ZZ
        //  and optional {} to enclose the exp after $, say ${.x}

        consume("$");
        boolean enclosed = false;
        Node target = null;
        List<JsonPathValueNode.PathAccessor> access = new ArrayList<>();

        // check whether enclosed
        if (hasNext() && peek().term.equals("{")) {
            enclosed = true;
            next();
        }

        // parse possible target
        String name = parseJsonName();
        if (name != null) {
            target = new SimpleValueNode(Value.of(name));
        }

        // parse json path
        while (hasNext()) {
            Token t = peek();
            boolean exit = false;
            switch (t.term) {
                case "[": {
                    next();
                    Integer index = null;
                    ExpValueNode filter = null;

                    Token n = peek();
                    switch (n.term) {
                        case "]":
                            next();
                            break;
                        case "@":
                            next();
                            List<Node> args = parseEnclosedValue("{", "}", parseExpressFunc, ";");
                            filter = new ExpValueNode(args.toArray(new Node[0]));
                            consume("]");
                            break;
                        default:
                            if (n.type == Type.Number) {
                                next();
                                index = Integer.parseInt(n.term);
                                consume("]");
                            } else {
                                // special process
                                if (n.term.equals("-")) {
                                    next();
                                    if (peek().type == Type.Number) {
                                        index = -Integer.parseInt(peek().term);
                                        next();
                                        consume("]");
                                    } else {
                                        throw new Exp4jException.ParseException(String.format("unexpected '%s' at pos %d", n.term, n.pos));
                                    }
                                } else {
                                    throw new Exp4jException.ParseException(String.format("unexpected '%s' at pos %d", n.term, n.pos));
                                }
                            }
                            break;
                    }
                    access.add(new JsonPathValueNode.PathAccessor(index, filter));
                    break;
                }
                case ".": {
                    if (access.size() == 1 && access.get(0).root) {
                        throw new Exp4jException.ParseException(String.format("unexpected '%s' at pos %d", t.term, t.pos));
                    }
                    next();

                    String property = parseJsonName();
                    if (property != null) {
                        access.add(new JsonPathValueNode.PathAccessor(property));
                    } else {
                        if (access.isEmpty()) {
                            // root access
                            access.add(new JsonPathValueNode.PathAccessor());
                        } else {
                            throw new Exp4jException.ParseException(String.format("unexpected '%s' at pos %d", peek().term, peek().pos));
                        }
                    }
                    break;
                }
                default:
                    exit = true;
                    break;
            }

            if (exit)
                break;
        }

        // close enclosed if any
        Token close = null;
        if (enclosed) {
            if (peek().term.equals("}")) {
                close = peek();
                next();
            } else {
                throw new Exp4jException.ParseException(String.format("unexpected '%s' at pos %d", peek().term, peek().pos));
            }
        }

        if (access.isEmpty() && target == null) {
            throw new Exp4jException.ParseException(String.format("unexpected '%s' at pos %d",
                    close == null? peek().term : close.term,
                    close == null? peek().pos : close.pos));
        }
        return new JsonPathValueNode(target, access);
    }

    private Node parseValue() {
        Token t = peek();
        switch (t.type) {
            case String:
            case Null:
            case Number:
            case Boolean:
                next();
                return new SimpleValueNode(t.value);
            case Template:
                next();
                return new TemplateValueNode(Arrays.stream(t.value.asVector())
                        .map(x -> x.isString()? new SimpleValueNode(x) :
                                ((ExpValueNode.JExp)x.asExpression()).node)
                        .collect(Collectors.toList())
                );
            case Name:
                next();
                JExpFunction<?> func = getFunction(t);
                if (func == null) {
                    if (hasNext() && peek().term.equals("(")) {
                        // function call style, take it as custom function call
                        List<Node> args = parseEnclosedValue("(", ")", parseExpressFunc, ",");
                        return new NameNode(t.term, args.toArray(new Node[0]));
                    } else {
                        return new NameNode(t.term);
                    }
                } else {
                    List<Node> args = parseEnclosedValue("(", ")", parseExpressFunc, ",");
                    return new FuncNode(func, args.toArray(new Node[0]));
                }
            case Punctuation:
                switch (t.term) {
                    case "{": {
                        List<Pair<String, Node>> args = parseEnclosedValue("{", "}", this::parseKeyValue, ",");
                        Map<String, Node> map = new HashMap<>();
                        for (Pair<String, Node> e : args) {
                            map.put(e.first, e.second);
                        }
                        return new MapValueNode(map);
                    }
                    case "[": {
                        List<Node> args = parseEnclosedValue("[", "]", parseExpressFunc, ",");
                        return new ListValueNode(args);
                    }
                    case "@": {
                        next();
                        List<Node> args = parseEnclosedValue("{", "}", parseExpressFunc, ";");
                        return new ExpValueNode(args.toArray(new Node[0]));
                    }
                    case "$": {
                        return parseJsonPath();
                    }
                    default:
                        throw new Exp4jException.ParseException(String.format("unexpected '%s' at pos %d", t.term, t.pos));
                }
            default:
                throw new Exp4jException.ParseException("unexpected node type: " + t.type);
        }
    }

    private Node parseHighestPrecedence() {
        // <X> ::= <value> | "(" <A> ")"
        // terminals (literal values etc), grouped expression
        if (!hasNext())
            throw new Exp4jException.ParseException("unexpected EOF");

        Token t = peek();
        if (t.isTerm("(")) {
            List<Node> args = parseEnclosedValue("(", ")", parseExpressFunc, ",");
            if (args.isEmpty())
                return null;
            else
                return args.get(args.size() - 1);
        } else {
            return parseValue();
        }
    }

    private Node parseLeftAssociative(Node lhs, Operator op, int precedence) {
        // <X> ::= <X+1> { "op" <X> | "op2" <X>) }
        // left associative binary operator
        Node rhs = parseRightAssociative(precedence + 1);
        if (rhs == null)
            throw new Exp4jException.ParseException("unexpected EOF");

        Node node = new OpNode(lhs, op, rhs);
        if (hasNext()) {
            Operator newOp = getOperator(peek(), precedence, 2);
            if (newOp != null) {
                next();
                return parseLeftAssociativeOpt(node, newOp, precedence);
            }
        }

        return node;
    }

    private Node parseRightAssociative(int precedence) {
        // <D> ::= <D+1> | <D> "op" <D+1>
        // <D> ::= <D+1> "op"
        // <D> ::= "op" <D+1>
        // unary operator and right associative binary operator
        if (isHighest(precedence)) {
            return parseHighestPrecedence();
        }

        if (hasNext()) {
            Operator op1 = getOperator(peek(), precedence, 1);
            if (op1 != null) {
                next();
                return new OpNode(op1, parseRightAssociative(precedence));
            }

            Node r = parseRightAssociative(precedence + 1);
            if (hasNext()) {
                Token t = peek();
                op1 = getOperator(t, precedence, 1);
                if (op1 != null && op1.suffix == Operator.Suffix.POSTFIX) {
                    next();
                    return new OpNode(op1, r);
                }

                Operator op2 = getOperator(t, precedence, 2);
                if (op2 != null) {
                    next();
                    if (op2.associative == Operator.Associative.RIGHT) {
                        Node rhs = parseRightAssociative(precedence + op2.refer);
                        if (rhs == null)
                            throw new Exp4jException.ParseException("unexpected EOF");
                        return new OpNode(r, op2, rhs);
                    }
                    else
                        return parseLeftAssociative(r, op2, precedence + op2.refer);
                }
            }

            return r;
        }

        return null;
    }

    private Node parseLeftAssociativeOpt(Node lhs, Operator op, int precedence) {
        // <X> ::= <X+1> { "op" <X> | "op2" <X>) }
        // left associative binary operator
        Node rhs = parseRightAssociativeOpt(precedence + 1);
        if (rhs == null)
            throw new Exp4jException.ParseException("unexpected EOF");

        Node node = new OpNode(lhs, op, rhs);
        if (hasNext()) {
            Pair<Operator, Integer> newOp = getOperatorFwd(peek(), precedence, 2);
            if (newOp.first != null) {
                next();
                return parseLeftAssociativeOpt(node, newOp.first, newOp.second);
//            } else {
//                break;
            }
        }

        return node;
    }

    private Node parseRightAssociativeOpt(int precedence) {
        // <D> ::= <D+1> | <D> "op" <D+1>
        // <D> ::= <D+1> "op"
        // <D> ::= "op" <D+1>
        // unary operator and right associative binary operator
        Node r = null;
        while (hasNext()) {
            if (r == null) {
                Pair<Operator, Integer> op1 = getOperatorFwd(peek(), precedence, 1);
                if (op1.first != null) {
                    next();
                    r = new OpNode(op1.first, parseRightAssociativeOpt(op1.second));
                } else {
                    r = parseHighestPrecedence();
                }
            }

            if (hasNext()) {
                Token t = peek();
                Pair<Operator, Integer> op1 = getOperatorFwd(t, precedence, 1);
                do {
                    if (op1.first != null && op1.first.suffix == Operator.Suffix.POSTFIX) {
                        next();
                        r = new OpNode(op1.first, r);
                    }
                    op1 = getOperatorFwd(t, op1.second + 1, 1);
                } while (op1.first != null);

                Pair<Operator, Integer> op2 = getOperatorFwd(t, precedence, 2);
                if (op2.first != null) {
                    next();
                    if (op2.first.associative == Operator.Associative.RIGHT) {
                        Node rhs = parseRightAssociativeOpt(op2.second + op2.first.refer);
                        if (rhs == null)
                            throw new Exp4jException.ParseException("unexpected EOF");
                        r = new OpNode(r, op2.first, rhs);
                    } else
                        r = parseLeftAssociativeOpt(r, op2.first, op2.second + op2.first.refer);
                } else {
                    break;
                }
            }
        }

        return r;
    }

    private List<Node> parse(boolean firstNodeOnly) {
        int precedence = 0;
        List<Node> ns = new ArrayList<>();
        do {
            Node n = parseExpressFunc.apply(precedence);
            if (n != null)
                ns.add(n);

            if (firstNodeOnly)
                break;

            if (hasNext()) {
                Token t = peek();
                if (!t.term.matches("[\r\n;]")) {
                    throw new Exp4jException.ParseException(String.format("unexpected '%s' at pos %d", t.term, t.pos));
                }
                next();
            }
        } while (hasNext());

        return ns;
    }

    static class TokenEmitter {
        private final List<Token> tokens;
        private final Tokenizer tokenizer;
        private int offset;

        TokenEmitter(char[] input, int start, boolean optimize) {
            this.tokens = new ArrayList<>();
            this.tokenizer = new Tokenizer(input, start, optimize);
            this.offset = 0;
        }

        boolean hasNext() {
            if (offset < tokens.size())
                return true;

            if (tokenizer.hasNext()) {
                Token t = tokenizer.nextToken();
                if (t != null) {
                    tokens.add(t);
                    return true;
                }
            }

            return false;
        }

        Token peek() {
            return tokens.get(offset);
        }

        void next() {
            assert hasNext();
            offset++;
        }

        static class Tokenizer {
            private final char[] expression;
            private final int expressionLength;
            private final boolean optimize;
            private int pos;

            public Tokenizer(char[] expression, int offset, boolean optimize) {
                this.expression = expression;
                this.pos = offset;
                this.optimize = optimize;
                this.expressionLength = this.expression.length;
            }

            public boolean hasNext() {
                return this.expression.length > pos;
            }

            public char peek() {
                return expression[this.pos];
            }

            private boolean isEndOfExpression() {
                return this.expressionLength <= this.pos;
            }

            public void advance() {
                advance(1);
            }

            public void stepBack() {
                advance(-1);
            }

            public char getAndAdvance() {
                assert(!isEndOfExpression());
                return expression[this.pos++];
            }

            public void advance(char skipChar) {
                assert(!isEndOfExpression());
                assert(expression[this.pos] == skipChar);
                advance(1);
            }

            public void advance(int n) {
                this.pos += n;
            }

            public Token nextToken() {
                skipSpaces();
                if (isEndOfExpression())
                    return null;

                Token token = null;
                char ch = peek();
                switch (ch) {
                    case '#':
                        while (pos < expressionLength) {
                            char n = getAndAdvance();
                            if (n == '\r' || n == '\n')
                                break;
                        }
                        return nextToken();
                    case ',':
                    case '(':
                    case ')':
                    case '[':
                    case ']':
                    case '{':
                    case '}':
                    case ':':
                    case ';':
                    case '@':
                    case '*':
                    case '/':
                    case '%':
                    case '.':
                    case '-':
                    case '^':
                    case '$':
                        token = new Token(String.valueOf(ch), Type.Punctuation, pos, null);
                        advance();
                        break;
                    case '=':
                    case '!':
                    case '>':
                    case '<':
                        advance();
                        if (hasNext() && peek() == '=') {
                            if (ch == '<') {
                                advance();
                                if (hasNext() && peek() == '>') {
                                    token = new Token("<=>", Type.Punctuation, pos - 2, null);
                                    advance();
                                } else {
                                    token = new Token("<=", Type.Punctuation, pos - 1, null);
                                }
                            } else {
                                token = new Token(ch + "=", Type.Punctuation, pos - 1, null);
                                advance();
                            }
                        } else {
                            token = new Token(String.valueOf(ch), Type.Punctuation, pos - 1, null);
                        }
                        break;
                    case '+':
                        advance();
                        if (hasNext() && peek() == '+') {
                            token = new Token("++", Type.Punctuation, pos - 1, null);
                            advance();
                        } else {
                            token = new Token("+", Type.Punctuation, pos - 1, null);
                        }
                        break;
                    case '|':
                        advance();
                        if (hasNext() && peek() == '|') {
                            token = new Token("||", Type.Punctuation, pos - 1, null);
                            advance();
                        }
                        break;
                    case '&':
                        advance();
                        if (hasNext() && peek() == '&') {
                            token = new Token("&&", Type.Punctuation, pos - 1, null);
                            advance();
                        }
                        break;
                    case '"':
                        token = parseStringLiteral();
                        break;
                    case '`':
                        token = parseTemplate();
                        break;
                    case '\r':
                    case '\n':
                        token = new Token(String.valueOf(ch), Type.EOL, pos, null);
                        advance();
                        break;
                    default: {
                        if (Character.isDigit(ch)) {
                            token = parseNumberToken();
                        } else if (Character.isLetter(ch) || ch == '_') {
                            token = parseConstantTerm();
                        }
                    }
                }

                if (token == null) {
                    throw new Exp4jException.ParseException(String.format("unexpected '%s' at pos %d", ch, pos));
                }
                return token;
            }

            private String getLiteral(int offset) {
                return new String(expression, offset, pos - offset);
            }

            private Token parseNumberToken() {
                Pattern pattern = Pattern.compile("^[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?");
                Matcher matcher = pattern.matcher(new String(expression, pos, expression.length - pos));

                if (matcher.find()) {
                    String term = matcher.group();
                    int start = pos;
                    pos += term.length();
                    if (Pattern.compile("[.eE]").matcher(term).find())
                        return new Token(term, Type.Number, start, Value.of(Double.parseDouble(term)));
                    else
                        return new Token(term, Type.Number, start, Value.of(Long.parseLong(term)));
                } else {
                    throw new Exp4jException.ParseException("???");
                }
            }

            private Token parseStringLiteral() {
                int offset = pos;
                advance('"');
                boolean escape = false;
                StringBuilder buffer = new StringBuilder();
                while (!isEndOfExpression()) {
                    char ch = getAndAdvance();
                    if (escape) {
                        if (isEscapable(ch)) {
                            buffer.append(ch);
                            escape = false;
                        }
                        else
                            throw new Exp4jException.ParseException("\\" + ch, this.pos);
                    } else {
                        if (ch == '"') {
                            return new Token(getLiteral(offset), Type.String, offset, Value.of(buffer.toString()));
                        } else if (ch == '\\') {
                            escape = true;
                        } else {
                            buffer.append(ch);
                        }
                    }
                }

                throw new Exp4jException.ParseException("unexpected EOF");
            }

            private String extractJsonPathInTemplate() {
                // this is a simplified version of get the json path string without actually parsing
                StringBuilder buffer = new StringBuilder();
                boolean escaped = false;
                boolean enclosed = false;
                boolean quoted = false;

                advance('$');
                buffer.append('$');

                if (peek() == '{') {
                    enclosed = true;
                    advance();
                }

                while (!isEndOfExpression()) {
                    char ch = getAndAdvance();
                    boolean quitFlag = false;
                    switch (ch) {
                        case '\\': {
                            if (escaped) {
                                escaped = false;
                                buffer.append(ch);
                            } else {
                                escaped = true;
                            }
                            break;
                        }
                        case '}':
                            if (!quoted) {
                                quitFlag = true;
                                break;
                            }
                        case '"':
                            quoted = !quoted;
                            buffer.append(ch);
                            break;
                        default: {
                            if (quoted || enclosed || isAlphabetic(ch) || ch == '.') {
                                buffer.append(ch);
                            } else {
                                stepBack();
                                quitFlag = true;
                            }
                            break;
                        }
                    }

                    if (quitFlag)
                        break;
                }

                if (escaped)
                    stepBack();

                return buffer.toString();
            }

            private Token parseTemplate() {
                int offset = pos;
                advance('`');

                List<Value> tokens = new ArrayList<>();

                StringBuilder buffer = new StringBuilder();
                boolean escaped = false;
                while (!isEndOfExpression()) {
                    char ch = getAndAdvance();
                    if (ch == '\\') {
                        if (escaped) {
                            escaped = false;
                            buffer.append(ch);
                        } else {
                            escaped = true;
                        }
                        continue;
                    }

                    if (ch == '$' && !escaped) {
                        // wrap all chars before exp as string token
                        String before = buffer.toString();
                        tokens.add(Value.of(before));

                        // reset the buffer
                        buffer = new StringBuilder();

                        // take the exp as JsonPath token
                        stepBack();
                        String path = extractJsonPathInTemplate();
                        JExpParser parser = new JExpParser(path.toCharArray(), 0, optimize);
                        List<Node> ns = parser.parse(true);
                        tokens.add(Value.of(new ExpValueNode.JExp(new ExpValueNode(ns.toArray(new Node[0])))));
                    } else if (ch != '`' || escaped) {
                        buffer.append(ch);
                        escaped = false;
                    } else {
                        break;
                    }
                }

                if (buffer.length() > 0) {
                    tokens.add(Value.of(buffer.toString()));
                }

                return new Token(getLiteral(offset), Type.Template, pos, Value.of(tokens));
            }

            private boolean isEscapable(char ch) {
                return Arrays.asList('\\', '\b', '\t', '\n', '\f', '\r', '"').contains(ch);
            }

            private void skipSpaces() {
                while (!isEndOfExpression()) {
                    char ch = getAndAdvance();
                    if (ch != ' ' && ch != '\t') {
                        stepBack();
                        break;
                    }
                }
            }

            private Token parseConstantTerm() {
                int start = pos;
                while (pos < this.expressionLength && isVariableOrFunctionCharacter(expression[pos])) {
                    ++pos;
                }

                String name = new String(expression, start, pos - start);
                if (name.equals("null")) {
                    return new Token(name, Type.Null, pos, Value.NULL);
                } else if (name.equals("true") || name.equals("false")) {
                    return new Token(name, Type.Boolean, pos, Value.of(Boolean.parseBoolean(name)));
                } else {
                    return new Token(name, Type.Name, pos, null);
                }
            }

            private static boolean isAlphabetic(char codePoint) {
                return Character.isLetter(codePoint);
            }

            private static boolean isVariableOrFunctionCharacter(char codePoint) {
                return isAlphabetic(codePoint) ||
                        Character.isDigit(codePoint) ||
                        codePoint == '_';
            }
        }
    }

    private final TokenEmitter tokenEmitter;

    private final Function<Integer, Node> parseExpressFunc;

    public JExpParser(char[] expression, int start, boolean optimize) {
        this.tokenEmitter = new TokenEmitter(expression, start, optimize);
        this.parseExpressFunc = optimize?
                this::parseRightAssociativeOpt : this::parseRightAssociative;
    }

    public static void main(String[] args) {
        // region: test cases
        String[][] testSet = {
                {"", ""},
                {"5", "5"},
                {"-5", "-5"},
                {"+5", "5"},
                {"5.2", "5.2"},
                {"0.5", "0.5"},
                {"1.5e2", "150.0"},
                {"1.5E-2", "0.015"},
                {"1.5e22", "1.5E22"},
                {"\"hello\"", "hello"},
                {"null", "null"},
                {"false", "false"},
                {"unknown", "null"},
                {"()", ""},
                {"(1)", "1"},
                {"(1, true)", "true"},
                {"-2^4", "-16.0"},
                {"1+2+3", "6"},
                {"3/2", "1.5"},
                {"5%2 + 1", "2.0"},
                {"1-2-3", "-4"},
                {"1-2/2-4/2-6/2", "-5.0"},
                {"1--2-+3", "0"},
                {"1--2*+3", "7"},
                {"(1--2)*+3", "9"},
                {"-3!", "-6"},
                {"2>=1", "true"},
                {"3<=>3", "0"},
                {"true && 2>1", "true"},
                {"2>=1 || false", "true"},
                {"1+2*3", "7"},
                {"2*2+3", "7"},
                {"!(3<3)", "true"},
                {"!(3<3);", "true"},
                {"!(3<3)\r2", "2"},
                {"!(3<3)\n4", "4"},
                {"2?2", "unexpected '?' at pos 1"},
                {"2;2+1", "3"},
                {"x=3;x+1", "4"},
                {"pow(2,2)+2", "6.0"},
                {"[12, false, null, \"hello\"]", "[12, false, null, \"hello\"]"},
                {"[12, false, null, [], []]", "[12, false, null, [], []]"},
                {"{\"name\":\"Jerry\", \"age\": 22, \"address\":[]}", "{\"address\": [], \"name\": \"Jerry\", \"age\": 22}"},
                {"length([12, false, null])", "3"},
                {"abs(-1)", "1"},
                {"abs([-1,2,-3])", "[1, 2, 3]"},
                {"pow([1,2,3],2)", "[1.0, 4.0, 9.0]"},
                {"[12, false, null].length()", "3"},
                {"- [1,2,3]", "[-1, -2, -3]"},
                {"- ([1,2,3]!)", "[-1, -2, -6]"},
                {"[1,2,3]*2", "[2, 4, 6]"},
                {"4-[1,2,3]", "[3, 2, 1]"},
                {"[1,3,5]-[1,2,3]", "[0, 1, 2]"},
                {"x=@{1+1;1+2}", ".+?JExpParser\\$ExpValueNode\\$JExp@\\w+"},
                {"x=@{1+1;1+2}; x()", "3"},
                {"x=1;y=7;z=@{x+1;x+y};z()", "8"},
                {"z=@{_*2};z(2)", "4"},
                {"---1", "-1"},
                {"[1]++[false]", "[1, false]"},
                {"2^-2", "0.25"},
                {"-2^-2", "-0.25"},
                {"-2.abs()", "-2"},
                {"add(1,2,3,4,5)", "15"},
                {"#add(1,2,3,4,5)\n1+2#+3", "3"},
                {"x={\"\": 2}; $x.\"\"", "2"},
                {"x=[1,2,3,4,5];    $x[2]", "3"},
                {"x={\"a\": 4, \"a+b\":\"15\", \"b\": 11}; $x.\"a+b\"", "15"},
                {"x={\"a\": [1,false,null], \"b\": []}; $x.a", "[1, false, null]"},
                {"x={\"a\": [1,false,null], \"b\": []}; $x.a[1]", "false"},
                {"x={\"a\": [1,false,null], \"b\": []}; $x.a[3]", "null"},
                {"x={\"a\": [1,-2,-3]}; $x.a[1].abs()", "unexpected '(' at pos 31"},
                {"x={\"a\": [1,-2,-3]}; ${x.a[1]}.abs()", "2"},
                {"x={\".\":{\"x\": false}}; $x.\".\".x", "false"},
                {"$..x", "unexpected '.' at pos 2"},
                {"name=\"Fox\"; `name: $name`", "name: Fox"},
        };
        // endregion

        String redColor = "\u001B[31m";
        String greenColor = "\u001B[32m";
        String resetColor = "\u001B[0m";

        boolean optimize = false;
        if (args.length !=  0) {
            optimize = args[0].compareTo("optimize") == 0;
        }
        System.out.println("Using optimized parser: " + optimize);

        int total = 0, good = 0, bad = 0;
        String json = "{}";
        SimpleContext ctx = new SimpleContext(json);
        for (String[] test : testSet) {
            Value v = Value.of("");
            boolean matched;
            StringBuilder sb = new StringBuilder();
            sb.append("RAW: '").append(test[0]).append("', ");

            String error = "";
            try {
                SimpleBuilder builder = new SimpleBuilder(test[0], optimize);
                List<IExpression> exps = builder.buildAll();
                sb.append("PARSED: '");
                for (IExpression e: exps) {
                    sb.append(((SimpleExpression)e).dump()).append(";");
                    v = e.evaluate(ctx);
                }
                sb.append("'");

                String value = v.isString()? v.asString() : v.toString();
                matched = value.equals(test[1]);
                if (!matched)
                    matched = value.matches(test[1]);
            } catch (PatternSyntaxException e) {
                matched = false;
                sb.append(e.getMessage());
            } catch (Exp4jException e) {
                matched = e.getMessage().equals(test[1]);
                if (!matched)
                    matched = e.getMessage().matches(test[1]);

                if (!matched) {
                    e.printStackTrace();
                    error = e.getMessage();
                } else {
                    sb.append("got expected exception for bad format express: ").append(e.getMessage());
                }
            } catch (Exception e) {
                e.printStackTrace();
                matched = false;
                error = e.getMessage();
            }

            if (matched) {
                good++;
                System.out.printf("%s[GOOD]%s %s.%n", greenColor, resetColor, sb);
            } else {
                bad++;
                System.out.printf("%s[BAD]%s expected {%s}, actually {%s}. %s.%n",
                        redColor, resetColor, test[1], error, sb);
            }
            total++;
        }

        System.out.printf("\u001B[1m=== Run case %d, passed %d, failed %d. ===%s%n", total, good, bad, resetColor);
    }
}
