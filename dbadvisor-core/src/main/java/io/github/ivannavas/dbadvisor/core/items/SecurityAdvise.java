package io.github.ivannavas.dbadvisor.core.items;

public record SecurityAdvise(
        SecurityAdviseType type
) {
    public enum SecurityAdviseType {
        EXCESIVE_PRIVILEGES,
    }
}
