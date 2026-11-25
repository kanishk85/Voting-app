package com.voting.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.voting.entity.Campaign;
import com.voting.entity.Candidate;
import com.voting.repository.VoteRepository;

@Service
public class ResultsService {
    private final CampaignService campaigns;
    private final VoteRepository votes;

    public ResultsService(CampaignService campaigns, VoteRepository votes) {
        this.campaigns = campaigns;
        this.votes = votes;
    }

    @Transactional(readOnly = true)
    public List<Campaign> resultsHome() {
        List<Campaign> results = new ArrayList<>();
        for (Campaign campaign : campaigns.findAll()) {
            if (campaign.getEndDate().isBefore(LocalDate.now())) {
                
                results.add(campaign);
            }
        }
        return results;
    }

    @Transactional(readOnly = true)
    public CampaignResult resultFor(Long campaignId, Long voterId) {
        Campaign campaign = campaigns.findById(campaignId);
        if (!campaign.getEndDate().isBefore(LocalDate.now())) return null;
        var campaignVotes = votes.findAllByCampaign_Id(campaignId);
        Map<Candidate, Long> counts = campaignVotes.stream().collect(Collectors.groupingBy(
                vote -> vote.getCandidate(), Collectors.counting()));
        Candidate winner = counts.entrySet().stream()
                .max(Comparator.<Map.Entry<Candidate, Long>>comparingLong(Map.Entry::getValue)
                        .thenComparing(entry -> entry.getKey().getName(), Comparator.reverseOrder()))
                .map(Map.Entry::getKey).orElse(null);
        Candidate voterChoice = campaignVotes.stream().filter(vote -> vote.getVoter().getId().equals(voterId))
                .map(vote -> vote.getCandidate()).findFirst().orElse(null);
        return new CampaignResult(campaign.getId(), campaign.getName(), campaign.getTagline(),
                winner == null ? null : winner.getName(), voterChoice == null ? null : voterChoice.getName());
    }

    public record CampaignResult(Long campaignId, String campaignName, String tagline,
                                 String winnerName, String voterChoiceName) {}
}
