package FunctionHub.FunctionProcessor;

import FunctionHub.exception.InvalidClass;
import FunctionHub.exception.InvalidMethod;
import FunctionHub.models.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImplementationHandler {
    private final static Logger LOGGER = Logger.getLogger(LocalFunctionInstance.class.getName());

    public static FunctionInstance instantiateFunctionImplementation(Function function, ImplementationMapping implementationMapping) throws IOException {
        Implementation implementation = implementationMapping.getImplementation();
        if (implementation.getClass() == JavaClassImplementation.class) {
            return instantiateLocalFunction(function, (JavaClassImplementationMapping)implementationMapping);
        } else if (implementation.getClass() == WebImplementation.class){
            return instantiateWebFunction(function, (WebAPIImplementationMapping) implementationMapping);
        }
        return null;
    }

    private static FunctionInstance instantiateWebFunction(Function function, WebAPIImplementationMapping implementationMapping) {
        Class<?> parameters[] = new Class[function.expects.length];
        for (int i = 0; i < function.expects.length; i++) {
            parameters[i] = getParamType(function.expects[i]);
        }

        return new WebFunctionInstance(function, implementationMapping, parameters, getParamType(function.returns));
    }

    private static Path URLToPath(URL url){
        List<String> result = new ArrayList<>();
        String noProtocol = url.toString().replaceFirst(".*://", "");
        String[] splittedUrl = noProtocol.split("/");
        for (String s :
                splittedUrl) {
            String[] splittedDots = s.split("\\.");
            result.addAll(Arrays.asList(splittedDots));
        }
        return Paths.get(String.join("/",result));
    }

    private static FunctionInstance instantiateLocalFunction(Function function, JavaClassImplementationMapping implementationMapping) throws IOException {
        JavaClassImplementation implementation = (JavaClassImplementation)implementationMapping.getImplementation();

        // TODO: versioning system
        URL downloadPage = new URL(implementation.downloadPage);
        Path otherPath = URLToPath(new URL(implementation.getURI()));
        Path target = FileSystems.getDefault().getPath("functionhub_implementations/").resolve(otherPath).resolve(FilenameUtils.getName(downloadPage.getPath()));

        // Only download if file does not exist yet
        target.getParent().toFile().mkdirs();
        if (!target.toFile().exists()){
            InputStream in = downloadPage.openStream();
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        // TODO: make this less ugly? Use MIME types?
        Class instantiatedClass = null;
        if (target.toString().toLowerCase().endsWith(".java")) {
            instantiatedClass = getClassFromJAVA(target, implementation.className);
        } else if (target.toString().toLowerCase().endsWith(".jar")) {
            instantiatedClass = getClassFromJAR(target, implementation.className);
        }

        // ERROR loading class
        if (instantiatedClass == null) {
            LOGGER.log(Level.SEVERE, "Class " + implementation.className + " does not exist in file \"" + implementation.downloadPage + "\" or could not be compiled.");
            throw new InvalidClass();
        }

        Class<?> parameters[] = new Class[function.expects.length];
        for (int i = 0; i < function.expects.length; i++) {
            parameters[i] = getParamType(function.expects[i]);
        }
        Method method = MethodUtils.getMatchingAccessibleMethod(instantiatedClass, implementationMapping.methodName, parameters);

        // ERROR loading method
        if (method == null) {
            StringBuilder message = new StringBuilder();
            message.append("The function description does not match the implementation\n");
            message.append( "Argument types in description: ").append("\n");
            for (Class arg: parameters){
                message.append("\t- ").append(arg.getName()).append("\n");
            }
            message.append( "Argument types in implementation: ").append("\n");
            for (Method err_method: instantiatedClass.getDeclaredMethods()) {
                if (err_method.getName().equals(implementationMapping.methodName)) {
                    for (Object arg : err_method.getParameterTypes()) {
                        message.append("\t- ").append(arg.toString()).append("\n");
                    }
                }
            }
            LOGGER.log(Level.SEVERE, message.toString());
            throw new InvalidMethod();
        }

        // SUCCESS! Return instantiated method
        return new LocalFunctionInstance(instantiatedClass, method, function, implementationMapping);
    }

    private static Class getClassFromJAR(Path sourceFile, String className) {
        Class<?> cls = null;

        URLClassLoader child = null;
        try {
            child = URLClassLoader.newInstance(new URL[]{sourceFile.getParent().toUri().toURL()});
            cls = Class.forName(className, true, child);
        } catch (MalformedURLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return cls;
    }


    private static Class getClassFromJAVA(Path sourceFile, String className) {
        Class<?> cls = null;

        // Compile source file if not yet compiled previously.
        File f = sourceFile.toFile();
        int i = f.getName().lastIndexOf('.');
        String name = f.getName().substring(0,i);
        if (!new File(f.getParent() + "/" + name + ".class").exists()) {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            int res = compiler.run(null, null, null, sourceFile.toString());
            if (res != 0) {
                return null;
            }
        }

        // Load and instantiate compiled class.
        URLClassLoader classLoader = null;
        try {
            classLoader = URLClassLoader.newInstance(new URL[]{sourceFile.getParent().toUri().toURL()});
            cls = Class.forName(className, true, classLoader);
        } catch (MalformedURLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return cls;
    }

    private static Map<String, Class> typeMapping = Map.ofEntries(
            Map.entry("http://www.w3.org/2001/XMLSchema#float", float.class),
            Map.entry("http://www.w3.org/2001/XMLSchema#double", double.class),
            Map.entry("http://www.w3.org/2001/XMLSchema#string", String.class),
            Map.entry("http://www.w3.org/2001/XMLSchema#boolean", boolean.class),
            Map.entry("http://www.w3.org/2001/XMLSchema#int", int.class),
            Map.entry("http://www.w3.org/2001/XMLSchema#integer", int.class),
            Map.entry("http://www.w3.org/2001/XMLSchema#long", long.class),
            Map.entry("http://www.w3.org/2001/XMLSchema#short", short.class)
    );

    private static Class getParamType(Parameter parameter) {
        return typeMapping.get(parameter.type);
    }

}
