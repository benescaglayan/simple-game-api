package com.example.rowmatch.tournament.response;

import com.example.rowmatch.tournament.participation.TournamentParticipationDto;

import java.util.List;

public class GetLeaderboardResponse {
    public List<TournamentParticipationDto> participations;

    // TODO: use a flag to indicate if tournament is ongoing or not, just so we can cache the response if tournament is over

    public GetLeaderboardResponse(List<TournamentParticipationDto> participations) {
        this.participations = participations;
    }
}