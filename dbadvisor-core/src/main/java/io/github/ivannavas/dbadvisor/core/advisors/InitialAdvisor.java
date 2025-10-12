package io.github.ivannavas.dbadvisor.core.advisors;

import java.util.List;

public abstract class InitialAdvisor<A> implements Advisor<A>{
    public abstract List<A> getAdvise();
}
