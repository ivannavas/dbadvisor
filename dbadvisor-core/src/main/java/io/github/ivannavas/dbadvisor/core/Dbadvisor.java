package io.github.ivannavas.dbadvisor.core;

import io.github.ivannavas.dbadvisor.core.advisors.Advisor;
import io.github.ivannavas.dbadvisor.core.advisors.InitialAdvisor;
import io.github.ivannavas.dbadvisor.core.advisors.QueryAdvisor;
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

    public void runQueryAnalysis(String rawPlan) {
        Configuration configuration = configurationBuilder.build();
        Plan plan = configuration.getPlanParser().parse(rawPlan);

        for (Advisor<?> advisor : configuration.getAdvisors()) {
            if (!(advisor instanceof QueryAdvisor<?>)) {
                continue;
            }

            processQueryAdvisor((QueryAdvisor<?>) advisor, plan);
        }
    }

    public void runInitialAnalysis() {
        Configuration configuration = configurationBuilder.build();
        Plan plan = configuration.getPlanParser().parse("");

        for (Advisor<?> advisor : configuration.getAdvisors()) {
            if (!(advisor instanceof InitialAdvisor<?>)) {
                continue;
            }

            processInitialAdvisor((InitialAdvisor<?>) advisor);
        }
    }

    private <A> void processQueryAdvisor(QueryAdvisor<A> advisor, Plan plan) {
        advisor.getAdvise(plan).forEach(advise -> log.info(advisor.getAdviseMessage(advise)));
    }

    private <A> void processInitialAdvisor(InitialAdvisor<A> advisor) {
        advisor.getAdvise().forEach(advise -> log.info(advisor.getAdviseMessage(advise)));
    }
}
