package FunctionHub.models;

import com.google.api.client.util.Key;

public class WebImplementation extends Implementation {
    @Key
    public String baseUrl;
    @Key
    public String paramMethod;
    @Key
    public String httpMethod;

    public WebImplementation(String uri, String baseUrl, String paramMethod,  String httpMethod) {
        this.baseUrl = baseUrl;
        this.paramMethod = paramMethod;
        this.httpMethod = httpMethod;
        this.type = "JsonApi";
        this.uri = uri;
    }
}
