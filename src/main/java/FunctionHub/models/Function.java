package FunctionHub.models;

import com.google.api.client.util.Key;
import com.google.gson.annotations.SerializedName;

public class Function {
    @SerializedName("@id")
    public String uri;
    @Key
    public String name;
    @Key
    public String description;
    @Key
    public Parameter returns;
    @Key
    public Parameter[] expects;
    @Key
    public Problem[] solves;
    @Key
    public Implementation[] implementations;
    @Key
    public ImplementationMapping[] implementationMappings;

    public Function(String uri, String name, String description, Parameter returns, Parameter[] expects, Problem[] solves, Implementation[] implementations, ImplementationMapping[] implementationMappings) {
        this.uri = uri;
        this.name = name;
        this.description = description;
        this.returns = returns;
        this.expects = expects;
        this.solves = solves;
        this.implementations = implementations;
        this.implementationMappings = implementationMappings;
    }
}
