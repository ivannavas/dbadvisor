package io.github.ivannavas.dbadvisor.core.items;

public record IndexAdvise(
        String tableName,
        String columnName,
        String indexType,
        String reason
) {
}
