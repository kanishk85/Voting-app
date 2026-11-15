package com.voting.service;

import com.voting.entity.Vote;
import com.voting.repository.CampaignRepository;
import com.voting.repository.CandidateRepository;
import com.voting.repository.UserRepository;
import com.voting.repository.VoteRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VoteService {
    private final UserRepository users;
    private final CandidateRepository candidates;
    private final VoteRepository votes;
    private final CampaignRepository campaigns;

    public VoteService(UserRepository users, CandidateRepository candidates, VoteRepository votes, CampaignRepository campaigns) {
        this.users = users;
        this.candidates = candidates;
        this.votes = votes;
        this.campaigns = campaigns;
    }

    @Transactional
    public void castVote(String email, Long campaignId, Long candidateId) {
        var voter = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Your account could not be found."));
        var campaign = campaigns.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found."));
        if (!campaign.isOpen()) throw new IllegalStateException("This campaign is not open for voting.");
        if (votes.existsByVoterIdAndCampaignId(voter.getId(), campaignId)) {
            throw new IllegalStateException("You have already voted in this campaign.");
        }
        var candidate = candidates.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Please choose a valid candidate."));
        if (candidate.getCampaign() == null || !campaignId.equals(candidate.getCampaign().getId())) {
            throw new IllegalArgumentException("Choose a candidate in this campaign.");
        }
        votes.save(new Vote(voter, candidate, campaign, Instant.now()));
        users.save(voter);
    }
}
