package com.example.rowmatch.services;

import com.example.rowmatch.exception.*;
import com.example.rowmatch.tournament.TournamentEntity;
import com.example.rowmatch.tournament.TournamentRepository;
import com.example.rowmatch.tournament.TournamentService;
import com.example.rowmatch.tournament.group.TournamentGroupService;
import com.example.rowmatch.tournament.participation.TournamentParticipationDto;
import com.example.rowmatch.tournament.participation.TournamentParticipationEntity;
import com.example.rowmatch.tournament.participation.TournamentParticipationService;
import com.example.rowmatch.tournament.response.GetLeaderboardResponse;
import com.example.rowmatch.user.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static com.example.rowmatch.util.Generators.*;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TournamentServiceTests {

    @Mock
    UserService userService;

    @Mock
    TournamentRepository tournamentRepository;

    @Mock
    TournamentGroupService tournamentGroupService;

    @Mock
    TournamentParticipationService tournamentParticipationService;

    @InjectMocks
    TournamentService tournamentService;

    private final Random random = new Random();

    private static final int TOURNAMENT_MINIMUM_ENTRY_LEVEL = 20;
    private static final int TOURNAMENT_MINIMUM_ENTRY_COINS = 1000;
    private static final int TOURNAMENT_FIRST_RANK_REWARD = 10000;
    private static final int TOURNAMENT_SECOND_RANK_REWARD = 5000;
    private static final int TOURNAMENT_THIRD_RANK_REWARD = 3000;
    private static final int TOURNAMENT_NON_PODIUM_REWARD = 1000;
    private static final int TOURNAMENT_LAST_REWARD_RANK = 10;

    @Test
    void create_shouldCreateAndSaveTournament() {
        doAnswer(returnsFirstArg()).when(tournamentRepository).save(argThat(TournamentEntity::isActive));

        tournamentService.create();

        verify(tournamentRepository, times(1)).save(argThat(TournamentEntity::isActive));
    }

    @Test
    void participate_shouldThrowNoActiveTournamentException_WhenThereIsNoActiveTournament() throws ParticipationNotFoundException, UserNotFoundException, GroupNotFoundException {
        doReturn(empty()).when(tournamentRepository).getTournamentByIsActiveTrue();

        assertThrows(NoActiveTournamentException.class, () -> tournamentService.participate(1231));

        verify(tournamentRepository, times(1)).getTournamentByIsActiveTrue();
        verify(tournamentParticipationService, never()).existsByTournamentIdAndUserId(anyInt(), anyInt());
        verify(userService, never()).get(anyInt());
        verify(tournamentParticipationService, never()).getRankByTournamentIdAndUserId(anyInt(), anyInt());
        verify(tournamentParticipationService, never()).isRewardClaimed(anyInt(), anyInt());
        verify(tournamentParticipationService, never()).participate(anyInt(), anyInt(), anyInt());
        verify(tournamentParticipationService, never()).findAllByGroupIdOrderByUserScoreDesc(anyInt());
    }

    @Test
    void participate_shouldThrowUserAlreadyJoinedTournamentException_WhenUserHasAlreadyJoinedActiveTournament() throws ParticipationNotFoundException, UserNotFoundException, GroupNotFoundException {
        int userId = 1;
        int tournamentId = 100;

        TournamentEntity tournament = generateTournament(tournamentId, false);

        doReturn(of(tournament)).when(tournamentRepository).getTournamentByIsActiveTrue();
        doReturn(true).when(tournamentParticipationService).existsByTournamentIdAndUserId(tournamentId, userId);

        assertThrows(UserAlreadyJoinedTournamentException.class, () -> tournamentService.participate(userId));

        verify(tournamentRepository, times(1)).getTournamentByIsActiveTrue();
        verify(tournamentParticipationService, times(1)).existsByTournamentIdAndUserId(tournamentId, userId);
        verify(userService, never()).get(anyInt());
        verify(tournamentParticipationService, never()).getRankByTournamentIdAndUserId(anyInt(), anyInt());
        verify(tournamentParticipationService, never()).isRewardClaimed(anyInt(), anyInt());
        verify(tournamentParticipationService, never()).participate(anyInt(), anyInt(), anyInt());
        verify(tournamentParticipationService, never()).findAllByGroupIdOrderByUserScoreDesc(anyInt());
    }

    @Test
    void participate_shouldThrowRankTooLowForTournamentException_WhenUserLevelIsBelowCutoff() throws UserNotFoundException, ParticipationNotFoundException, GroupNotFoundException {
        int userId = 1;
        int tournamentId = 100;
        int coins = 7500;
        int level = random.nextInt(TOURNAMENT_MINIMUM_ENTRY_LEVEL);
        Integer lastEnteredTournamentId = null;

        UserDto user = generateUserDto(userId, coins, level, lastEnteredTournamentId);
        TournamentEntity tournament = generateTournament(tournamentId, false);

        doReturn(of(tournament)).when(tournamentRepository).getTournamentByIsActiveTrue();
        doReturn(false).when(tournamentParticipationService).existsByTournamentIdAndUserId(tournamentId, userId);
        doReturn(user).when(userService).get(userId);

        assertThrows(RankTooLowForTournamentException.class, () -> tournamentService.participate(userId));

        verify(tournamentRepository, times(1)).getTournamentByIsActiveTrue();
        verify(tournamentParticipationService, times(1)).existsByTournamentIdAndUserId(tournamentId, userId);
        verify(userService, times(1)).get(userId);
        verify(tournamentParticipationService, never()).getRankByTournamentIdAndUserId(anyInt(), anyInt());
        verify(tournamentParticipationService, never()).isRewardClaimed(anyInt(), anyInt());
        verify(tournamentParticipationService, never()).participate(anyInt(), anyInt(), anyInt());
        verify(tournamentParticipationService, never()).findAllByGroupIdOrderByUserScoreDesc(anyInt());
    }

    @Test
    void participate_shouldThrowNotEnoughCoinsForTournamentException_WhenUserHasTooFewCoins() throws UserNotFoundException, ParticipationNotFoundException, GroupNotFoundException {
        int userId = 1;
        int tournamentId = 100;
        int coins = random.nextInt(TOURNAMENT_MINIMUM_ENTRY_COINS);
        int level = TOURNAMENT_MINIMUM_ENTRY_LEVEL + 2;
        Integer lastEnteredTournamentId = null;

        UserDto user = generateUserDto(userId, coins, level, lastEnteredTournamentId);
        TournamentEntity tournament = generateTournament(tournamentId, false);

        doReturn(of(tournament)).when(tournamentRepository).getTournamentByIsActiveTrue();
        doReturn(false).when(tournamentParticipationService).existsByTournamentIdAndUserId(tournamentId, userId);
        doReturn(user).when(userService).get(userId);

        assertThrows(NotEnoughCoinsForTournamentException.class, () -> tournamentService.participate(userId));

        verify(tournamentRepository, times(1)).getTournamentByIsActiveTrue();
        verify(tournamentParticipationService, times(1)).existsByTournamentIdAndUserId(tournamentId, userId);
        verify(userService, times(1)).get(userId);
        verify(tournamentParticipationService, never()).getRankByTournamentIdAndUserId(anyInt(), anyInt());
        verify(tournamentParticipationService, never()).isRewardClaimed(anyInt(), anyInt());
        verify(tournamentParticipationService, never()).participate(anyInt(), anyInt(), anyInt());
        verify(tournamentParticipationService, never()).findAllByGroupIdOrderByUserScoreDesc(anyInt());
    }

    @Test
    void participate_shouldThrowLastEarnedRewardNotClaimedException_WhenUserHasUnclaimedRewardFromLastTournament() throws UserNotFoundException, ParticipationNotFoundException, GroupNotFoundException {
        int userId = 1;
        int tournamentId = 100;
        int coins = TOURNAMENT_MINIMUM_ENTRY_COINS * 2;
        int level = TOURNAMENT_MINIMUM_ENTRY_LEVEL + 2;
        int lastEnteredTournamentRank = random.nextInt(TOURNAMENT_LAST_REWARD_RANK);
        int lastEnteredTournamentId = 282;

        UserDto user = generateUserDto(userId, coins, level, lastEnteredTournamentId);
        TournamentEntity tournament = generateTournament(tournamentId, false);

        doReturn(of(tournament)).when(tournamentRepository).getTournamentByIsActiveTrue();
        doReturn(false).when(tournamentParticipationService).existsByTournamentIdAndUserId(tournamentId, userId);
        doReturn(user).when(userService).get(userId);
        doReturn(lastEnteredTournamentRank).when(tournamentParticipationService).getRankByTournamentIdAndUserId(lastEnteredTournamentId, userId);
        doReturn(false).when(tournamentParticipationService).isRewardClaimed(lastEnteredTournamentId, userId);

        assertThrows(LastEarnedRewardNotClaimedException.class, () -> tournamentService.participate(userId));

        verify(tournamentRepository, times(1)).getTournamentByIsActiveTrue();
        verify(tournamentParticipationService, times(1)).existsByTournamentIdAndUserId(tournamentId, userId);
        verify(userService, times(1)).get(userId);
        verify(tournamentParticipationService, times(1)).getRankByTournamentIdAndUserId(lastEnteredTournamentId, userId);
        verify(tournamentParticipationService, times(1)).isRewardClaimed(lastEnteredTournamentId, userId);
        verify(tournamentParticipationService, never()).participate(anyInt(), anyInt(), anyInt());
        verify(tournamentParticipationService, never()).findAllByGroupIdOrderByUserScoreDesc(anyInt());
    }

    @Test
    void participate_shouldParticipateAndReturnLeaderboard_WhenAllConditionsAreMetWithLastEnteredTournamentIdNull() throws UserNotFoundException, ParticipationNotFoundException, RankTooLowForTournamentException, NoActiveTournamentException, NotEnoughCoinsForTournamentException, UserAlreadyJoinedTournamentException, LastEarnedRewardNotClaimedException, GroupNotFoundException {
        int userId = 1;
        int groupId = 25;
        int tournamentId = 100;
        int participationId = 9241;
        int coins = TOURNAMENT_MINIMUM_ENTRY_COINS * 2;
        int level = TOURNAMENT_MINIMUM_ENTRY_LEVEL + 2;
        boolean isRewardClaimed = false;
        int userScore = 0;
        Integer lastEnteredTournamentId = null;

        UserDto user = generateUserDto(userId, coins, level, lastEnteredTournamentId);
        TournamentEntity tournament = generateTournament(tournamentId, false);
        TournamentParticipationEntity participation = generateParticipation(participationId, groupId, tournamentId, userId, isRewardClaimed, userScore);

        doReturn(of(tournament)).when(tournamentRepository).getTournamentByIsActiveTrue();
        doReturn(false).when(tournamentParticipationService).existsByTournamentIdAndUserId(tournamentId, userId);
        doReturn(user).when(userService).get(userId);
        doReturn(groupId).when(tournamentGroupService).getGroupIdForUserLevel(user.getLevel(), tournamentId);
        doNothing().when(tournamentParticipationService).participate(tournamentId, groupId, userId);
        doReturn(Collections.singletonList(new TournamentParticipationDto(participation))).when(tournamentParticipationService).findAllByGroupIdOrderByUserScoreDesc(groupId);

        GetLeaderboardResponse actualLeaderboard = tournamentService.participate(userId);

        assertEquals(1, actualLeaderboard.participations.size());
        assertEquals(userId, actualLeaderboard.participations.get(0).getUserId());
        assertEquals(groupId, actualLeaderboard.participations.get(0).getGroupId());
        assertEquals(tournamentId, actualLeaderboard.participations.get(0).getTournamentId());
        assertEquals(isRewardClaimed, actualLeaderboard.participations.get(0).isRewardClaimed());
        assertEquals(userScore, actualLeaderboard.participations.get(0).getUserScore());

        verify(tournamentRepository, times(1)).getTournamentByIsActiveTrue();
        verify(tournamentParticipationService, times(1)).existsByTournamentIdAndUserId(tournamentId, userId);
        verify(userService, times(1)).get(userId);
        verify(tournamentParticipationService, never()).getRankByTournamentIdAndUserId(anyInt(), anyInt());
        verify(tournamentParticipationService, never()).isRewardClaimed(anyInt(), anyInt());
        verify(tournamentParticipationService, times(1)).participate(tournamentId, groupId, userId);
        verify(tournamentParticipationService, times(1)).findAllByGroupIdOrderByUserScoreDesc(groupId);
    }

    @Test
    void participate_shouldParticipateAndReturnLeaderboard_WhenAllConditionsAreMetWithLastEnteredTournamentRankNotRewarded() throws UserNotFoundException, ParticipationNotFoundException, RankTooLowForTournamentException, NoActiveTournamentException, NotEnoughCoinsForTournamentException, UserAlreadyJoinedTournamentException, LastEarnedRewardNotClaimedException, GroupNotFoundException {
        int userId = 1;
        int groupId = 25;
        int tournamentId = 100;
        int participationId = 9241;
        int userScore = 445;
        boolean isRewardClaimed = false;
        int coins = TOURNAMENT_MINIMUM_ENTRY_COINS * 2;
        int level = TOURNAMENT_MINIMUM_ENTRY_LEVEL + 2;
        int lastEnteredTournamentId = 323;

        UserDto user = generateUserDto(userId, coins, level, lastEnteredTournamentId);
        TournamentEntity tournament = generateTournament(tournamentId, false);
        TournamentParticipationEntity participation = generateParticipation(participationId, groupId, tournamentId, userId, isRewardClaimed, userScore);

        doReturn(of(tournament)).when(tournamentRepository).getTournamentByIsActiveTrue();
        doReturn(false).when(tournamentParticipationService).existsByTournamentIdAndUserId(tournamentId, userId);
        doReturn(user).when(userService).get(userId);
        doReturn(TOURNAMENT_LAST_REWARD_RANK * 2).when(tournamentParticipationService).getRankByTournamentIdAndUserId(lastEnteredTournamentId, userId);
        doReturn(groupId).when(tournamentGroupService).getGroupIdForUserLevel(user.getLevel(), tournamentId);
        doNothing().when(tournamentParticipationService).participate(tournamentId, groupId, userId);
        doReturn(Collections.singletonList(new TournamentParticipationDto(participation))).when(tournamentParticipationService).findAllByGroupIdOrderByUserScoreDesc(groupId);

        GetLeaderboardResponse actualLeaderboard = tournamentService.participate(userId);

        assertEquals(1, actualLeaderboard.participations.size());
        assertEquals(userId, actualLeaderboard.participations.get(0).getUserId());
        assertEquals(groupId, actualLeaderboard.participations.get(0).getGroupId());
        assertEquals(tournamentId, actualLeaderboard.participations.get(0).getTournamentId());
        assertEquals(isRewardClaimed, actualLeaderboard.participations.get(0).isRewardClaimed());
        assertEquals(userScore, actualLeaderboard.participations.get(0).getUserScore());

        verify(tournamentRepository, times(1)).getTournamentByIsActiveTrue();
        verify(tournamentParticipationService, times(1)).existsByTournamentIdAndUserId(tournamentId, userId);
        verify(userService, times(1)).get(userId);
        verify(tournamentParticipationService, times(1)).getRankByTournamentIdAndUserId(lastEnteredTournamentId, userId);
        verify(tournamentParticipationService, never()).isRewardClaimed(anyInt(), anyInt());
        verify(tournamentParticipationService, times(1)).participate(tournamentId, groupId, userId);
        verify(tournamentParticipationService, times(1)).findAllByGroupIdOrderByUserScoreDesc(groupId);
    }

    @Test
    void getUserRank_shouldReturnUserRank() throws ParticipationNotFoundException, GroupNotFoundException {
        int userId = 775;
        int tournamentId = 332;
        int rank = 45;

        doReturn(rank).when(tournamentParticipationService).getRankByTournamentIdAndUserId(tournamentId, userId);

        int actualRank = tournamentService.getUserRank(tournamentId, userId);

        assertEquals(rank, actualRank);

        verify(tournamentParticipationService, times(1)).getRankByTournamentIdAndUserId(tournamentId, userId);
    }

    @Test
    void isActive_shouldReturnIsActiveFalse_WhenTournamentIsNull() {
        int tournamentId = 332;

        doReturn(empty()).when(tournamentRepository).findById(tournamentId);

        boolean actualIsActive = tournamentService.isActive(tournamentId);

        assertFalse(actualIsActive);
    }

    @Test
    void isActive_shouldReturnIsActiveFalse_WhenTournamentIsNotActive() {
        int tournamentId = 332;

        TournamentEntity tournament = generateTournament(tournamentId, false);

        doReturn(of(tournament)).when(tournamentRepository).findById(tournamentId);

        boolean actualIsActive = tournamentService.isActive(tournamentId);

        assertFalse(actualIsActive);
    }

    @Test
    void isActive_shouldReturnIsActiveTrue_WhenTournamentIsNotNullAndIsActive() {
        int tournamentId = 332;

        TournamentEntity tournament = generateTournament(tournamentId, true);

        doReturn(of(tournament)).when(tournamentRepository).findById(tournamentId);

        boolean actualIsActive = tournamentService.isActive(tournamentId);

        assertTrue(actualIsActive);
    }

    @Test
    void deactivatePreviousTournament_shouldReturn_WhenActiveTournamentDoesNotExist() {
        doReturn(empty()).when(tournamentRepository).getTournamentByIsActiveTrue();

        tournamentService.deactivatePreviousTournament();

        verify(tournamentRepository, times(1)).getTournamentByIsActiveTrue();
        verify(tournamentRepository, never()).save(any(TournamentEntity.class));
    }

    @Test
    void deactivatePreviousTournament_shouldDeactivatePrevious_WhenActiveTournamentExists() {
        int tournamentId = 9421;

        doReturn(of(generateTournament(9421, true))).when(tournamentRepository).getTournamentByIsActiveTrue();
        doAnswer(returnsFirstArg()).when(tournamentRepository).save(argThat((TournamentEntity tournament) -> !tournament.isActive() && tournament.getId() == tournamentId));

        tournamentService.deactivatePreviousTournament();

        verify(tournamentRepository, times(1)).getTournamentByIsActiveTrue();
        verify(tournamentRepository, times(1)).save(argThat((TournamentEntity tournament) -> !tournament.isActive() && tournament.getId() == tournamentId));
    }

    @Test
    void incrementTournamentScore_shouldReturn_WhenTournamentIsNotFound() throws ParticipationNotFoundException {
        int userId = 12;
        int tournamentId = 332;

        doReturn(empty()).when(tournamentRepository).findById(tournamentId);

        tournamentService.incrementTournamentScore(tournamentId, userId);

        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(tournamentParticipationService, never()).incrementTournamentScore(anyInt(), anyInt());
    }

    @Test
    void incrementTournamentScore_shouldReturn_WhenTournamentIsNotActive() throws ParticipationNotFoundException {
        int userId = 12;
        int tournamentId = 332;

        TournamentEntity tournament = generateTournament(tournamentId, false);

        doReturn(of(tournament)).when(tournamentRepository).findById(tournamentId);

        tournamentService.incrementTournamentScore(tournamentId, userId);

        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(tournamentParticipationService, never()).incrementTournamentScore(anyInt(), anyInt());
    }

    @Test
    void incrementTournamentScore_shouldIncrementTournamentScore_WhenTournamentExistsAndActive() throws ParticipationNotFoundException {
        int userId = 12;
        int tournamentId = 332;

        TournamentEntity tournament = generateTournament(tournamentId, true);

        doReturn(of(tournament)).when(tournamentRepository).findById(tournamentId);
        doNothing().when(tournamentParticipationService).incrementTournamentScore(tournamentId, userId);

        tournamentService.incrementTournamentScore(tournamentId, userId);

        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(tournamentParticipationService, times(1)).incrementTournamentScore(tournamentId, userId);
    }

    @Test
    void claimReward_shouldThrowOngoingTournamentClaimedException_WhenClaimedTournamentIsActive() throws ParticipationNotFoundException, GroupNotFoundException {
        int userId = 12;
        int tournamentId = 332;

        TournamentEntity tournament = generateTournament(tournamentId, true);

        doReturn(of(tournament)).when(tournamentRepository).findById(tournamentId);

        assertThrows(OngoingTournamentClaimedException.class, () -> tournamentService.claimReward(tournamentId, userId));

        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(tournamentParticipationService, never()).isRewardClaimed(anyInt(), anyInt());
        verify(tournamentParticipationService, never()).getRankByTournamentIdAndUserId(anyInt(), anyInt());
        verify(tournamentParticipationService, never()).claimReward(anyInt(), anyInt());
        verify(userService, never()).updateAfterRewardClaim(anyInt(), anyInt());
    }

    @Test
    void claimReward_shouldThrowRewardAlreadyClaimedException_WhenTournamentIsAlreadyClaimed() throws ParticipationNotFoundException, GroupNotFoundException {
        int userId = 12;
        int tournamentId = 332;

        TournamentEntity tournament = generateTournament(tournamentId, false);

        doReturn(of(tournament)).when(tournamentRepository).findById(tournamentId);
        doReturn(true).when(tournamentParticipationService).isRewardClaimed(tournamentId, userId);

        assertThrows(RewardAlreadyClaimedException.class, () -> tournamentService.claimReward(tournamentId, userId));

        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(tournamentParticipationService, times(1)).isRewardClaimed(tournamentId, userId);
        verify(tournamentParticipationService, never()).getRankByTournamentIdAndUserId(anyInt(), anyInt());
        verify(tournamentParticipationService, never()).claimReward(anyInt(), anyInt());
        verify(userService, never()).updateAfterRewardClaim(anyInt(), anyInt());
    }

    @Test
    void claimReward_shouldThrowNoRewardEarnedException_WhenTournamentIsInactiveAndNotClaimed() throws ParticipationNotFoundException, RewardAlreadyClaimedException, OngoingTournamentClaimedException, NoRewardEarnedException, GroupNotFoundException {
        int userId = 12;
        int tournamentId = 332;

        TournamentEntity tournament = generateTournament(tournamentId, false);

        doReturn(of(tournament)).when(tournamentRepository).findById(tournamentId);
        doReturn(false).when(tournamentParticipationService).isRewardClaimed(tournamentId, userId);
        doReturn(TOURNAMENT_LAST_REWARD_RANK * 2).when(tournamentParticipationService).getRankByTournamentIdAndUserId(tournamentId, userId);

        assertThrows(NoRewardEarnedException.class, () -> tournamentService.claimReward(tournamentId, userId));

        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(tournamentParticipationService, times(1)).isRewardClaimed(tournamentId, userId);
        verify(tournamentParticipationService, times(1)).getRankByTournamentIdAndUserId(tournamentId, userId);
        verify(tournamentParticipationService, never()).claimReward(anyInt(), anyInt());
        verify(userService, never()).updateAfterRewardClaim(anyInt(), anyInt());
    }

    @ParameterizedTest
    @MethodSource("ranksToRewards")
    void claimReward_shouldClaimReward_WhenTournamentIsInactiveAndNotClaimed(int rank, int reward) throws ParticipationNotFoundException, RewardAlreadyClaimedException, OngoingTournamentClaimedException, NoRewardEarnedException, GroupNotFoundException {
        int userId = 12;
        int tournamentId = 332;

        TournamentEntity tournament = generateTournament(tournamentId, false);

        doReturn(of(tournament)).when(tournamentRepository).findById(tournamentId);
        doReturn(false).when(tournamentParticipationService).isRewardClaimed(tournamentId, userId);
        doReturn(rank).when(tournamentParticipationService).getRankByTournamentIdAndUserId(tournamentId, userId);
        doNothing().when(tournamentParticipationService).claimReward(tournamentId, userId);

        tournamentService.claimReward(tournamentId, userId);

        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(tournamentParticipationService, times(1)).isRewardClaimed(tournamentId, userId);
        verify(tournamentParticipationService, times(1)).getRankByTournamentIdAndUserId(tournamentId, userId);
        verify(tournamentParticipationService, times(1)).claimReward(tournamentId, userId);
        verify(userService, times(1)).updateAfterRewardClaim(userId, reward);
    }

    private static Stream<Arguments> ranksToRewards() {
        return Stream.of(
                arguments(1, TOURNAMENT_FIRST_RANK_REWARD),
                arguments(2, TOURNAMENT_SECOND_RANK_REWARD),
                arguments(3, TOURNAMENT_THIRD_RANK_REWARD),
                arguments(4, TOURNAMENT_NON_PODIUM_REWARD),
                arguments(5, TOURNAMENT_NON_PODIUM_REWARD),
                arguments(6, TOURNAMENT_NON_PODIUM_REWARD),
                arguments(7, TOURNAMENT_NON_PODIUM_REWARD),
                arguments(8, TOURNAMENT_NON_PODIUM_REWARD),
                arguments(9, TOURNAMENT_NON_PODIUM_REWARD),
                arguments(10, TOURNAMENT_NON_PODIUM_REWARD)
        );
    }
}
