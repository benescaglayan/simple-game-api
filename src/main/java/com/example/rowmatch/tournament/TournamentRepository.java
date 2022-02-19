package com.example.rowmatch.tournament;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TournamentRepository extends JpaRepository<TournamentEntity, Integer> {

    Optional<TournamentEntity> getTournamentByIsActiveTrue();
}
