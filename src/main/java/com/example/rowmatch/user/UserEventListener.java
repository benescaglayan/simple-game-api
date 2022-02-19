package com.example.rowmatch.user;

import com.example.rowmatch.tournament.TournamentService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener {

    private final TournamentService tournamentService;

    public UserEventListener(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @EventListener
    public void handleUserLevelledUpEvent(UserLevelledUpEvent event) {
        tournamentService.incrementTournamentScore(event.tournamentId, event.userId);
    }
}
