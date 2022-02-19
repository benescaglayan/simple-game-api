package com.example.rowmatch.services;

import com.example.rowmatch.exception.*;
import com.example.rowmatch.tournament.TournamentEntity;
import com.example.rowmatch.tournament.group.TournamentGroupEntity;
import com.example.rowmatch.tournament.group.TournamentGroupRepository;
import com.example.rowmatch.tournament.group.TournamentGroupService;
import com.example.rowmatch.tournament.participation.TournamentParticipationService;
import com.example.rowmatch.user.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TournamentGroupServiceTests {

    @Mock
    TournamentGroupRepository tournamentGroupRepository;

    @Mock
    TournamentParticipationService tournamentParticipationService;

    @InjectMocks
    TournamentGroupService tournamentGroupService;

    private static final int TOURNAMENT_GROUP_MAX_PARTICIPATOR_COUNT = 20;

    @ParameterizedTest
    @MethodSource("userLevelsToGroupLevel")
    void getGroupIdForUserLevel_shouldCreateAndReturnNewGroupId_WhenThereIsNoGroupInLevel(int userLevel, int groupLevel) {
        int tournamentId = 323;

        doReturn(empty()).when(tournamentGroupRepository).findFirstByTournamentIdAndGroupLevelOrderByCreatedAtDesc(tournamentId, groupLevel);
        doAnswer(returnsFirstArg()).when(tournamentGroupRepository).save(argThat((TournamentGroupEntity group) -> group.getTournamentId() == tournamentId && group.getGroupLevel() == groupLevel));

        tournamentGroupService.getGroupIdForUserLevel(userLevel, tournamentId);

        verify(tournamentGroupRepository, times(1)).findFirstByTournamentIdAndGroupLevelOrderByCreatedAtDesc(tournamentId, groupLevel);
        verify(tournamentGroupRepository, times(1)).save(argThat((TournamentGroupEntity group) -> group.getTournamentId() == tournamentId && group.getGroupLevel() == groupLevel));
        verify(tournamentParticipationService, never()).getParticipationCountByTournamentIdAndGroupId(anyInt(), anyInt());
    }

    @ParameterizedTest
    @MethodSource("userLevelsToGroupLevel")
    void getGroupIdForUserLevel_shouldCreateAndReturnNewGroupId_WhenAllGroupsInLevelAreFull(int userLevel, int groupLevel) {
        int groupId = 2415;
        int tournamentId = 323;

        TournamentGroupEntity fullGroup = new TournamentGroupEntity();
        fullGroup.setId(groupId);

        doReturn(of(fullGroup)).when(tournamentGroupRepository).findFirstByTournamentIdAndGroupLevelOrderByCreatedAtDesc(tournamentId, groupLevel);
        doAnswer(returnsFirstArg()).when(tournamentGroupRepository).save(argThat((TournamentGroupEntity group) -> group.getTournamentId() == tournamentId && group.getGroupLevel() == groupLevel));
        doReturn(TOURNAMENT_GROUP_MAX_PARTICIPATOR_COUNT).when(tournamentParticipationService).getParticipationCountByTournamentIdAndGroupId(tournamentId, groupId);

        tournamentGroupService.getGroupIdForUserLevel(userLevel, tournamentId);

        verify(tournamentGroupRepository, times(1)).findFirstByTournamentIdAndGroupLevelOrderByCreatedAtDesc(tournamentId, groupLevel);
        verify(tournamentGroupRepository, times(1)).save(argThat((TournamentGroupEntity group) -> group.getTournamentId() == tournamentId && group.getGroupLevel() == groupLevel));
        verify(tournamentParticipationService, times(1)).getParticipationCountByTournamentIdAndGroupId(tournamentId, groupId);
    }

    @ParameterizedTest
    @MethodSource("userLevelsToGroupLevel")
    void getGroupIdForUserLevel_shouldReturnExistingGroupId_WhenThereIsNonFullGroupInLevel(int userLevel, int groupLevel) {
        int groupId = 2415;
        int tournamentId = 323;

        TournamentGroupEntity nonFullGroup = new TournamentGroupEntity();
        nonFullGroup.setId(groupId);

        doReturn(of(nonFullGroup)).when(tournamentGroupRepository).findFirstByTournamentIdAndGroupLevelOrderByCreatedAtDesc(tournamentId, groupLevel);
        doReturn(TOURNAMENT_GROUP_MAX_PARTICIPATOR_COUNT / 2).when(tournamentParticipationService).getParticipationCountByTournamentIdAndGroupId(tournamentId, groupId);

        tournamentGroupService.getGroupIdForUserLevel(userLevel, tournamentId);

        verify(tournamentGroupRepository, times(1)).findFirstByTournamentIdAndGroupLevelOrderByCreatedAtDesc(tournamentId, groupLevel);
        verify(tournamentGroupRepository, never()).save(any(TournamentGroupEntity.class));
        verify(tournamentParticipationService, times(1)).getParticipationCountByTournamentIdAndGroupId(tournamentId, groupId);
    }

    private static Stream<Arguments> userLevelsToGroupLevel() {
        return Stream.of(
                arguments(50, 0),
                arguments(100, 0),
                arguments(155, 1),
                arguments(200, 1),
                arguments(250, 2)
        );
    }
}
