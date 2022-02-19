package com.example.rowmatch.user;

import com.example.rowmatch.exception.UserNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final ApplicationEventPublisher eventPublisher;

    private static final int LEVELUP_COIN_REWARD = 25;

    public UserService(UserRepository userRepository, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    public UserDto create() {
        UserEntity user = save(new UserEntity());

        return new UserDto(user);
    }

    public UserDto levelUp(int id) throws UserNotFoundException {
        UserEntity user = getOrThrow(id);

        updateProgress(user, user.getCurrentLevel() + 1);
        incrementTournamentScore(user.getLastEnteredTournamentId(), user.getId());

        return new UserDto(user);
    }

    private void updateProgress(UserEntity user, int newLevel) {
        user.setCoins(user.getCoins() + UserService.LEVELUP_COIN_REWARD);
        user.setCurrentLevel(newLevel);

        save(user);
    }

    public void updateAfterParticipation(int id, int tournamentId, int participationFee) {
        UserEntity user = getEntityOrNull(id);

        user.setCoins(user.getCoins() - participationFee);
        user.setLastEnteredTournamentId(tournamentId);

        save(user);
    }

    public UserDto updateAfterRewardClaim(int id, int reward) {
        UserEntity user = getEntityOrNull(id);

        user.setCoins(user.getCoins() + reward);

        return new UserDto(save(user));
    }

    public UserDto get(int id) throws UserNotFoundException {
        UserEntity user = getOrThrow(id);

        return new UserDto(user);
    }

    private void incrementTournamentScore(Integer lastActiveTournamentId, int id) {
        if (lastActiveTournamentId != null) {
            eventPublisher.publishEvent(new UserLevelledUpEvent(id, lastActiveTournamentId));
        }
    }

    private UserEntity save(UserEntity user) {
        return userRepository.save(user);
    }

    private UserEntity getEntityOrNull(int id) {
        return userRepository.findById(id).orElse(null);
    }

    private UserEntity getOrThrow(int id) throws UserNotFoundException {
        UserEntity user = getEntityOrNull(id);
        if (user == null) {
            throw new UserNotFoundException();
        }

        return user;
    }
}
