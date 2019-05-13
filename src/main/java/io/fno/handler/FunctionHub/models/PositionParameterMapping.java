package io.fno.handler.FunctionHub.models;

public class PositionParameterMapping extends ParameterMapping {
    private int implementationParameterPosition;

    public PositionParameterMapping(String functionParameterId, int implementationParameterPosition) {
        super(functionParameterId);
        this.implementationParameterPosition = implementationParameterPosition;
    }

    public int getImplementationParameterPosition() {
        return implementationParameterPosition;
    }

    public void setImplementationParameterPosition(int implementationParameterPosition) {
        this.implementationParameterPosition = implementationParameterPosition;
    }
}
