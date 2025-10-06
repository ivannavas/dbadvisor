package io.github.ivannavas.dbadvisor.core.parsers;

import io.github.ivannavas.dbadvisor.core.items.Plan;

public interface PlanParser {
    Plan parse(String rawPlan);

    String getPlanGeneratorQuery(String query);
}
