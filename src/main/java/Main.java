
import FunctionHub.FunctionProcessor.FunctionInstance;
import FunctionHub.QueryParam;
import FunctionHub.Server;
import FunctionHub.models.Function;
import FunctionHub.models.ImplementationType;
import FunctionHub.models.Parameter;
import FunctionHub.Query;
import FunctionHub.FunctionProcessor.ImplementationHandler;

import java.net.URL;
import java.text.NumberFormat;

/**
 * Created by landernoterman on 06/12/2017.
 */
public class Main {
    public static void main(String [] args) throws Exception {
        // Connect to the FnHub Server
        Server fnServer = new Server(new URL("https://fno.io/hub/api"));

        double totalArea =  30528.0;
        double populationTotal = 11099981;

        QueryParam[] params = {new QueryParam("float", new String[]{"total population"}), new QueryParam("float", new String[]{"area"})};
        Query query = new Query(new String[]{"population density"}, params, null, null, ImplementationType.WEB_API);
        Function function = fnServer.query(query)[0];
        FunctionInstance calculatePopulationDensity = ImplementationHandler.instantiateFunctionImplementation(function, function.implementationMappings[0]);
        System.out.println("The population density of Belgium is: " + calculatePopulationDensity.executeFunction((float)populationTotal, (float)totalArea));

        QueryParam[] reversedParams = {new QueryParam("float", new String[]{"area"}), new QueryParam("float", new String[]{"total population"})};
        query = new Query(new String[]{"population density"}, reversedParams, null, null, ImplementationType.LOCAL);
        function = fnServer.query(query)[0];
        calculatePopulationDensity = ImplementationHandler.instantiateFunctionImplementation(function, function.implementationMappings[0]);
        System.out.println("The population density of Belgium is: " + calculatePopulationDensity.executeFunction((float)totalArea, (float)populationTotal));

        query = new Query(new String[]{"hello world"}, null, null, null);
        function = fnServer.query(query)[0];
        FunctionInstance helloWorld = ImplementationHandler.instantiateFunctionImplementation(function, function.implementationMappings[0]);
        System.out.println("Output: " + helloWorld.executeFunction());


    }
}

