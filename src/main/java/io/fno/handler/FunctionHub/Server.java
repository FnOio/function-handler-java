package io.fno.handler.FunctionHub;

import io.fno.handler.FunctionHub.models.*;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Server {
    private URL url;

    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    private static final JsonFactory JSON_FACTORY = new JacksonFactory();

    public Server(URL url) {
        this.url = url;
    }

    public Function[] query(Query query) throws IOException {
        HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(request -> request.setParser(new JsonObjectParser(JSON_FACTORY)));
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        String jsonString = gson.toJson(query);
        HttpRequest request = requestFactory.buildPostRequest(new GenericUrl(url + "/query"),
                ByteArrayContent.fromString("application/json", jsonString));
        Object jsonObject = JsonUtils.fromInputStream(request.execute().getContent());

        try {
            return convertToFunctions(jsonObject, query).toArray(new Function[0]);
        } catch (JsonLdError jsonLdError) {
            jsonLdError.printStackTrace();
        }
        return null;
    }

    private List retrieveNodeFromServer(String id) throws IOException, JsonLdError {
        NodeRequest req = new NodeRequest(id);
        HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(request -> request.setParser(new JsonObjectParser(JSON_FACTORY)));
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        String jsonString = gson.toJson(req);
        HttpRequest request = requestFactory.buildPostRequest(new GenericUrl(url + "/node"),
                ByteArrayContent.fromString("application/json", jsonString));
        Object jsonObject = JsonUtils.fromInputStream(request.execute().getContent());
        return JsonLdProcessor.expand(jsonObject);
    }

    private List<Function> convertToFunctions(Object jsonld, Query query) throws JsonLdError {
        List functionList = (List) jsonld;
        List<Function> result = new ArrayList<>();
        for (Object doc : functionList) {
            // Expand JSON-LD document for easier access
            List graph = JsonLdProcessor.expand(doc);

            // Find node with Function type (should be only one)
            Map functionNode = (Map) getNodesOfType("http://w3id.org/function/ontology/Function", graph).get(0);

            // Get basic information from node
            String id = functionNode.get("@id").toString();
            String description = (String) getValueOrId("http://purl.org/dc/terms/description", functionNode);
            String name = (String) getValueOrId("http://w3id.org/function/ontology/name", functionNode);

            // Retrieving "expects" value
            List<Parameter> expectsTmp = new ArrayList<>();
            for (Object param : (List) ((Map) ((List) functionNode.get("http://w3id.org/function/ontology/expects")).get(0)).get("@list")) {
                String paramId = (String) ((Map) param).get("@id");
                Map paramNode = (Map) getNodeById(paramId, graph);
                expectsTmp.add(paramNodeToParameter(paramNode, graph));
            }

            Parameter[] expects = new Parameter[expectsTmp.size()];
            // Make "expects" list same order as query
            if (query.getExpects() != null) {
                for (Parameter param : expectsTmp) {
                    for (int i = 0; i < query.getExpects().length; i++) {
                        if (expects[i] != null) {
                            continue;
                        }
                        QueryParam queryparam = query.getExpects()[i];
                        boolean error = false;
                        if (queryparam.getType() != null && !queryparam.getType().equals(param.type.replace("http://www.w3.org/2001/XMLSchema#", ""))) {
                            continue;
                        }
                        if (queryparam.getKeywords() != null) {
                            for (String keyword : queryparam.getKeywords()) {
                                if (!param.description.contains(keyword)) {
                                    error = true;
                                    break;
                                }
                            }
                        }
                        if (!error) {
                            expects[i] = param;
                            break;
                        }
                    }
                }
            }

            // Retrieving "returns" value
            Object returnsValue = ((List) functionNode.get("http://w3id.org/function/ontology/returns")).get(0);
            String returnsId = (String) ((Map) returnsValue).get("@id");
            Map returnsNode = getNodeById(returnsId, graph);
            Parameter returns = paramNodeToParameter(returnsNode, graph);

            // Retrieving implementationMappings
            List<Map> implementationMappingNodes = getNodesOfType("http://example.com/functions/Mapping", graph);
            List<ImplementationMapping> implementationMappings = new ArrayList<>();
            for (Map implementationMappingNode : implementationMappingNodes) {
                implementationMappings.add(implementationMappingNodeToImplementationMapping(implementationMappingNode, graph));
            }

            // TODO: multiple solves
            List<Problem> problems = new ArrayList<>();
            List solvesList = (List) functionNode.get("http://w3id.org/function/ontology/solves");
            if (solvesList != null && !solvesList.isEmpty()) {
                Object solvesValue = ((List) functionNode.get("http://w3id.org/function/ontology/solves")).get(0);
                String solvesId = (String) ((Map) solvesValue).get("@id");
                Map solvesNode = getNodeById(solvesId, graph);
                List<String> inputs = new ArrayList<>();
                for (Object param : (List) ((Map) ((List) solvesNode.get("http://example.com/functions/input")).get(0)).get("@list")) {
                    String type = (String) ((Map) param).get("@id");
                    inputs.add(type);
                }
                String output = getValueOrId("http://example.com/functions/output", solvesNode);
                problems.add(new Problem(inputs.toArray(new String[0]), output));
            }
            if (query.getSolves() != null) {
                int counter = 0;
                String[] input = problems.get(0).input;
                for (int i = 0; i < query.getSolves().input.length; i++) {
                    String paramRequested = query.getSolves().input[i];
                    for (int j = 0; j < input.length; j++) {
                        String paramReturned = input[j];
                        if (paramRequested.equals(paramReturned)) {
                            expects[counter] = expectsTmp.get(j);
                            counter++;
                        }
                    }
                }
                expects = expectsTmp.toArray(new Parameter[0]);
            }

            if (query.getExpects() == null && query.getSolves() == null) {
                expects = expectsTmp.toArray(new Parameter[0]);
            }

            Function function = new Function(
                    id,
                    name,
                    description,
                    returns,
                    expects,
                    problems.toArray(new Problem[0]),
                    null,
                    implementationMappings.toArray(new ImplementationMapping[0]));
            result.add(function);

        }
        return result;
    }

    private ImplementationMapping implementationMappingNodeToImplementationMapping(Map implementationMappingNode, List graph) {
        List<String> types = (List<String>) implementationMappingNode.get("@type");
        if (types.contains("http://example.com/functions/JavaClassMapping")) {
            return nodeToJavaClassImplementationMapping(implementationMappingNode, graph);
        }
        if (types.contains("http://example.com/functions/JsonApiMapping")) {
            return nodeToWebAPIImplementationMapping(implementationMappingNode, graph);
        }
        return null;
    }

    private JavaClassImplementationMapping nodeToJavaClassImplementationMapping(Map node, List graph) {
        String methodName = getValueOrId("http://example.com/functions/method-name", node);

        List<PositionParameterMapping> parameterMappings = new ArrayList<>();
        if (node.containsKey("http://example.com/functions/parameterMapping")) {
            for (Map<String, String> paramMapId : (List<Map<String, String>>) node.get("http://example.com/functions/parameterMapping")) {
                Map<String, List<Map<String, String>>> paramNode = getNodeById(paramMapId.get("@id"), graph);
                String functionParameter = getValueOrId("http://example.com/functions/functionParameter", paramNode);
                int implementationParameterPosition = Integer.parseInt(getValueOrId("http://example.com/functions/implementationParameterPosition", paramNode));
                parameterMappings.add(new PositionParameterMapping(functionParameter, implementationParameterPosition));
            }
        }
        Object implementationNode = getNodeById(getValueOrId("http://example.com/functions/implementation", node), graph);
        String className = getValueOrId("http://example.com/functions/class-name", (Map) implementationNode);
        String downloadPage = getValueOrId("http://usefulinc.com/ns/doap#download-page", (Map) implementationNode);
        return new JavaClassImplementationMapping(new JavaClassImplementation((String) ((Map) implementationNode).get("@id"), downloadPage, className), methodName, parameterMappings.toArray(new PositionParameterMapping[0]));
    }

    private WebAPIImplementationMapping nodeToWebAPIImplementationMapping(Map node, List graph) {
        Map operationNode = getNodeById(getValueOrId("http://example.com/functions/operation", node), graph);

        // TODO: for mapping
        Object expectsNode = getNodeById(getValueOrId("http://www.w3.org/ns/hydra/core#expects", operationNode), graph);
        List<Object> supportedProperties = new ArrayList<>();
        if (expectsNode != null) {
            for (String id : getIds("http://www.w3.org/ns/hydra/core#supportedProperty", (Map<String, List<Map<String, String>>>) expectsNode)) {
                supportedProperties.add(getNodeById(id, graph));
            }
        }
        Object returnsNode = getNodeById(getValueOrId("http://www.w3.org/ns/hydra/core#returns", operationNode), graph);
        String method = getValueOrId("http://www.w3.org/ns/hydra/core#method", operationNode);

        List<String> parameterMappingNodes = getIds("http://example.com/functions/parameterMapping", node);
        Object outputMappingNode = getNodeById((String) getIds("http://example.com/functions/outputMapping", node).get(0), graph);
        String resultKey = getValueOrId("http://example.com/functions/implementationProperty", (Map) outputMappingNode);

        List<PropertyParameterMapping> parameterMappings = new ArrayList<>();
        for (String n : parameterMappingNodes) {
            Map paramNode = getNodeById(n, graph);
            String functionParameter = getValueOrId("http://example.com/functions/functionParameter", paramNode);
            String implementationProperty = getValueOrId("http://example.com/functions/implementationProperty", paramNode);
            parameterMappings.add(new PropertyParameterMapping(functionParameter, implementationProperty));
        }

        Object implementationNode = getNodeById(getValueOrId("http://example.com/functions/implementation", node), graph);

        String entrypoint = getValueOrId("http://www.w3.org/ns/hydra/core#entrypoint", (Map) implementationNode);
        String description = getValueOrId("http://www.w3.org/ns/hydra/core#description", (Map) implementationNode);
        String title = getValueOrId("http://www.w3.org/ns/hydra/core#title", (Map) implementationNode);
        return new WebAPIImplementationMapping(new WebImplementation((String) ((Map) implementationNode).get("@id"), entrypoint, null, method), parameterMappings.toArray(new PropertyParameterMapping[0]), resultKey);
    }

    private Parameter paramNodeToParameter(Map paramNode, List graph) {
        String predicateId = (String) ((Map) ((List) paramNode.get("http://w3id.org/function/ontology/predicate")).get(0)).get("@id");
        Map predicateNode = (Map) getNodeById(predicateId, graph);
        return new Parameter(
                (String) paramNode.get("@id"),
                (String) getValueOrId("http://w3id.org/function/ontology/type", predicateNode),
                (String) getValueOrId("http://purl.org/dc/terms/description", predicateNode)
        );
    }

    private List<Map<String, Object>> getNodesById(String id, List<Map<String, Object>> graph) {
        if (id == null || id.isEmpty()) {
            return null;
        }

        List<Map<String, Object>> result = graph.stream()
                .filter(p -> p.get("@id").equals(id)).collect(Collectors.toList());

        // node not in current graph, fetch node
        if (result.isEmpty()) {
            try {
                List newGraph = retrieveNodeFromServer(id);
                graph.addAll(newGraph);
                if (newGraph.isEmpty()) {
                    return null;
                }
                return getNodesById(id, (List<Map<String, Object>>) newGraph);
            } catch (IOException | JsonLdError e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    private Map<String, Object> getNodeById(String id, List<Map<String, Object>> graph) {
        List<Map<String, Object>> result = getNodesById(id, graph);

        if (result == null) {
            return null;
        }

        // merging nodes
        if (result.size() > 1) {
            Map<String, Object> first = result.get(0);
            for (int i = 1; i < result.size(); i++) {
                for (String key : result.get(i).keySet()) {
                    if (!first.containsKey(key)) {
                        first.put(key, result.get(i).get(key));
                    }
                }
            }
            return first;
        }
        return result.get(0);
    }

    private List<Map<String, List<Object>>> getNodesOfType(String type, List<Map<String, List<Object>>> graph) {
        List<Map<String, List<Object>>> list = new ArrayList<>();
        for (Map<String, List<Object>> p : graph) {
            if (p.containsKey("@type") && p.get("@type").contains(type)) {
                list.add(p);
            }
        }
        return list;
    }

    private String getValueOrId(String key, Map<String, List<Map<String, String>>> node) {
        if (node.containsKey(key)) {
            if (node.get(key).get(0).containsKey("@value")) {
                return node.get(key).get(0).get("@value");
            } else if (node.get(key).get(0).containsKey("@id")) {
                return node.get(key).get(0).get("@id");
            }
        }
        return null;
    }

    private List<String> getIds(String key, Map<String, List<Map<String, String>>> node) {
        List<String> res = new ArrayList<>();
        if (!node.containsKey(key)) {
            return res;
        }
        for (Map n : node.get(key)) {
            res.add((String) n.get("@id"));
        }
        return res;
    }
}
