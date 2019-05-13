package io.fno.handler.FunctionHub.models;

import com.google.api.client.util.Key;

public class Problem {
    @Key
    public String[] input;

    @Key
    public String output;

    public Problem(String[] input, String output) {
        this.input = input;
        this.output = output;
    }

    public Problem() {

    }
}
