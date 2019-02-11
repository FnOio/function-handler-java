package FunctionHub.FunctionProcessor;

import java.lang.reflect.InvocationTargetException;

public interface FunctionInstance {
    Class[] getParameterTypes();
    Class getReturnType();
    String getImplementationType();
    Object executeFunction(Object... arguments) throws InvocationTargetException, IllegalAccessException;
}
