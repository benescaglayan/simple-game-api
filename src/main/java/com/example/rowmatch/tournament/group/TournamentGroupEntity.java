package com.example.rowmatch.tournament.group;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tournament_groups")
public class TournamentGroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private int tournamentId;

    @Column(nullable = false)
    private int groupLevel;

    @CreationTimestamp
    @Column(nullable = false)
    private Instant createdAt;

    public TournamentGroupEntity(int tournamentId, int groupLevel) {
        this.tournamentId = tournamentId;
        this.groupLevel = groupLevel;
    }
}
