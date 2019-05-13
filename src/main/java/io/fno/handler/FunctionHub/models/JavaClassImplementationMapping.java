package io.fno.handler.FunctionHub.models;

import com.google.gson.annotations.SerializedName;

public class JavaClassImplementationMapping extends ImplementationMapping {
    @SerializedName("method-name")
    public String methodName;
    private PositionParameterMapping[] parameterMappings;

    public JavaClassImplementationMapping(JavaClassImplementation implementation, String methodName, PositionParameterMapping[] parameterMappings) {
        super(implementation);
        this.parameterMappings = parameterMappings;
        this.methodName = methodName;
    }

    public PositionParameterMapping[] getParameterMappings() {
        return parameterMappings;
    }
}
