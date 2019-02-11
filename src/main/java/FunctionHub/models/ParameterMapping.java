package FunctionHub.models;

public class ParameterMapping {
    private String functionParameterId;

    public ParameterMapping(String functionParameterId) {
        this.functionParameterId = functionParameterId;
    }

    public String getFunctionParameterId() {
        return functionParameterId;
    }

    public void setFunctionParameterId(String functionParameterId) {
        this.functionParameterId = functionParameterId;
    }
}
