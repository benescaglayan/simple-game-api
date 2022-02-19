package com.example.rowmatch.user;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private int coins;

    @Column(nullable = false)
    private int currentLevel;

    @Column
    private Integer lastEnteredTournamentId;

    public UserEntity() {
        setCoins(5000);
        setCurrentLevel(1);
        setLastEnteredTournamentId(null);
    }
}
