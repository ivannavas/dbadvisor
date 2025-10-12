package io.github.ivannavas.dbadvisor.hibernate;

import io.github.ivannavas.dbadvisor.core.Dbadvisor;
import io.github.ivannavas.dbadvisor.core.advisors.Advisor;
import io.github.ivannavas.dbadvisor.core.parsers.PlanParser;
import io.github.ivannavas.dbadvisor.h2.parsers.H2PlanParser;
import io.github.ivannavas.dbadvisor.hibernate.listeners.QueryPlanListener;
import io.github.ivannavas.dbadvisor.postgresql.parsers.PostgresqlJsonPlanParser;
import lombok.extern.slf4j.Slf4j;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.service.spi.ServiceContributor;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
public class AutoInstallContributor implements ServiceContributor {

    private final Dbadvisor dbadvisor = Dbadvisor.getInstance();

    @Override
    public void contribute(StandardServiceRegistryBuilder serviceRegistry) {
        Map<String, Object> settings = serviceRegistry.getSettings();

        if (!Boolean.parseBoolean(String.valueOf(settings.getOrDefault("dbadvisor.enabled", "false")))) {
            log.debug("Dbadvisor is disabled. Skipping configuration.");
            return;
        }

        dbadvisor.runInitialAnalysis();

        String dialect = String.valueOf(settings.get(AvailableSettings.DIALECT)).toLowerCase();
        PlanParser planParser =
                dialect.contains("postgresql") ? new PostgresqlJsonPlanParser() :
                        dialect.contains("h2")         ? new H2PlanParser() :
                                new PostgresqlJsonPlanParser();
        if (!dialect.contains("postgresql") && !dialect.contains("h2")) {
            log.warn("Unsupported dialect '{}', using Postgres JSON parser.", dialect);
        }

        List<Advisor<?>> advisors = Stream.of(String.valueOf(settings.get("dbadvisor.advisors")).split(","))
                .<Advisor<?>>map(
                        s -> {
                            try {
                                return (Advisor<?>) Class.forName(s.trim()).getDeclaredConstructor().newInstance();
                            } catch (Exception e) {
                                log.error("Failed to instantiate advisor: {}", s, e);
                                return null;
                            }
                        }
                )
                .filter(Objects::nonNull)
                .toList();

        dbadvisor.configure().advisors(advisors).planParser(planParser);

        Object dsObj = settings.get(AvailableSettings.DATASOURCE);
        if (dsObj instanceof DataSource original) {
            DataSource proxy = ProxyDataSourceBuilder
                    .create(original)
                    .name("dbadvisor-ds")
                    .listener(new QueryPlanListener(original))
                    .build();
            settings.put(AvailableSettings.DATASOURCE, proxy);
            log.debug("Dbadvisor QueryPlanListener installed via datasource-proxy.");
        } else {
            log.warn("No DataSource in Hibernate settings. QueryPlanListener not installed.");
        }
    }
}
