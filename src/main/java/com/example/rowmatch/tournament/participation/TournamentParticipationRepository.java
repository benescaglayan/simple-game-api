package com.example.rowmatch.tournament.participation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentParticipationRepository extends JpaRepository<TournamentParticipationEntity, Integer> {

    boolean existsByTournamentIdAndUserId(int tournamentId, int userId);

    int countByTournamentIdAndGroupId(int tournamentId, int groupId);

    List<TournamentParticipationEntity> findAllByGroupIdOrderByUserScoreDesc(int groupId);

    Optional<TournamentParticipationEntity> findByTournamentIdAndUserId(int tournamentId, int userId);
}
