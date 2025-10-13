package io.github.ivannavas.dbadvisor.core;

import io.github.ivannavas.dbadvisor.core.advisors.Advisor;
import io.github.ivannavas.dbadvisor.core.items.Plan;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class Dbadvisor {
    private static final Dbadvisor INSTANCE = new Dbadvisor();
    private static final Configuration.ConfigurationBuilder<?, ?> configurationBuilder = Configuration.builder();

    public static Dbadvisor getInstance() {
        return INSTANCE;
    }

    public Configuration.ConfigurationBuilder<?, ?> configure() {
        return configurationBuilder;
    }

    public Configuration getConfiguration() {
        return configurationBuilder.build();
    }

    public void runQueryAnalysis(String rawPlan, String query) {
        Configuration configuration = configurationBuilder.build();
        Plan plan = configuration.getPlanParser().parse(rawPlan, query);

        for (Advisor<?> advisor : configuration.getAdvisors()) {
            processQueryAdvisor(advisor, plan);
        }
    }

    public void runInitialAnalysis(Function<String, List<Object>> queryExecutor) {
        Configuration configuration = configurationBuilder.build();

        for (Advisor<?> advisor : configuration.getAdvisors()) {
            processInitialAdvisor(advisor, queryExecutor);
        }
    }

    private <A> void processQueryAdvisor(Advisor<A> advisor, Plan plan) {
        advisor.onQuery(plan).forEach(advise -> log.warn(advisor.getAdviseMessage(advise)));
    }

    private <A> void processInitialAdvisor(Advisor<A> advisor, Function<String, List<Object>> queryExecutor) {
        advisor.onInit(queryExecutor).forEach(advise -> log.warn(advisor.getAdviseMessage(advise)));
    }
}
