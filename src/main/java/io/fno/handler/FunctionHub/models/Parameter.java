package io.fno.handler.FunctionHub.models;

import com.google.api.client.util.Key;

public class Parameter {
    public String id;
    @Key
    public String type;
    @Key
    public String description;

    public Parameter(String id, String type, String description) {
        this.id = id;
        this.type = type;
        this.description = description;
    }


    public Parameter() {
    }
}
