package io.github.ivannavas.dbadvisor.postgresql.parsers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ivannavas.dbadvisor.core.items.Node;
import io.github.ivannavas.dbadvisor.core.items.Plan;
import io.github.ivannavas.dbadvisor.core.parsers.PlanParser;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PostgresqlJsonPlanParser implements PlanParser {

    private final ObjectMapper om = new ObjectMapper();

    @Override
    public Plan parse(String rawPlan) {
        try {
            JsonNode root = om.readTree(rawPlan).get(0).get("Plan");
            return toPlan(root);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid EXPLAIN json", e);
        }
    }

    @Override
    public String getPlanGeneratorQuery(String query) {
        return "EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON) " + query;
    }

    private Plan toPlan(JsonNode explain) {
        JsonNode rootPlan = explain.has("Plan") ? explain.get("Plan") : explain;
        if (rootPlan == null || rootPlan.isMissingNode()) {
            throw new IllegalArgumentException("Invalid EXPLAIN json: no 'Plan' node");
        }
        Node root = toNode(rootPlan);
        return new Plan(root);
    }

    private Node toNode(JsonNode n) {
        Node.NodeType nodeType = switch (n.path("Node Type").asText()) {
            case "Seq Scan" -> Node.NodeType.SEQ_SCAN;
            case "Index Scan" -> Node.NodeType.INDEX_SCAN;
            case "Join" -> Node.NodeType.JOIN;
            case "Aggregate" -> Node.NodeType.AGGREGATE;
            case "Filter" -> Node.NodeType.FILTER;
            default -> Node.NodeType.UNKNOWN;
        };

        List<Node> children = Optional.ofNullable(n.get("Plans"))
                .filter(JsonNode::isArray)
                .map(arr -> StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(arr.elements(), Spliterator.ORDERED),
                        false))
                .orElseGet(Stream::empty)
                .map(this::toNode)
                .toList();

        return new Node(
                nodeType,
                n.path("Relation Name").asText(null),
                n.path("Alias").asText(null),
                n.path("Filter").asText(null),
                n.path("Startup Cost").asDouble(0.0),
                n.path("Total Cost").asDouble(0.0),
                n.path("Plan Rows").asLong(0),
                children
        );
    }
}
