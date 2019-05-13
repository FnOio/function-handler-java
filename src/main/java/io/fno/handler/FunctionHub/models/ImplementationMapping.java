package io.fno.handler.FunctionHub.models;

import com.google.api.client.util.Key;

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
