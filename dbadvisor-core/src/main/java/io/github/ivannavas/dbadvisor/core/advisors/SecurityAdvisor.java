package io.github.ivannavas.dbadvisor.core.advisors;

import io.github.ivannavas.dbadvisor.core.items.Plan;
import io.github.ivannavas.dbadvisor.core.items.SecurityAdvise;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Slf4j
public class SecurityAdvisor implements Advisor<SecurityAdvise> {
    @Override
    public List<SecurityAdvise> onInit(Function<String, List<Object>> queryExecutor) {
        List<SecurityAdvise> advises = new ArrayList<>();

        advises.addAll(testUserPermissions(queryExecutor));

        return advises;
    }

    private List<SecurityAdvise> testUserPermissions(Function<String, List<Object>> queryExecutor) {
        List<SecurityAdvise> advises = new ArrayList<>();

        try {
            queryExecutor.apply("CREATE TABLE security_audit (id VARCHAR);");
            advises.add(new SecurityAdvise("The application database user has permission to create tables. This may pose a security risk."));

            queryExecutor.apply("DROP TABLE IF EXISTS security_audit;");
            advises.add(new SecurityAdvise("The application database user has permission to drop tables. This may pose a security risk."));
        } catch (Exception e) {
            log.trace("The application database user does not have permission to create tables or the create table query failed.", e);
        }

        return advises;
    }

    @Override
    public List<SecurityAdvise> onQuery(Plan plan) {
        String query = plan.query().toLowerCase();
        List<SecurityAdvise> advises = new ArrayList<>();

        return advises;
    }

    @Override
    public String getAdviseMessage(SecurityAdvise advise) {
        return advise.message();
    }
}
