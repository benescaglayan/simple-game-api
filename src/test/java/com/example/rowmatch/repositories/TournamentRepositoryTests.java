package com.example.rowmatch.repositories;

import com.example.rowmatch.tournament.TournamentEntity;
import com.example.rowmatch.tournament.TournamentRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

@DataJpaTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class TournamentRepositoryTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    TournamentRepository repository;

    @Test
    public void getTournamentByIsActiveTrue_ShouldReturnEmpty_WhenThereIsNoActiveTournament() {
        TournamentEntity tournament = new TournamentEntity();
        tournament.setActive(false);

        entityManager.persist(tournament);

        Optional<TournamentEntity> activeTournaments = repository.getTournamentByIsActiveTrue();

        assertTrue(activeTournaments.isEmpty());
    }

    @Test
    public void getTournamentByIsActiveTrue_ShouldReturnActiveTournament_WhenThereIsActiveTournament() {
        TournamentEntity tournament = new TournamentEntity();
        tournament.setActive(true);

        entityManager.persist(tournament);

        Optional<TournamentEntity> actualActiveTournament = repository.getTournamentByIsActiveTrue();

        assertTrue(actualActiveTournament.isPresent());
        assertTrue(actualActiveTournament.get().isActive());
    }
}
