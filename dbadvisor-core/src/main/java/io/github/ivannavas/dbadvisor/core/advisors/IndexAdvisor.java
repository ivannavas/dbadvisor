package io.github.ivannavas.dbadvisor.core.advisors;

import io.github.ivannavas.dbadvisor.core.items.IndexAdvise;
import io.github.ivannavas.dbadvisor.core.items.Node;
import io.github.ivannavas.dbadvisor.core.items.Plan;

import java.util.List;

public class IndexAdvisor implements Advisor<IndexAdvise> {

    @Override
    public List<IndexAdvise> onQuery(Plan plan) {
        return plan.root().children()
                .stream()
                .filter(n -> !n.type().equals(Node.NodeType.UNKNOWN))
                .map(this::analyzeNode)
                .flatMap(List::stream)
                .toList();
    }

    @Override
    public String getAdviseMessage(IndexAdvise advise) {
        return String.format(
                "Consider creating a %s index on column '%s' of table '%s'. Reason: %s",
                advise.indexType(),
                advise.columnName(),
                advise.tableName(),
                advise.reason()
        );
    }

    private List<IndexAdvise> analyzeNode(Node node) {
        if (node.totalCost() != null && node.totalCost() < 10.0) {
            return List.of();
        }

        switch (node.type()) {
            case SEQ_SCAN -> {
                if (node.filter() != null && !node.filter().isEmpty()) {
                    String column = extractColumnFromFilter(node.filter());
                    IndexAdvise advise = new IndexAdvise(
                            node.relationName(),
                            column,
                            "B-tree",
                            "Sequential scan with filter on column '" + column + "'"
                    );
                    return List.of(advise);
                }
            }
            case JOIN -> {
                IndexAdvise advise = new IndexAdvise(
                        node.relationName(),
                        "join_column",
                        "B-tree",
                        "Join operation on table '" + node.relationName() + "'"
                );
                return List.of(advise);
            }
            default -> {
                return node.children()
                        .stream()
                        .map(this::analyzeNode)
                        .flatMap(List::stream)
                        .toList();
            }
        }
        return List.of();
    }

    private String extractColumnFromFilter(String filter) {
        if (filter == null || filter.isEmpty()) {
            return "unknown_column";
        }
        String[] parts = filter.split(" ");
        return parts.length > 0 ? parts[0] : "unknown_column";
    }
}
