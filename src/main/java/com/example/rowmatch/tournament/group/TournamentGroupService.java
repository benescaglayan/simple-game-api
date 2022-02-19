package com.example.rowmatch.tournament.group;

import com.example.rowmatch.tournament.participation.TournamentParticipationService;
import org.springframework.stereotype.Service;

@Service
public class TournamentGroupService {

    private final TournamentGroupRepository tournamentGroupRepository;

    private final TournamentParticipationService tournamentParticipationService;

    private static final int TOURNAMENT_GROUP_MAX_PARTICIPATOR_COUNT = 20;

    public TournamentGroupService(TournamentGroupRepository tournamentGroupRepository, TournamentParticipationService tournamentParticipationService) {
        this.tournamentGroupRepository = tournamentGroupRepository;
        this.tournamentParticipationService = tournamentParticipationService;
    }

    public int getGroupIdForUserLevel(int userLevel, int tournamentId) {
        int groupLevel = getTournamentGroupLevelForUserLevel(userLevel);
        TournamentGroupEntity group = tournamentGroupRepository.findFirstByTournamentIdAndGroupLevelOrderByCreatedAtDesc(tournamentId, groupLevel).orElse(null);

        if (group == null) {
            group = createGroup(tournamentId, groupLevel);
        } else {
            int numOfParticipatorsInGroup = tournamentParticipationService.getParticipationCountByTournamentIdAndGroupId(tournamentId, group.getId());
            if (numOfParticipatorsInGroup == TOURNAMENT_GROUP_MAX_PARTICIPATOR_COUNT) {
                group = createGroup(tournamentId, groupLevel);
            }
        }

        return group.getId();
    }

    private TournamentGroupEntity createGroup(int tournamentId, int groupLevel) {
        return tournamentGroupRepository.save(new TournamentGroupEntity(tournamentId, groupLevel));
    }

    private int getTournamentGroupLevelForUserLevel(int level) {
        return level / 100 + (level % 100 == 0 ? -1 : 0);
    }
}
