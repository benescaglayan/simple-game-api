package com.example.rowmatch.tournament.scheduled;

import com.example.rowmatch.tournament.TournamentService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TournamentJobs {

    private final TournamentService tournamentService;

    public TournamentJobs(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void createActiveTournamentAndDeactivatePrevious() {
        tournamentService.deactivatePreviousTournament();

        tournamentService.create();
    }
}
