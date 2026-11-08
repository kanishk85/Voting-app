package com.voting.repository;

import com.voting.entity.Candidate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    List<Candidate> findAllByOrderByIdAsc();
    List<Candidate> findAllByCampaignIdOrderByIdAsc(Long campaignId);
    List<Candidate> findAllByCampaignIsNullOrderByIdAsc();
    boolean existsByCampaignIdAndNameIgnoreCase(Long campaignId, String name);
    long deleteAllByCampaignId(Long campaignId);
}
