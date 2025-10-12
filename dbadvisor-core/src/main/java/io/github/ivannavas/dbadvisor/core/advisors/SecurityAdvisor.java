package io.github.ivannavas.dbadvisor.core.advisors;

import io.github.ivannavas.dbadvisor.core.items.SecurityAdvise;

import java.util.List;

public class SecurityAdvisor extends InitialAdvisor<SecurityAdvise> {
    @Override
    public List<SecurityAdvise> getAdvise() {
        return List.of();
    }

    @Override
    public String getAdviseMessage(SecurityAdvise advise) {
        return "";
    }
}
