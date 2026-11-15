package com.voting.service;

import com.voting.entity.Campaign;
import com.voting.repository.CampaignRepository;
import com.voting.repository.CandidateRepository;
import com.voting.repository.VoteRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampaignService {
    private final CampaignRepository campaigns;
    private final CandidateRepository candidates;
    private final VoteRepository votes;

    public CampaignService(CampaignRepository campaigns, CandidateRepository candidates, VoteRepository votes) {
        this.campaigns = campaigns;
        this.candidates = candidates;
        this.votes = votes;
    }

    @Transactional(readOnly = true)
    public List<Campaign> findAll() { return campaigns.findAllByOrderByStartDateDesc(); }

    @Transactional(readOnly = true)
    public Campaign findById(Long id) {
        return campaigns.findById(id).orElseThrow(() -> new IllegalArgumentException("Campaign not found."));
    }

    @Transactional
    public Campaign create(String name, String tagline, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("The end date must be the same as or later than the start date.");
        }
        return campaigns.save(new Campaign(name.trim(), tagline.trim(), startDate, endDate));
    }

    @Transactional
    public void delete(Long campaignId) {
        Campaign campaign = findById(campaignId);
        votes.deleteAllByCampaignId(campaignId);
        candidates.deleteAllByCampaignId(campaignId);
        campaigns.delete(campaign);
    }
}
