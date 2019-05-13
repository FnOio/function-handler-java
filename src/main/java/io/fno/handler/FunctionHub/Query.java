package io.fno.handler.FunctionHub;

import io.fno.handler.FunctionHub.models.ImplementationType;
import io.fno.handler.FunctionHub.models.Problem;

public class Query {


    private String[] keywords;
    private QueryParam[] expects;
    private QueryParam output;
    private Problem solves;
    private String implementationType;

    public Query(String[] keywords, QueryParam[] expects, QueryParam output, Problem solves, ImplementationType implementationType) {
        this.keywords = keywords;
        this.expects = expects;
        this.output = output;
        this.solves = solves;
        this.implementationType = getImplementationType(implementationType);
    }

    public Query(String[] keywords, QueryParam[] expects, QueryParam output, Problem solves) {
        this.keywords = keywords;
        this.expects = expects;
        this.output = output;
        this.solves = solves;
        this.implementationType = null;
    }

    public QueryParam[] getExpects() {
        return expects;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public QueryParam getOutput() {
        return output;
    }

    public Problem getSolves() {
        return solves;
    }

    public String getImplementationType() {
        return implementationType;
    }

    private String getImplementationType(ImplementationType implementationType) {
        switch (implementationType) {
            case LOCAL:
                return "http://example.com/functions/JavaImplementation";
            case WEB_API:
                return "http://example.com/functions/WebApi";
            default:
                return null;
        }
    }
}
