package io.fno.handler;

import io.fno.handler.FunctionHub.FunctionProcessor.FunctionInstance;
import io.fno.handler.FunctionHub.FunctionProcessor.ImplementationHandler;
import io.fno.handler.FunctionHub.Query;
import io.fno.handler.FunctionHub.Server;
import io.fno.handler.FunctionHub.models.Function;
import io.fno.handler.FunctionHub.models.ImplementationMapping;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class TimeEvaluation {
    private static Map<ImplementationMapping, Float> times = new HashMap<>();

    public static ImplementationMapping getFastestImplementation(ImplementationMapping[] mappings) {
        if (times.isEmpty()) {
            return mappings[0];
        }
        float smallestTime = Float.MAX_VALUE;
        ImplementationMapping fastest = null;
        for (ImplementationMapping m : times.keySet()) {
            if (times.get(m) < smallestTime) {
                fastest = m;
                smallestTime = times.get(m);
            }
        }
        return fastest;
    }

    public static void main(String[] args) throws Exception {
        // Connect to the FnHub Server
        Server fnServer = new Server(new URL("https://fno.io/hub/api"));
        Query query = new Query(new String[]{"hello world"}, null, null, null);
        Function function = fnServer.query(query)[0];
        int iterations = 5;

        for (ImplementationMapping m : function.implementationMappings) {
            FunctionInstance instance = ImplementationHandler.instantiateFunctionImplementation(function, m);
            long startTime2 = System.nanoTime();
            Object output = instance.executeFunction();
            long endTime = System.nanoTime();
            double timeWithout = (endTime - startTime2) / 1000000.0;
            times.put(m, (float) timeWithout);
        }

        for (int i = 0; i < 25; i++) {
            System.out.println("Iteration: " + i);
            ImplementationMapping m = getFastestImplementation(function.implementationMappings);
            float timeWith = 0;
            float timeWithout = 0;
            for (int j = 0; j < iterations; j++) {
                long startTime = System.nanoTime();
                FunctionInstance instance = ImplementationHandler.instantiateFunctionImplementation(function, m);
                long startTime2 = System.nanoTime();
                Object output = instance.executeFunction();
                long endTime = System.nanoTime();
                timeWith += (endTime - startTime) / 1000000.0;
                timeWithout += (endTime - startTime2) / 1000000.0;
            }
            times.put(m, timeWithout / iterations);
            System.out.println(m.getImplementation().getType() + ": " + timeWithout / iterations);
        }


        for (int j = 0; j < 25; j++) {
            System.out.println("Iteration: " + j);
            for (ImplementationMapping m : function.implementationMappings) {
                float timeWith = 0;
                float timeWithout = 0;
                for (int i = 0; i < iterations; i++) {
                    long startTime = System.nanoTime();
                    FunctionInstance instance = ImplementationHandler.instantiateFunctionImplementation(function, m);
                    long startTime2 = System.nanoTime();
                    Object output = instance.executeFunction();
                    long endTime = System.nanoTime();
                    timeWith += (endTime - startTime) / 1000000.0;
                    timeWithout += (endTime - startTime2) / 1000000.0;
                }

                System.out.println(m.getImplementation().getType() + ": " + timeWithout / iterations);
            }
        }

    }
}
