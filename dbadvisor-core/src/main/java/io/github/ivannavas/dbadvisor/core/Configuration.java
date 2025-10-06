package io.github.ivannavas.dbadvisor.core;

import io.github.ivannavas.dbadvisor.core.advisors.Advisor;
import io.github.ivannavas.dbadvisor.core.parsers.PlanParser;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@NoArgsConstructor
@Getter
public class Configuration {
    @NonNull
    @Builder.Default
    private List<Advisor<?>> advisors = List.of();

    @NonNull
    private PlanParser planParser;
}
