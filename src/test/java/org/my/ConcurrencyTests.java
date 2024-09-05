package org.my;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

public class ConcurrencyTests {

    @Test
    public void testFutureEvaluation() throws Exception {
        ExecutorService exec = Executors.newFixedThreadPool(10);
        int numTests = 10000;
        double[] correct1 = new double[numTests];
        Future<?>[] results1 = new Future[numTests];

        double[] correct2 = new double[numTests];
        Future<?>[] results2 = new Future[numTests];

        for (int i = 0; i < numTests; i++) {
            IExpressionContext context = Entry.buildContext();
            context.updateVariable("n", i);
            correct1[i] = Math.sin(2 * Math.PI / (i + 1));
            results1[i] = Entry.buildExpressionBuilder("sin(2*pi/(n+1))")
                    .build()
                    .evaluateAsync(exec, context);

            IExpressionContext context2 = Entry.buildContext();
            context2.updateVariable("n", Value.of(i));
            correct2[i] = Math.log(Math.E * Math.PI * (i + 1));
            results2[i] = Entry.buildExpressionBuilder("log(e*pi*(n+1))")
                    .build()
                    .evaluateAsync(exec, context2);
        }

        for (int i = 0; i < numTests; i++) {
            assertEquals(correct1[i], ((Value)results1[i].get()).asDouble(), 0.000000001d);
            assertEquals(correct2[i], ((Value)results2[i].get()).asDouble(), 0.000000001d);
        }
    }
}
