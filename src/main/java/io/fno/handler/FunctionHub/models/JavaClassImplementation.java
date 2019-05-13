package io.fno.handler.FunctionHub.models;

import com.google.gson.annotations.SerializedName;

public class JavaClassImplementation extends Implementation {

    @SerializedName("download-page")
    public String downloadPage;
    @SerializedName("class-name")
    public String className;

    public JavaClassImplementation(String uri, String downloadPage, String className) {
        this.downloadPage = downloadPage;
        this.className = className;
        this.type = "JavaClass";
        this.uri = uri;
    }
}
