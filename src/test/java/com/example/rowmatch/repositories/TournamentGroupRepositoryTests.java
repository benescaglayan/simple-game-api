package com.example.rowmatch.repositories;

import com.example.rowmatch.tournament.group.TournamentGroupEntity;
import com.example.rowmatch.tournament.group.TournamentGroupRepository;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class TournamentGroupRepositoryTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    TournamentGroupRepository repository;

    @Test
    public void findFirstByTournamentIdAndGroupLevelOrderByCreatedAtDesc_ShouldReturnEmpty_WhenTournamentIdIsIncorrect() {
        int groupLevel = 20;
        int tournamentId = 9421;

        TournamentGroupEntity firstGroup = generateGroup(tournamentId, groupLevel, DateTime.now());
        TournamentGroupEntity secondGroup = generateGroup(tournamentId, groupLevel, DateTime.now());

        entityManager.persist(firstGroup);
        entityManager.persist(secondGroup);

        Optional<TournamentGroupEntity> actualGroup = repository.findFirstByTournamentIdAndGroupLevelOrderByCreatedAtDesc(tournamentId * 5, groupLevel);

        assertTrue(actualGroup.isEmpty());
    }

    @Test
    public void findFirstByTournamentIdAndGroupLevelOrderByCreatedAtDesc_ShouldReturnEmpty_WhenGroupLevelIsIncorrect() {
        int groupLevel = 20;
        int tournamentId = 9421;

        TournamentGroupEntity firstGroup = generateGroup(tournamentId, groupLevel, DateTime.now());
        TournamentGroupEntity secondGroup = generateGroup(tournamentId, groupLevel, DateTime.now());

        entityManager.persist(firstGroup);
        entityManager.persist(secondGroup);

        Optional<TournamentGroupEntity> actualGroup = repository.findFirstByTournamentIdAndGroupLevelOrderByCreatedAtDesc(tournamentId, groupLevel * 3);

        assertTrue(actualGroup.isEmpty());
    }


    @Test
    public void findFirstByTournamentIdAndGroupLevelOrderByCreatedAtDesc_ShouldReturnEmpty_WhenGroupLevelAndTournamentIdAreIncorrect() {
        int groupLevel = 20;
        int tournamentId = 9421;

        TournamentGroupEntity firstGroup = generateGroup(tournamentId, groupLevel, DateTime.now());
        TournamentGroupEntity secondGroup = generateGroup(tournamentId, groupLevel, DateTime.now());

        entityManager.persist(firstGroup);
        entityManager.persist(secondGroup);

        Optional<TournamentGroupEntity> actualGroup = repository.findFirstByTournamentIdAndGroupLevelOrderByCreatedAtDesc(tournamentId * 8, groupLevel * 3);

        assertTrue(actualGroup.isEmpty());
    }

    @Test
    public void findFirstByTournamentIdAndGroupLevelOrderByCreatedAtDesc_ShouldReturnGroup_WhenThereIsGroupWithTournamentIdAndGroupLevel() {
        int groupLevel = 20;
        int tournamentId = 9421;

        TournamentGroupEntity firstGroup = generateGroup(tournamentId, groupLevel, DateTime.now());
        TournamentGroupEntity secondGroup = generateGroup(tournamentId * 5, groupLevel, DateTime.now());
        TournamentGroupEntity thirdGroup = generateGroup(tournamentId * 5, groupLevel, DateTime.now());

        entityManager.persist(firstGroup);
        entityManager.persist(secondGroup);
        entityManager.persist(thirdGroup);

        Optional<TournamentGroupEntity> actualGroup = repository.findFirstByTournamentIdAndGroupLevelOrderByCreatedAtDesc(tournamentId, groupLevel);

        assertTrue(actualGroup.isPresent());
        assertEquals(groupLevel, actualGroup.get().getGroupLevel());
        assertEquals(tournamentId, actualGroup.get().getTournamentId());
    }
}
