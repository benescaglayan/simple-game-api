package com.example.rowmatch.tournament;

import com.example.rowmatch.exception.*;
import com.example.rowmatch.tournament.participation.TournamentParticipationEntity;
import com.example.rowmatch.tournament.response.GetRankResponse;
import com.example.rowmatch.user.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tournaments")
public class TournamentController {

    private final TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @PutMapping(value="/participants/{userId}")
    public ResponseEntity<List<TournamentParticipationEntity>> participate(@PathVariable int userId) throws UserNotFoundException, NoActiveTournamentException, UserAlreadyJoinedTournamentException, RankTooLowForTournamentException, NotEnoughCoinsForTournamentException, ParticipationNotFoundException, LastEarnedRewardNotClaimedException, GroupNotFoundException {
        List<TournamentParticipationEntity> leaderboard = tournamentService.participate(userId);

        return ResponseEntity.ok().body(leaderboard);
    }

    @GetMapping(value="/{id}/participants/{userId}/rank")
    public ResponseEntity<GetRankResponse> getUserRank(@PathVariable int id, @PathVariable int userId) throws ParticipationNotFoundException, GroupNotFoundException {
        int rank = tournamentService.getUserRank(id, userId);

        return ResponseEntity.ok().body(new GetRankResponse(rank));
    }

    @GetMapping(value="/groups/{groupId}/leaderboard")
    public ResponseEntity<List<TournamentParticipationEntity>> getGroupLeaderboard(@PathVariable int groupId) throws GroupNotFoundException {
        List<TournamentParticipationEntity> leaderboard = tournamentService.getGroupLeaderboard(groupId);

        return ResponseEntity.ok().body(leaderboard);
    }

    @PatchMapping(value= "/{id}/participants/{userId}/claim_reward")
    public ResponseEntity<UserDto> claimReward(@PathVariable int id, @PathVariable int userId) throws ParticipationNotFoundException, OngoingTournamentClaimedException, RewardAlreadyClaimedException, NoRewardEarnedException, GroupNotFoundException {
        UserDto user = tournamentService.claimReward(id, userId);

        return ResponseEntity.ok().body(user);
    }
}
