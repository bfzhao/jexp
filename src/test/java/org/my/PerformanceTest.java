package org.my;

import org.junit.Ignore;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Formatter;
import java.util.Random;

public class PerformanceTest {

    private static final long BENCH_TIME = 2L;
    private static final String EXPRESSION = "log(x) - y * (sqrt(x^cos(y)))";

    @Ignore
    @Test
    public void testBenches() throws Exception {
        StringBuffer sb = new StringBuffer();
        Formatter fmt = new Formatter(sb);
        fmt.format("+------------------------+---------------------------+--------------------------+%n");
        fmt.format("| %-22s | %-25s | %-24s |%n", "Implementation", "Calculations per Second", "Percentage of Math");
        fmt.format("+------------------------+---------------------------+--------------------------+%n");
        System.out.print(sb.toString());
        sb.setLength(0);

        int math = benchJavaMath();
        double mathRate = (double) math / (double) BENCH_TIME;
        fmt.format("| %-22s | %25.2f | %22.2f %% |%n", "Java Math", mathRate, 100f);
        System.out.print(sb.toString());
        sb.setLength(0);

        for (int i = 1; i < 6; ++i) {
            int db = benchDouble();
            double dbRate = (double) db / (double) BENCH_TIME;
            fmt.format("| %-20s %d | %25.2f | %22.2f %% |%n", "my", i, dbRate, dbRate * 100 / mathRate);
            System.out.print(sb.toString());
            sb.setLength(0);
        }

        int js = benchJavaScript();
        double jsRate = (double) js / (double) BENCH_TIME;
        fmt.format("| %-22s | %25.2f | %22.2f %% |%n", "JSR-223 (Java Script)", jsRate, jsRate * 100 / mathRate);
        fmt.format("+------------------------+---------------------------+--------------------------+%n");
        System.out.print(sb.toString());
    }

    private int benchDouble() {
        IExpressionContext context = Entry.buildContext();
        final IExpression expression = Entry.buildExpressionBuilder(EXPRESSION)
                .build();

        Random rnd = new Random();
        long time = System.currentTimeMillis() + (1000 * BENCH_TIME);
        int count = 0;
        while (time > System.currentTimeMillis()) {
            context.updateVariable("x", Value.of(rnd.nextDouble()));
            context.updateVariable("y", Value.of(rnd.nextDouble()));
            expression.evaluate(context).asDouble();
            count++;
        }
        return count;
    }

    private int benchJavaMath() {
        long time = System.currentTimeMillis() + (1000 * BENCH_TIME);
        double x, y;
        int count = 0;
        Random rnd = new Random();
        while (time > System.currentTimeMillis()) {
            x = rnd.nextDouble();
            y = rnd.nextDouble();
            y = Math.log(x) - y * (Math.sqrt(Math.pow(x, Math.cos(y))));
            count++;
        }
        return count;
    }

    private int benchJavaScript() throws Exception {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        long time;
        double x, y;
        int count = 0;
        Random rnd = new Random();
        if (engine == null) {
            System.err.println("Unable to instantiate javascript engine. skipping naive JS bench.");
            return -1;
        } else {
            time = System.currentTimeMillis() + (1000 * BENCH_TIME);
            while (time > System.currentTimeMillis()) {
                x = rnd.nextDouble();
                y = rnd.nextDouble();
                engine.eval("Math.log(" + x + ") - " + y + "* (Math.sqrt(" + x + "^Math.cos(" + y + ")))");
                count++;
            }
        }
        return count;
    }
}
