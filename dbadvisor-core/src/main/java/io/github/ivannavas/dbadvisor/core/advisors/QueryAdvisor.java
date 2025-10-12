package io.github.ivannavas.dbadvisor.core.advisors;

import io.github.ivannavas.dbadvisor.core.items.Plan;

import java.util.List;

public abstract class QueryAdvisor<A> implements Advisor<A> {
    public abstract List<A> getAdvise(Plan plan);
}
