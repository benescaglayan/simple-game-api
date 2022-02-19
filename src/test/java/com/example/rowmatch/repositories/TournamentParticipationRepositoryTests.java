package com.example.rowmatch.repositories;

import com.example.rowmatch.tournament.group.TournamentGroupEntity;
import com.example.rowmatch.tournament.group.TournamentGroupRepository;
import com.example.rowmatch.tournament.participation.TournamentParticipationEntity;
import com.example.rowmatch.tournament.participation.TournamentParticipationRepository;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static com.example.rowmatch.util.Generators.generateGroup;
import static com.example.rowmatch.util.Generators.generateParticipation;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class TournamentParticipationRepositoryTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    TournamentParticipationRepository repository;

    @Test
    public void existsByTournamentIdAndUserId_ShouldReturnFalse_WhenUserIdIsIncorrect() {
        int userId = 123;
        int tournamentId = 9421;

        TournamentParticipationEntity participation = new TournamentParticipationEntity();
        participation.setUserId(userId);
        participation.setTournamentId(tournamentId);

        entityManager.persist(participation);

        boolean participationExists = repository.existsByTournamentIdAndUserId(tournamentId * 5, userId);

        assertFalse(participationExists);
    }

    @Test
    public void existsByTournamentIdAndUserId_ShouldReturnFalse_WhenTournamentIdIsIncorrect() {
        int userId = 123;
        int tournamentId = 9421;

        TournamentParticipationEntity participation = new TournamentParticipationEntity();
        participation.setUserId(userId);
        participation.setTournamentId(tournamentId);

        entityManager.persist(participation);

        boolean participationExists = repository.existsByTournamentIdAndUserId(tournamentId, userId * 7);

        assertFalse(participationExists);
    }

    @Test
    public void existsByTournamentIdAndUserId_ShouldReturnFalse_WhenTournamentIdAndUserIdAreIncorrect() {
        int userId = 123;
        int tournamentId = 9421;

        TournamentParticipationEntity participation = new TournamentParticipationEntity();
        participation.setUserId(userId);
        participation.setTournamentId(tournamentId);

        entityManager.persist(participation);

        boolean participationExists = repository.existsByTournamentIdAndUserId(tournamentId * 5, userId * 3);

        assertFalse(participationExists);
    }

    @Test
    public void existsByTournamentIdAndUserId_ShouldReturnTrue_WhenTournamentIdAndUserIdAreCorrect() {
        int userId = 123;
        int tournamentId = 9421;

        TournamentParticipationEntity participation = new TournamentParticipationEntity();
        participation.setUserId(userId);
        participation.setTournamentId(tournamentId);

        entityManager.persist(participation);

        boolean participationExists = repository.existsByTournamentIdAndUserId(tournamentId, userId);

        assertTrue(participationExists);
    }

    @Test
    public void countByTournamentIdAndGroupId_ShouldReturn0_WhenGroupIdIsIncorrect() {
        int groupId = 123;
        int tournamentId = 9421;

        TournamentParticipationEntity participation = new TournamentParticipationEntity();
        participation.setGroupId(groupId);
        participation.setTournamentId(tournamentId);

        entityManager.persist(participation);

        int actualCount = repository.countByTournamentIdAndGroupId(tournamentId, groupId * 3);

        assertEquals(0, actualCount);
    }

    @Test
    public void countByTournamentIdAndGroupId_ShouldReturn0_WhenTournamentIdIsIncorrect() {
        int groupId = 123;
        int tournamentId = 9421;

        TournamentParticipationEntity participation = new TournamentParticipationEntity();
        participation.setGroupId(groupId);
        participation.setTournamentId(tournamentId);

        entityManager.persist(participation);

        int actualCount = repository.countByTournamentIdAndGroupId(tournamentId * 5, groupId);

        assertEquals(0, actualCount);
    }

    @Test
    public void countByTournamentIdAndGroupId_ShouldReturn0_WhenTournamentIdAndGroupIdAreIncorrect() {
        int groupId = 123;
        int tournamentId = 9421;

        TournamentParticipationEntity participation = new TournamentParticipationEntity();
        participation.setGroupId(groupId);
        participation.setTournamentId(tournamentId);

        entityManager.persist(participation);

        int actualCount = repository.countByTournamentIdAndGroupId(tournamentId * 5, groupId * 3);

        assertEquals(0, actualCount);
    }

    @Test
    public void countByTournamentIdAndGroupId_ShouldReturn1_WhenParticipationExistsWithTournamentIdAndGroupId() {
        int groupId = 123;
        int tournamentId = 9421;

        TournamentParticipationEntity participation = new TournamentParticipationEntity();
        participation.setGroupId(groupId);
        participation.setTournamentId(tournamentId);

        entityManager.persist(participation);

        int actualCount = repository.countByTournamentIdAndGroupId(tournamentId, groupId);

        assertEquals(1, actualCount);
    }

    @Test
    public void findByTournamentIdAndUserId_ShouldReturnEmpty_WhenUserIdIsIncorrect() {
        int userId = 123;
        int tournamentId = 9421;

        TournamentParticipationEntity participation = new TournamentParticipationEntity();
        participation.setUserId(userId);
        participation.setTournamentId(tournamentId);

        entityManager.persist(participation);

        Optional<TournamentParticipationEntity> actualParticipation = repository.findByTournamentIdAndUserId(tournamentId, userId * 3);

        assertTrue(actualParticipation.isEmpty());
    }

    @Test
    public void findByTournamentIdAndUserId_ShouldReturnEmpty_WhenTournamentIdIsIncorrect() {
        int userId = 123;
        int tournamentId = 9421;

        TournamentParticipationEntity participation = new TournamentParticipationEntity();
        participation.setUserId(userId);
        participation.setTournamentId(tournamentId);

        entityManager.persist(participation);

        Optional<TournamentParticipationEntity> actualParticipation = repository.findByTournamentIdAndUserId(tournamentId * 5, userId);

        assertTrue(actualParticipation.isEmpty());
    }

    @Test
    public void findByTournamentIdAndUserId_ShouldReturnEmpty_WhenTournamentIdAndGroupIdAreIncorrect() {
        int groupId = 123;
        int tournamentId = 9421;

        TournamentParticipationEntity participation = new TournamentParticipationEntity();
        participation.setUserId(groupId);
        participation.setTournamentId(tournamentId);

        entityManager.persist(participation);

        Optional<TournamentParticipationEntity> actualParticipation = repository.findByTournamentIdAndUserId(tournamentId * 5, groupId * 3);

        assertTrue(actualParticipation.isEmpty());
    }

    @Test
    public void findByTournamentIdAndUserId_ShouldReturnParticipation_WhenParticipationExistsWithTournamentIdAndGroupId() {
        int userId = 123;
        int tournamentId = 9421;

        TournamentParticipationEntity participation = new TournamentParticipationEntity();
        participation.setUserId(userId);
        participation.setTournamentId(tournamentId);

        entityManager.persist(participation);

        Optional<TournamentParticipationEntity> actualParticipation  = repository.findByTournamentIdAndUserId(tournamentId, userId);

        assertTrue(actualParticipation.isPresent());
        assertEquals(userId, actualParticipation.get().getUserId());
        assertEquals(tournamentId, actualParticipation.get().getTournamentId());
    }
}
