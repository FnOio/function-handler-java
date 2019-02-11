package FunctionHub.models;

public class PropertyParameterMapping extends ParameterMapping{
    private String implementationProperty;
    public PropertyParameterMapping(String functionParameterId, String implementationProperty) {
        super(functionParameterId);
        this.implementationProperty = implementationProperty;
    }

    public String getImplementationProperty() {
        return implementationProperty;
    }
}
