package com.voting.repository;

import com.voting.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    long countByCandidateId(Long candidateId);
    long countByCampaignId(Long campaignId);
    boolean existsByVoterIdAndCampaignId(Long voterId, Long campaignId);
    java.util.List<Vote> findAllByCampaignIsNull();
    long deleteAllByCampaignId(Long campaignId);
}
