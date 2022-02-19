package com.example.rowmatch.tournament;

import com.example.rowmatch.exception.*;
import com.example.rowmatch.tournament.group.TournamentGroupService;
import com.example.rowmatch.tournament.participation.TournamentParticipationDto;
import com.example.rowmatch.tournament.participation.TournamentParticipationService;
import com.example.rowmatch.tournament.response.GetLeaderboardResponse;
import com.example.rowmatch.user.UserDto;
import com.example.rowmatch.user.UserService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TournamentService {

    private final UserService userService;

    private final TournamentRepository tournamentRepository;

    private final TournamentGroupService tournamentGroupService;

    private final TournamentParticipationService tournamentParticipationService;

    private static final int TOURNAMENT_MINIMUM_ENTRY_LEVEL = 20;
    private static final int TOURNAMENT_MINIMUM_ENTRY_COINS = 1000;
    private static final int TOURNAMENT_FIRST_RANK_REWARD = 10000;
    private static final int TOURNAMENT_SECOND_RANK_REWARD = 5000;
    private static final int TOURNAMENT_THIRD_RANK_REWARD = 3000;
    private static final int TOURNAMENT_NON_PODIUM_RANK_REWARD = 1000;
    private static final int TOURNAMENT_LAST_REWARDED_RANK = 10;

    public TournamentService(UserService userService, TournamentRepository tournamentRepository, TournamentGroupService tournamentGroupService, TournamentParticipationService tournamentParticipationService) {
        this.userService = userService;
        this.tournamentRepository = tournamentRepository;
        this.tournamentGroupService = tournamentGroupService;
        this.tournamentParticipationService = tournamentParticipationService;
    }

    public void create() {
        save(new TournamentEntity());
    }

    public GetLeaderboardResponse participate(int userId) throws NoActiveTournamentException, UserNotFoundException, UserAlreadyJoinedTournamentException, RankTooLowForTournamentException, NotEnoughCoinsForTournamentException, ParticipationNotFoundException, LastEarnedRewardNotClaimedException, GroupNotFoundException {
        int tournamentId = getActiveTournamentId();

        checkIfUserAlreadyJoined(tournamentId, userId);

        UserDto user = userService.get(userId);
        isEligibleToEnterTournament(user);

        int groupId = tournamentGroupService.getGroupIdForUserLevel(user.getLevel(), tournamentId);

        tournamentParticipationService.participate(tournamentId, groupId, userId);

        return getGroupLeaderboard(groupId);
    }

    public GetLeaderboardResponse getGroupLeaderboard(int groupId) {
        List<TournamentParticipationDto> participations = tournamentParticipationService.findAllByGroupIdOrderByUserScoreDesc(groupId);

        return new GetLeaderboardResponse(participations, true);
    }

    public int getUserRank(int id, int userId) throws ParticipationNotFoundException, GroupNotFoundException {
        return tournamentParticipationService.getRankByTournamentIdAndUserId(id, userId);
    }

    public UserDto claimReward(int id, int userId) throws OngoingTournamentClaimedException, ParticipationNotFoundException, RewardAlreadyClaimedException, NoRewardEarnedException, GroupNotFoundException {
        if (isActive(id)) {
            throw new OngoingTournamentClaimedException();
        }

        checkIfCanClaimReward(id, userId);

        int reward = calculateReward(id, userId);

        tournamentParticipationService.claimReward(id, userId);

        return userService.updateAfterRewardClaim(userId, reward);
    }

    public void incrementTournamentScore(int id, int userId) {
        if (!isActive(id)) {
            return;
        }

        try {
            tournamentParticipationService.incrementTournamentScore(id, userId);
        } catch (ParticipationNotFoundException ignored) {
        }
    }

    public boolean isActive(int id) {
        TournamentEntity tournament = tournamentRepository.findById(id).orElse(null);

        return tournament != null && tournament.isActive();
    }

    public void deactivatePreviousTournament() {
        TournamentEntity tournament = tournamentRepository.getTournamentByIsActiveTrue().orElse(null);
        if (tournament == null) {
            return;
        }

        tournament.setActive(false);
        save(tournament);
    }

    private void save(TournamentEntity tournament) {
        tournamentRepository.save(tournament);
    }

    private void isEligibleToEnterTournament(UserDto user) throws NotEnoughCoinsForTournamentException, RankTooLowForTournamentException, ParticipationNotFoundException, LastEarnedRewardNotClaimedException, GroupNotFoundException {
        checkIfLevelIsEnoughToEnter(user.getLevel());

        checkIfEnoughCoinsExistToEnter(user.getCoins());

        checkIfUnclaimedRewardExists(user.getLastEnteredTournamentId(), user.getId());
    }

    private void checkIfLevelIsEnoughToEnter(int level) throws RankTooLowForTournamentException {
        if (level < TOURNAMENT_MINIMUM_ENTRY_LEVEL) {
            throw new RankTooLowForTournamentException();
        }
    }

    private void checkIfEnoughCoinsExistToEnter(int coins) throws NotEnoughCoinsForTournamentException {
        if (coins < TOURNAMENT_MINIMUM_ENTRY_COINS) {
            throw new NotEnoughCoinsForTournamentException();
        }
    }

    private void checkIfUnclaimedRewardExists(Integer id, int userId) throws ParticipationNotFoundException, LastEarnedRewardNotClaimedException, GroupNotFoundException {
        if (id == null) {
            return;
        }

        int lastEnteredTournamentRank = getUserRank(id, userId);
        if (lastEnteredTournamentRank >= TOURNAMENT_LAST_REWARDED_RANK) {
            return;
        }

        boolean isRewardClaimed = tournamentParticipationService.isRewardClaimed(id, userId);
        if (!isRewardClaimed) {
            throw new LastEarnedRewardNotClaimedException();
        }
    }

    private void checkIfUserAlreadyJoined(int id, int userId) throws UserAlreadyJoinedTournamentException {
        boolean hasUserAlreadyJoined = tournamentParticipationService.existsByTournamentIdAndUserId(id, userId);
        if (hasUserAlreadyJoined) {
            throw new UserAlreadyJoinedTournamentException();
        }
    }

    private void checkIfCanClaimReward(int id, int userId) throws RewardAlreadyClaimedException, ParticipationNotFoundException {
        boolean isRewardClaimed = tournamentParticipationService.isRewardClaimed(id, userId);
        if (isRewardClaimed) {
            throw new RewardAlreadyClaimedException();
        }
    }

    private int getActiveTournamentId() throws NoActiveTournamentException {
        TournamentEntity tournament = tournamentRepository.getTournamentByIsActiveTrue().orElse(null);
        if (tournament == null) {
            throw new NoActiveTournamentException();
        }

        return tournament.getId();
    }

    private int calculateReward(int id, int userId) throws ParticipationNotFoundException, NoRewardEarnedException, GroupNotFoundException {
        int rank = getUserRank(id, userId);
        if (rank > TOURNAMENT_LAST_REWARDED_RANK) {
            throw new NoRewardEarnedException();
        }

        int reward;

        if (rank == 1) {
            reward = TOURNAMENT_FIRST_RANK_REWARD;
        } else if (rank == 2) {
            reward = TOURNAMENT_SECOND_RANK_REWARD;
        } else if (rank == 3) {
            reward = TOURNAMENT_THIRD_RANK_REWARD;
        } else {
            reward = TOURNAMENT_NON_PODIUM_RANK_REWARD;
        }

        return reward;
    }
}
