package com.example.rowmatch.tournament.participation;

import com.example.rowmatch.exception.GroupNotFoundException;
import com.example.rowmatch.exception.ParticipationNotFoundException;
import com.example.rowmatch.tournament.group.TournamentGroupService;
import com.example.rowmatch.user.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TournamentParticipationService {

    private final TournamentParticipationRepository tournamentParticipationRepository;

    private final UserService userService;

    private static final int TOURNAMENT_PARTICIPATION_FEE = 1000;

    public TournamentParticipationService(TournamentParticipationRepository tournamentParticipationRepository, UserService userService) {
        this.tournamentParticipationRepository = tournamentParticipationRepository;
        this.userService = userService;
    }

    public void participate(int tournamentId, int groupId, int userId) {
        save(new TournamentParticipationEntity(tournamentId, groupId, userId));

        userService.updateAfterParticipation(userId, tournamentId, TOURNAMENT_PARTICIPATION_FEE);
    }

    public void incrementTournamentScore(int tournamentId, int userId) throws ParticipationNotFoundException {
        TournamentParticipationEntity participation = getByTournamentIdAndUserId(tournamentId, userId);

        participation.setUserScore(participation.getUserScore() + 1);
        save(participation);
    }

    public int getParticipationCountByTournamentIdAndGroupId(int tournamentId, int groupId) {
        return tournamentParticipationRepository.countByTournamentIdAndGroupId(tournamentId, groupId);
    }

    public boolean existsByTournamentIdAndUserId(int tournamentId, int userId) {
        return tournamentParticipationRepository.existsByTournamentIdAndUserId(tournamentId, userId);
    }

    public List<TournamentParticipationEntity> findAllByGroupIdOrderByUserScoreDesc(int groupId) {
        return tournamentParticipationRepository.findAllByGroupIdOrderByUserScoreDesc(groupId);
    }

    public int getRankByTournamentIdAndUserId(int tournamentId, int userId) throws ParticipationNotFoundException, GroupNotFoundException {
        TournamentParticipationEntity participation = getByTournamentIdAndUserId(tournamentId, userId);

        List<TournamentParticipationEntity> participations = findAllByGroupIdOrderByUserScoreDesc(participation.getGroupId());

        return participations.indexOf(participation) + 1;
    }

    public void claimReward(int tournamentId, int userId) throws ParticipationNotFoundException {
        TournamentParticipationEntity participation = getByTournamentIdAndUserId(tournamentId, userId);

        participation.setRewardClaimed(true);
        save(participation);
    }

    public boolean isRewardClaimed(int tournamentId, int userId) throws ParticipationNotFoundException {
        TournamentParticipationEntity participation = getByTournamentIdAndUserId(tournamentId, userId);

        return participation.isRewardClaimed();
    }

    private TournamentParticipationEntity getByTournamentIdAndUserId(int tournamentId, int userId) throws ParticipationNotFoundException {
        TournamentParticipationEntity participation = tournamentParticipationRepository.findByTournamentIdAndUserId(tournamentId, userId).orElse(null);
        if (participation == null) {
            throw new ParticipationNotFoundException();
        }

        return participation;
    }

    private void save(TournamentParticipationEntity participation) {
        tournamentParticipationRepository.save(participation);
    }
}
