package io.fno.handler.FunctionHub;

public class QueryParam {
    private String type;
    private String[] keywords;

    public QueryParam(String type, String[] keywords) {
        this.type = type;
        this.keywords = keywords;
    }

    public String getType() {
        return type;
    }

    public String[] getKeywords() {
        return keywords;
    }
}
