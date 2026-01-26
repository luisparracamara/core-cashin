package com.core.cashin.payment.methods.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

@Configuration
@Slf4j
public class CircuitBreakerLogConfig {

    @Bean
    public RegistryEventConsumer<CircuitBreaker> myRegistryEventConsumer() {
        return new RegistryEventConsumer<CircuitBreaker>() {
            @Override
            public void onEntryAddedEvent(@NonNull EntryAddedEvent<CircuitBreaker> entryAddedEvent) {
                entryAddedEvent.getAddedEntry().getEventPublisher()
                        .onStateTransition(event -> log.info("[CIRCUIT BREAKER] {} Status changed: {} -> {}",
                                event.getCircuitBreakerName(),
                                event.getStateTransition().getFromState(),
                                event.getStateTransition().getToState()));
            }


            @Override
            public void onEntryRemovedEvent(@NonNull EntryRemovedEvent<CircuitBreaker> entryRemoveEvent) {
                log.info("[CIRCUIT BREAKER] Config removed for: {}", entryRemoveEvent.getRemovedEntry().getName());
            }

            @Override
            public void onEntryReplacedEvent(@NonNull EntryReplacedEvent<CircuitBreaker> entryReplacedEvent) {
                log.info("[CIRCUIT BREAKER] Config replaced for: {}", entryReplacedEvent.getNewEntry().getName());
            }
        };
    }

}
