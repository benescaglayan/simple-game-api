package com.example.rowmatch.user;

public class UserLevelledUpEvent {
    public int userId;

    public int tournamentId;

    public UserLevelledUpEvent(int userId, int tournamentId) {
        this.userId = userId;
        this.tournamentId = tournamentId;
    }
}
