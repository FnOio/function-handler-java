package io.fno.handler.FunctionHub.FunctionProcessor;

import io.fno.handler.FunctionHub.models.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LocalFunctionInstance implements FunctionInstance {

    private final static Logger LOGGER = Logger.getLogger(LocalFunctionInstance.class.getName());
    private Method functionMethod;
    private Class functionClass;
    private Function function;
    private JavaClassImplementationMapping implementationMapping;
    private Implementation implementation;

    public LocalFunctionInstance(Class functionClass, Method functionMethod, Function function, JavaClassImplementationMapping implementationMapping) {
        this.functionClass = functionClass;
        this.functionMethod = functionMethod;
        this.function = function;
        this.implementationMapping = implementationMapping;
        this.implementation = implementationMapping.getImplementation();
    }

    public Class[] getParameterTypes() {
        return functionMethod.getParameterTypes();
    }

    public Class getReturnType() {
        return functionMethod.getReturnType();
    }

    @Override
    public String getImplementationType() {
        return implementation.getType();
    }

    @Override
    public Object executeFunction(Object... arguments) throws InvocationTargetException, IllegalAccessException {
        Object[] argumentArray = new Object[arguments.length];
        Parameter[] expects = function.expects;
        for (int i = 0; i < expects.length; i++) {
            Parameter param = expects[i];
            for (PositionParameterMapping mapping : implementationMapping.getParameterMappings()) {
                if (param.id.equals(mapping.getFunctionParameterId())) {
                    argumentArray[mapping.getImplementationParameterPosition() - 1] = arguments[i];
                }
            }
        }
        try {
            return functionMethod.invoke(functionClass, argumentArray);
        } catch (IllegalArgumentException e) {
            StringBuilder message = new StringBuilder();
            message.append("Wrong argument type passed to function \"").append(function.name).append("\"\n");
            message.append("Argument types accepted: ").append("\n");
            for (Class arg : functionMethod.getParameterTypes()) {
                message.append("\t- ").append(arg.getName()).append("\n");
            }
            message.append("Argument types passed: ").append("\n");
            for (Object arg : arguments) {
                message.append("\t- ").append(arg.getClass().getName()).append("\n");
            }
            message.append("Did you forget a cast?");
            LOGGER.log(Level.SEVERE, message.toString());
            throw e;
        }
    }
}
