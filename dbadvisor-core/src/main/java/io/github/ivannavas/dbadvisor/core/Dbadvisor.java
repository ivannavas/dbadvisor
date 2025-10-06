package io.github.ivannavas.dbadvisor.core;

import io.github.ivannavas.dbadvisor.core.advisors.Advisor;
import io.github.ivannavas.dbadvisor.core.items.Plan;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    public void runAnalysis(String rawPlan) {
        Configuration configuration = configurationBuilder.build();
        Plan plan = configuration.getPlanParser().parse(rawPlan);

        for (Advisor<?> advisor : configuration.getAdvisors()) {
            processAdvisor(advisor, plan);
        }
    }

    private <T> void processAdvisor(Advisor<T> advisor, Plan plan) {
        advisor.getAdvise(plan).forEach(advise -> log.info(advisor.getAdviseMessage(advise)));
    }
}
