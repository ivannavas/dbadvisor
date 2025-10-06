package io.github.ivannavas.dbadvisor.core.items;

import java.util.List;

public record Node(
        NodeType type,
        String relationName,
        String alias,
        String filter,
        Double startupCost,
        Double totalCost,
        Long rows,
        List<Node> children
) {
    public enum NodeType {
        SEQ_SCAN,
        INDEX_SCAN,
        JOIN,
        AGGREGATE,
        FILTER,
        UNKNOWN
    }
}
