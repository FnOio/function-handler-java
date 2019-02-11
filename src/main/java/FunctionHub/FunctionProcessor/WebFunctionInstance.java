package FunctionHub.FunctionProcessor;

import FunctionHub.models.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class WebFunctionInstance implements FunctionInstance {
    private Class[] parameterTypes;
    private Class returnType;
    private Function function;
    private WebAPIImplementationMapping implementationMapping;
    private WebImplementation implementation;

    public WebFunctionInstance(Function function, WebAPIImplementationMapping implementationMapping, Class[] parameterTypes, Class returnType) {
        this.function = function;
        this.implementation = (WebImplementation) implementationMapping.getImplementation();
        this.implementationMapping = implementationMapping;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    @Override
    public Class[] getParameterTypes() {
        return parameterTypes;
    }

    @Override
    public Class getReturnType() {
        return returnType;
    }

    @Override
    public String getImplementationType() {
        return implementation.getType();
    }

    @Override
    public Object executeFunction(Object... arguments) throws InvocationTargetException, IllegalAccessException {


        StringBuilder sb = new StringBuilder();
        sb.append(implementation.baseUrl);
        JsonObject body = new JsonObject();
        for (int i = 0; i < function.expects.length; i++) {
            Parameter param = function.expects[i];
            for (PropertyParameterMapping mapping : implementationMapping.getParameterMappings()) {
                if (param.id.equals(mapping.getFunctionParameterId())) {
                    body.addProperty(mapping.getImplementationProperty(), arguments[i].toString());
                }
            }
        }
        URL url;
        HttpURLConnection connection = null;
        try {
            //Create connection
            url = new URL(sb.toString());
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod(this.implementation.httpMethod);
            connection.setRequestProperty("Content-Type",
                    "application/json");

            connection.setUseCaches (false);
            connection.setDoInput(true);

            //Send request
            if(function.expects.length > 0) {
                connection.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream (
                        connection.getOutputStream ());
                wr.writeBytes(body.toString());
                wr.flush ();
                wr.close ();
            }

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder response = new StringBuilder();
            while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            Gson gson = new Gson();
            LinkedTreeMap result = gson.fromJson(response.toString(), LinkedTreeMap.class);
            return result.get(implementationMapping.getResultKey());

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        } finally {

            if(connection != null) {
                connection.disconnect();
            }
        }
    }

}
