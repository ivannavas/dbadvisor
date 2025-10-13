package io.github.ivannavas.dbadvisor.hibernate.listeners;

import io.github.ivannavas.dbadvisor.core.Configuration;
import io.github.ivannavas.dbadvisor.core.Dbadvisor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

@AllArgsConstructor
@Slf4j
public class QueryPlanListener implements QueryExecutionListener {

    private static final Dbadvisor dbadvisor = Dbadvisor.getInstance();

    private DataSource dataSource;

    @Override
    public void beforeQuery(ExecutionInfo executionInfo, List<QueryInfo> list) {

    }

    @Override
    public void afterQuery(ExecutionInfo executionInfo, List<QueryInfo> list) {
        Configuration configuration = dbadvisor.getConfiguration();

        for (QueryInfo queryInfo : list) {
            String query = queryInfo.getQuery();
            if (!query.trim().toLowerCase().startsWith("select")) {
                continue;
            }

            try (Connection c = dataSource.getConnection()) {
                PreparedStatement ps = c.prepareStatement(configuration.getPlanParser().getPlanGeneratorQuery(query));
                var rs = ps.executeQuery();
                if (rs.next()) {
                    String rawPlan = rs.getString(1);
                    dbadvisor.runQueryAnalysis(rawPlan, query);
                }

                rs.close();
                ps.close();
            } catch (Exception e) {
                log.error("Failed to get query plan for query: {}", query, e);
            }
        }
    }
}
