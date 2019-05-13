package io.fno.handler.FunctionHub;

import com.google.api.client.util.Key;

public class NodeRequest {
    @Key
    String id;

    public NodeRequest(String id) {
        this.id = id;
    }
}
