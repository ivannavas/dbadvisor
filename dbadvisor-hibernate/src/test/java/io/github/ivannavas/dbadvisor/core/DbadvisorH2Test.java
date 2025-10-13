package io.github.ivannavas.dbadvisor.core;

import io.github.ivannavas.dbadvisor.core.advisors.IndexAdvisor;
import io.github.ivannavas.dbadvisor.h2.parsers.H2PlanParser;
import org.junit.jupiter.api.Test;

import java.util.List;

class DbadvisorH2Test {

    private final Dbadvisor dbadvisor = Dbadvisor.getInstance();

    @Test
    void runAnalysisTest_h2() {
        dbadvisor.configure().planParser(new H2PlanParser()).advisors(List.of(new IndexAdvisor()));
        String rawPlan = """
                PLAN
                ----
                SELECT STATEMENT  (cost=3 card=1 bytes=4)
                  TABLE ACCESS FULL EMPLOYEES  (cost=3 card=1 bytes=4)
                """;
        dbadvisor.runQueryAnalysis(rawPlan, "SELECT * FROM EMPLOYEES");
    }
}
