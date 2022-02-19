package com.example.rowmatch.tournament.group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TournamentGroupRepository extends JpaRepository<TournamentGroupEntity, Integer> {
    Optional<TournamentGroupEntity> findFirstByTournamentIdAndGroupLevelOrderByCreatedAtDesc(int tournamentId, int groupLevel);
}
