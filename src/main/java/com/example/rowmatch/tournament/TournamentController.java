package com.example.rowmatch.tournament;

import com.example.rowmatch.exception.*;
import com.example.rowmatch.tournament.response.GetLeaderboardResponse;
import com.example.rowmatch.tournament.response.GetRankResponse;
import com.example.rowmatch.user.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tournaments")
public class TournamentController {

    private final TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @PutMapping(value="/participants/{userId}")
    public ResponseEntity<GetLeaderboardResponse> participate(@PathVariable int userId) throws UserNotFoundException, NoActiveTournamentException, UserAlreadyJoinedTournamentException, RankTooLowForTournamentException, NotEnoughCoinsForTournamentException, ParticipationNotFoundException, LastEarnedRewardNotClaimedException, GroupNotFoundException {
        GetLeaderboardResponse leaderboard = tournamentService.participate(userId);

        return ResponseEntity.ok().body(leaderboard);
    }

    @GetMapping(value="/{id}/participants/{userId}/rank")
    public ResponseEntity<GetRankResponse> getUserRank(@PathVariable int id, @PathVariable int userId) throws ParticipationNotFoundException, GroupNotFoundException {
        int rank = tournamentService.getUserRank(id, userId);

        return ResponseEntity.ok().body(new GetRankResponse(rank));
    }

    @GetMapping(value="/groups/{groupId}/leaderboard")
    public ResponseEntity<GetLeaderboardResponse> getGroupLeaderboard(@PathVariable int groupId) throws GroupNotFoundException {
        GetLeaderboardResponse leaderboard = tournamentService.getGroupLeaderboard(groupId);

        return ResponseEntity.ok().body(leaderboard);
    }

    @PatchMapping(value= "/{id}/participants/{userId}/claim_reward")
    public ResponseEntity<UserDto> claimReward(@PathVariable int id, @PathVariable int userId) throws ParticipationNotFoundException, OngoingTournamentClaimedException, RewardAlreadyClaimedException, NoRewardEarnedException, GroupNotFoundException {
        UserDto user = tournamentService.claimReward(id, userId);

        return ResponseEntity.ok().body(user);
    }
}
