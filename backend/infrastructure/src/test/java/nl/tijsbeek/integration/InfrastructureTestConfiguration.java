package nl.tijsbeek.integration;

import nl.tijsbeek.entities.BoardJPA;
import nl.tijsbeek.repositories.domain.BoardRepositoryImpl;
import nl.tijsbeek.repositories.jpa.BoardJpaRepository;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration(proxyBeanMethods = false)
@EnableAutoConfiguration
@EntityScan(basePackageClasses = BoardJPA.class)
@EnableJpaRepositories(basePackageClasses = BoardJpaRepository.class)
@ComponentScan(basePackageClasses = BoardRepositoryImpl.class)
public class InfrastructureTestConfiguration {
}
