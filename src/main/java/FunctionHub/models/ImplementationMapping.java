package FunctionHub.models;

import com.google.api.client.util.Key;
import com.google.gson.annotations.SerializedName;

public class ImplementationMapping {
    @Key
    Implementation implementation;


    public Implementation getImplementation() {
        return implementation;
    }


    public ImplementationMapping(Implementation implementation) {
        this.implementation = implementation;
    }

}
