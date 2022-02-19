package com.example.rowmatch.services;

import com.example.rowmatch.exception.GroupNotFoundException;
import com.example.rowmatch.exception.ParticipationNotFoundException;
import com.example.rowmatch.tournament.group.TournamentGroupService;
import com.example.rowmatch.tournament.participation.TournamentParticipationEntity;
import com.example.rowmatch.tournament.participation.TournamentParticipationRepository;
import com.example.rowmatch.tournament.participation.TournamentParticipationService;
import com.example.rowmatch.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static com.example.rowmatch.util.Generators.*;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TournamentParticipationServiceTests {

    @Mock
    TournamentParticipationRepository tournamentParticipationRepository;

    @Mock
    TournamentGroupService tournamentGroupService;

    @Mock
    UserService userService;

    @InjectMocks
    TournamentParticipationService tournamentParticipationService;

    private static final int TOURNAMENT_PARTICIPATION_FEE = 1000;

    @Test
    void participate_shouldCreateParticipationAndUpdateUser() {
        int userId = 123;
        int groupId = 99;
        int tournamentId = 323;

        doAnswer(returnsFirstArg()).when(tournamentParticipationRepository).save(argThat((TournamentParticipationEntity participation) -> participation.getTournamentId() == tournamentId && participation.getUserId() == userId && participation.getGroupId() == groupId));
        doNothing().when(userService).updateAfterParticipation(userId, tournamentId, TOURNAMENT_PARTICIPATION_FEE);

        tournamentParticipationService.participate(tournamentId, groupId, userId);

        verify(tournamentParticipationRepository, times(1)).save(argThat((TournamentParticipationEntity participation) -> participation.getTournamentId() == tournamentId && participation.getUserId() == userId && participation.getGroupId() == groupId));
        verify(userService, times(1)).updateAfterParticipation(userId, tournamentId, TOURNAMENT_PARTICIPATION_FEE);
    }

    @Test
    void incrementTournamentScore_shouldThrowParticipationNotFoundException_WhenParticipationDoesNotExist() throws ParticipationNotFoundException {
        int userId = 123;
        int tournamentId = 323;

        doReturn(empty()).when(tournamentParticipationRepository).findByTournamentIdAndUserId(tournamentId, userId);

        assertThrows(ParticipationNotFoundException.class, () -> tournamentParticipationService.incrementTournamentScore(tournamentId, userId));

        verify(tournamentParticipationRepository, times(1)).findByTournamentIdAndUserId(tournamentId, userId);
        verify(tournamentParticipationRepository, never()).save(any(TournamentParticipationEntity.class));
    }

    @Test
    void incrementTournamentScore_shouldIncrementTournamentScoreBy1_WhenParticipationExists() throws ParticipationNotFoundException {
        int userId = 123;
        int groupId = 321;
        int tournamentId = 323;
        int userScore = 888;

        TournamentParticipationEntity participationToBeUpdated = spy(generateParticipation(312, groupId, tournamentId, userId, false, userScore));

        doReturn(of(participationToBeUpdated)).when(tournamentParticipationRepository).findByTournamentIdAndUserId(tournamentId, userId);

        tournamentParticipationService.incrementTournamentScore(tournamentId, userId);

        verify(tournamentParticipationRepository, times(1)).findByTournamentIdAndUserId(tournamentId, userId);
        verify(participationToBeUpdated, times(1)).setUserScore(userScore + 1);
        verify(tournamentParticipationRepository, times(1)).save(argThat((TournamentParticipationEntity participation) -> participation.getTournamentId() == tournamentId && participation.getUserId() == userId && participation.getGroupId() == groupId));
    }

    @Test
    void getParticipationCountByTournamentIdAndGroupId_shouldReturnTheCount() {
        int expectedCount = 333;
        int groupId = 321;
        int tournamentId = 323;

        doReturn(expectedCount).when(tournamentParticipationRepository).countByTournamentIdAndGroupId(tournamentId, groupId);

        int actualCount = tournamentParticipationService.getParticipationCountByTournamentIdAndGroupId(tournamentId, groupId);

        assertEquals(expectedCount, actualCount);

        verify(tournamentParticipationRepository, times(1)).countByTournamentIdAndGroupId(tournamentId, groupId);
    }

    @Test
    void existsByTournamentIdAndUserId_shouldReturnExistenceInfo() {
        boolean expectedExists = false;
        int userId = 321;
        int tournamentId = 323;

        doReturn(expectedExists).when(tournamentParticipationRepository).existsByTournamentIdAndUserId(tournamentId, userId);

        boolean actualExists = tournamentParticipationService.existsByTournamentIdAndUserId(tournamentId, userId);

        assertEquals(expectedExists, actualExists);

        verify(tournamentParticipationRepository, times(1)).existsByTournamentIdAndUserId(tournamentId, userId);
    }

    @Test
    void findAllByGroupIdOrderByUserScoreDesc_shouldReturnListOfParticipations() throws GroupNotFoundException {
        int firstParticipationScore = 321;
        int secondParticipationScore = 150;

        int groupId = 321;

        TournamentParticipationEntity firstParticipation = generateParticipation(123, groupId, 421, 3812, false, firstParticipationScore);
        TournamentParticipationEntity secondParticipation = generateParticipation(123, groupId, 421, 3812, false, secondParticipationScore);

        List<TournamentParticipationEntity> expectedParticipations = Arrays.asList(firstParticipation, secondParticipation);

        doReturn(expectedParticipations).when(tournamentParticipationRepository).findAllByGroupIdOrderByUserScoreDesc(groupId);

        List<TournamentParticipationEntity> actualParticipations = tournamentParticipationService.findAllByGroupIdOrderByUserScoreDesc(groupId);

        assertEquals(expectedParticipations.size(), actualParticipations.size());
        assertEquals(expectedParticipations.get(0).getUserScore(), actualParticipations.get(0).getUserScore());
        assertEquals(expectedParticipations.get(1).getUserScore(), actualParticipations.get(1).getUserScore());

        verify(tournamentParticipationRepository, times(1)).findAllByGroupIdOrderByUserScoreDesc(groupId);
    }

    @Test
    void isRewardClaimed_shouldThrowParticipationNotFoundException_WhenNoParticipationsExist() {
        int userId = 123;
        int tournamentId = 323;

        doReturn(empty()).when(tournamentParticipationRepository).findByTournamentIdAndUserId(tournamentId, userId);

        assertThrows(ParticipationNotFoundException.class, () -> tournamentParticipationService.isRewardClaimed(tournamentId, userId));

        verify(tournamentParticipationRepository, times(1)).findByTournamentIdAndUserId(tournamentId, userId);
    }

    @Test
    void isRewardClaimed_shouldReturnClaimInfo_WhenParticipationExists() throws ParticipationNotFoundException {
        int userId = 123;
        int tournamentId = 323;
        boolean expectedIsRewardClaimed = true;

        TournamentParticipationEntity participation = spy(generateParticipation(312, 42142, tournamentId, userId, expectedIsRewardClaimed, 321));

        doReturn(of(participation)).when(tournamentParticipationRepository).findByTournamentIdAndUserId(tournamentId, userId);

        boolean actualIsRewardClaimed =  tournamentParticipationService.isRewardClaimed(tournamentId, userId);

        assertEquals(expectedIsRewardClaimed, actualIsRewardClaimed);

        verify(tournamentParticipationRepository, times(1)).findByTournamentIdAndUserId(tournamentId, userId);
        verify(participation, times(1)).isRewardClaimed();
    }

    @Test
    void claimReward_shouldThrowParticipationNotFoundException_WhenNoParticipationsExist() {
        int userId = 123;
        int tournamentId = 323;

        doReturn(empty()).when(tournamentParticipationRepository).findByTournamentIdAndUserId(tournamentId, userId);

        assertThrows(ParticipationNotFoundException.class, () -> tournamentParticipationService.claimReward(tournamentId, userId));

        verify(tournamentParticipationRepository, times(1)).findByTournamentIdAndUserId(tournamentId, userId);
        verify(tournamentParticipationRepository, never()).save(any(TournamentParticipationEntity.class));
    }

    @Test
    void claimReward_shouldClaimRewardAndSaveParticipation_WhenParticipationExists() throws ParticipationNotFoundException {
        int userId = 123;
        int tournamentId = 323;

        TournamentParticipationEntity participationToBeClaimed = spy(generateParticipation(312, 42142, tournamentId, userId, false, 321));

        doReturn(of(participationToBeClaimed)).when(tournamentParticipationRepository).findByTournamentIdAndUserId(tournamentId, userId);
        doAnswer(returnsFirstArg()).when(tournamentParticipationRepository).save(argThat((TournamentParticipationEntity participation) -> participation.getTournamentId() == tournamentId && participation.getUserId() == userId));

        tournamentParticipationService.claimReward(tournamentId, userId);

        verify(tournamentParticipationRepository, times(1)).findByTournamentIdAndUserId(tournamentId, userId);
        verify(participationToBeClaimed, times(1)).setRewardClaimed(true);
        verify(tournamentParticipationRepository, times(1)).save(argThat((TournamentParticipationEntity participation) -> participation.getTournamentId() == tournamentId && participation.getUserId() == userId));
    }

    @Test
    void getRankByTournamentIdAndUserId_shouldThrowParticipationNotFoundException_WhenNoParticipationsExist() {
        int userId = 123;
        int tournamentId = 323;

        doReturn(empty()).when(tournamentParticipationRepository).findByTournamentIdAndUserId(tournamentId, userId);

        assertThrows(ParticipationNotFoundException.class, () -> tournamentParticipationService.getRankByTournamentIdAndUserId(tournamentId, userId));

        verify(tournamentParticipationRepository, times(1)).findByTournamentIdAndUserId(tournamentId, userId);
        verify(tournamentParticipationRepository, never()).findAllByGroupIdOrderByUserScoreDesc(anyInt());
    }

    @Test
    void getRankByTournamentIdAndUserId_shouldReturnIndexOfParticipationInLeaderboardPlus1_WhenParticipationsExistInGroup() throws ParticipationNotFoundException, GroupNotFoundException {
        int userId = 123;
        int groupId = 22;
        int tournamentId = 323;

        TournamentParticipationEntity firstParticipation = generateParticipation(userId, groupId, 421, 3812, false, 360);
        TournamentParticipationEntity secondParticipation = generateParticipation(123, groupId, 421, 3812, false, 125);

        List<TournamentParticipationEntity> participations = Arrays.asList(firstParticipation, secondParticipation);

        doReturn(of(firstParticipation)).when(tournamentParticipationRepository).findByTournamentIdAndUserId(tournamentId, userId);
        doReturn(participations).when(tournamentParticipationRepository).findAllByGroupIdOrderByUserScoreDesc(groupId);

        int index = tournamentParticipationService.getRankByTournamentIdAndUserId(tournamentId, userId);

        assertEquals(participations.indexOf(firstParticipation) + 1, index);

        verify(tournamentParticipationRepository, times(1)).findByTournamentIdAndUserId(tournamentId, userId);
        verify(tournamentParticipationRepository, times(1)).findAllByGroupIdOrderByUserScoreDesc(groupId);
    }
}
