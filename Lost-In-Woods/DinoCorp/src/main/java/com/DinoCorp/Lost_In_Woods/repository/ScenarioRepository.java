package com.DinoCorp.Lost_In_Woods.repository;
import com.DinoCorp.Lost_In_Woods.model.Scenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScenarioRepository extends JpaRepository<Scenario, Long> {}