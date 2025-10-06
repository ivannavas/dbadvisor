package io.github.ivannavas.dbadvisor.core.advisors;

import io.github.ivannavas.dbadvisor.core.items.Plan;

import java.util.List;

public interface Advisor<A> {
    List<A> getAdvise(Plan plan);

    String getAdviseMessage(A advise);
}
