package com.example.rowmatch.tournament.participation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TournamentParticipationDto {

    private int id;

    private int tournamentId;

    private int groupId;

    private int userId;

    private int userScore;

    private boolean isRewardClaimed;

    public TournamentParticipationDto(TournamentParticipationEntity entity) {
        this.id = entity.getId();
        this.tournamentId = entity.getTournamentId();
        this.groupId = entity.getGroupId();
        this.userId = entity.getUserId();
        this.userScore = entity.getUserScore();
        this.isRewardClaimed = entity.isRewardClaimed();
    }
}