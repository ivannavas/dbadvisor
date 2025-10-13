package io.github.ivannavas.dbadvisor.core.advisors;

import io.github.ivannavas.dbadvisor.core.items.Plan;

import java.util.List;
import java.util.function.Function;

public interface Advisor<A> {

    default List<A> onInit(Function<String, List<Object>> queryExecutor) {
        return List.of();
    }

    default List<A> onQuery(Plan plan) {
        return List.of();
    }

    String getAdviseMessage(A advise);
}
