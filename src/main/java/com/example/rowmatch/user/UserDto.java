package com.example.rowmatch.user;

import lombok.Getter;

@Getter
public class UserDto {

    private final int id;

    private final int level;

    private final int coins;

    private final Integer lastEnteredTournamentId;

    public UserDto(UserEntity userEntity) {
        id = userEntity.getId();
        level = userEntity.getCurrentLevel();
        coins = userEntity.getCoins();
        lastEnteredTournamentId = userEntity.getLastEnteredTournamentId();
    }
}
