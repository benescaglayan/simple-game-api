package com.example.rowmatch.tournament.response;

import com.example.rowmatch.tournament.participation.TournamentParticipationDto;

import java.util.List;

public class GetLeaderboardResponse {
    public List<TournamentParticipationDto> participations;

    public boolean isOngoingTournament;

    public GetLeaderboardResponse(List<TournamentParticipationDto> participations, boolean isOngoingTournament) {
        this.participations = participations;
        this.isOngoingTournament = isOngoingTournament;
    }
}