package com.example.rowmatch.services;

import com.example.rowmatch.exception.UserNotFoundException;
import com.example.rowmatch.user.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static com.example.rowmatch.util.Generators.generateUser;
import static java.util.Optional.*;
import static org.mockito.Mockito.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @Mock
    UserRepository userRepository;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    UserService userService;

    private static final int STARTING_COINS = 5000;
    private static final int LEVELUP_COIN_REWARD = 25;
    private static final int STARTING_LEVEL = 1;

    @Test
    void create_shouldCreateAndReturnUser() {
        doReturn(new UserEntity()).when(userRepository).save(
                argThat((UserEntity user) -> user.getLastEnteredTournamentId() == null && user.getCoins() == STARTING_COINS && user.getCurrentLevel() == STARTING_LEVEL));

        UserDto actualUser =  userService.create();

        assertNull(actualUser.getLastEnteredTournamentId());
        assertEquals(STARTING_COINS, actualUser.getCoins());
        assertEquals(STARTING_LEVEL, actualUser.getLevel());

        verify(userRepository, times(1)).save(
                argThat((UserEntity user) -> user.getLastEnteredTournamentId() == null && user.getCoins() == STARTING_COINS && user.getCurrentLevel() == STARTING_LEVEL));
    }

    @Test
    void levelUp_shouldThrowUserNotFoundException_WhenUserDoesNotExist() {
        int userId = 1;

        doReturn(empty()).when(userRepository).findById(userId);

        assertThrows(UserNotFoundException.class, () -> userService.levelUp(userId));

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void levelUp_shouldLevelUserUpAndPublishUserLevelledUpEventAndReturnUser_WhenUserIsInActiveTournament() throws UserNotFoundException {
        int userId = 1;
        int coins = 300;
        int level = 121;
        Integer lastEnteredTournamentId = 666;

        UserEntity userToLevelUp = generateUser(userId, coins, level, lastEnteredTournamentId);

        doReturn(of(userToLevelUp)).when(userRepository).findById(userId);
        doAnswer(returnsFirstArg()).when(userRepository).save(any(UserEntity.class));
        doNothing().when(eventPublisher)
                .publishEvent(argThat((UserLevelledUpEvent event) -> event.userId == userToLevelUp.getId() && event.tournamentId == userToLevelUp.getLastEnteredTournamentId()));

        UserDto levelledUpUser =  userService.levelUp(userId);

        assertEquals(lastEnteredTournamentId, levelledUpUser.getLastEnteredTournamentId());
        assertEquals(coins + LEVELUP_COIN_REWARD, levelledUpUser.getCoins());
        assertEquals(level + 1, levelledUpUser.getLevel());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(UserEntity.class));
        verify(eventPublisher, times(1))
                .publishEvent(argThat((UserLevelledUpEvent event) -> event.userId == userToLevelUp.getId() && event.tournamentId == userToLevelUp.getLastEnteredTournamentId()));
    }

    @Test
    void levelUp_shouldLevelUserUpAndNotPublishUserLevelledUpEventAndReturnUser_WhenUserIsNotInActiveTournament() throws UserNotFoundException {
        int userId = 1;
        int coins = 300;
        int level = 121;
        Integer lastEnteredTournamentId = null;

        UserEntity userToLevelUp = generateUser(userId, coins, level, lastEnteredTournamentId);

        doReturn(of(userToLevelUp)).when(userRepository).findById(userId);
        doAnswer(returnsFirstArg()).when(userRepository).save(any(UserEntity.class));

        UserDto levelledUpUser =  userService.levelUp(userId);

        assertEquals(lastEnteredTournamentId, levelledUpUser.getLastEnteredTournamentId());
        assertEquals(coins + LEVELUP_COIN_REWARD, levelledUpUser.getCoins());
        assertEquals(level + 1, levelledUpUser.getLevel());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(UserEntity.class));
        verify(eventPublisher, never())
                .publishEvent(argThat((UserLevelledUpEvent event) -> event.userId == userToLevelUp.getId() && event.tournamentId == userToLevelUp.getLastEnteredTournamentId()));
    }


}
