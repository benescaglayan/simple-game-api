package com.example.rowmatch.util;

import com.example.rowmatch.tournament.TournamentEntity;
import com.example.rowmatch.tournament.group.TournamentGroupEntity;
import com.example.rowmatch.tournament.participation.TournamentParticipationEntity;
import com.example.rowmatch.user.UserDto;
import com.example.rowmatch.user.UserEntity;
import org.joda.time.DateTime;

import java.util.Date;

public class Generators {

    public static UserEntity generateUser(int id, int coins, int level, Integer lastEnteredTournamentId) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setCoins(coins);
        user.setCurrentLevel(level);
        user.setLastEnteredTournamentId(lastEnteredTournamentId);

        return user;
    }

    public static UserDto generateUserDto(int id, int coins, int level, Integer lastEnteredTournamentId) {
        return new UserDto(generateUser(id, coins, level, lastEnteredTournamentId));
    }

    public static TournamentEntity generateTournament(int id, boolean isActive) {
        TournamentEntity tournament = new TournamentEntity();
        tournament.setId(id);
        tournament.setActive(isActive);

        return tournament;
    }

    public static TournamentParticipationEntity generateParticipation(int id, int groupId, int tournamentId, int userId, boolean isRewardClaimed, int userScore) {
        TournamentParticipationEntity participation = new TournamentParticipationEntity();
        participation.setId(id);
        participation.setGroupId(groupId);
        participation.setTournamentId(tournamentId);
        participation.setUserId(userId);
        participation.setRewardClaimed(isRewardClaimed);
        participation.setUserScore(userScore);

        return participation;
    }

    public static TournamentGroupEntity generateGroup(int tournamentId, int groupLevel, DateTime createdAt) {
        TournamentGroupEntity group = new TournamentGroupEntity();
        group.setTournamentId(tournamentId);
        group.setGroupLevel(groupLevel);
        group.setCreatedAt(createdAt.toDate().toInstant());

        return group;
    }
}
