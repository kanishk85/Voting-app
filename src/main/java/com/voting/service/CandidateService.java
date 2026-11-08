package com.voting.service;

import com.voting.entity.Candidate;
import com.voting.entity.Campaign;
import com.voting.repository.CandidateRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CandidateService {
    private final CandidateRepository candidates;

    public CandidateService(CandidateRepository candidates) { this.candidates = candidates; }

    @Transactional(readOnly = true)
    public List<Candidate> findAll() { return candidates.findAllByOrderByIdAsc(); }

    @Transactional(readOnly = true)
    public List<Candidate> findByCampaign(Long campaignId) { return candidates.findAllByCampaignIdOrderByIdAsc(campaignId); }

    @Transactional
    public void addToCampaign(Campaign campaign, String name) {
        String candidateName = name == null ? "" : name.trim();
        if (candidateName.isBlank() || candidateName.length() > 100) {
            throw new IllegalArgumentException("Candidate name must be between 1 and 100 characters.");
        }
        if (candidates.existsByCampaignIdAndNameIgnoreCase(campaign.getId(), candidateName)) {
            throw new IllegalArgumentException("That candidate is already in this campaign.");
        }
        candidates.save(new Candidate(java.util.UUID.randomUUID().toString(), candidateName, "Independent", campaign));
    }
}
