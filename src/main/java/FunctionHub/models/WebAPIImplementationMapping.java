package FunctionHub.models;

public class WebAPIImplementationMapping extends ImplementationMapping {
    private PropertyParameterMapping[] parameterMappings;
    private String resultKey;
    public WebAPIImplementationMapping(WebImplementation implementation, PropertyParameterMapping[] propertyParameterMappings, String resultKey) {
        super(implementation);
        this.parameterMappings = propertyParameterMappings;
        this.resultKey = resultKey;
    }

    public PropertyParameterMapping[] getParameterMappings() {
        return parameterMappings;
    }

    public String getResultKey() {
        return resultKey;
    }
}
