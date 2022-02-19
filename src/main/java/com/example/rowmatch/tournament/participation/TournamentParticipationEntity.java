package com.example.rowmatch.tournament.participation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tournament_participations")
public class TournamentParticipationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private int tournamentId;

    @Column(nullable = false)
    private int groupId;

    @Column(nullable = false)
    private int userId;

    @Column(nullable = false)
    private int userScore;

    @Column
    private boolean isRewardClaimed;

    public TournamentParticipationEntity(int tournamentId, int groupId, int userId) {
        this.tournamentId = tournamentId;
        this.groupId = groupId;
        this.userId = userId;
        this.userScore = 0;
        this.isRewardClaimed = false;
    }
}
