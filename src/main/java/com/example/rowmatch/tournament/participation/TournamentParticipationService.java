package com.example.rowmatch.tournament.participation;

import com.example.rowmatch.exception.GroupNotFoundException;
import com.example.rowmatch.exception.ParticipationNotFoundException;
import com.example.rowmatch.tournament.group.TournamentGroupService;
import com.example.rowmatch.user.UserService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<TournamentParticipationDto> findAllByGroupIdOrderByUserScoreDesc(int groupId) {
        List<TournamentParticipationEntity> participations = tournamentParticipationRepository.findAllByGroupIdOrderByUserScoreDesc(groupId);

        return participations.stream().map(TournamentParticipationDto::new).collect(Collectors.toCollection(ArrayList::new));
    }

    public int getRankByTournamentIdAndUserId(int tournamentId, int userId) throws ParticipationNotFoundException, GroupNotFoundException {
        TournamentParticipationEntity participation = getByTournamentIdAndUserId(tournamentId, userId);

        List<TournamentParticipationDto> participations = findAllByGroupIdOrderByUserScoreDesc(participation.getGroupId());

        return indexOfParticipation(participation.getId(), participations) + 1;
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

    private int indexOfParticipation(int id, List<TournamentParticipationDto> participations)  {
        int rank = 0;
        for (TournamentParticipationDto participation : participations) {
            if (participation.getId() == id) {
                break;
            }

            rank++;
        }

        return rank;
    }
}
