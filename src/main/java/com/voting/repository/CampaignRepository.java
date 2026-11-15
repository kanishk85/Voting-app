package com.voting.repository;

import com.voting.entity.Campaign;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findAllByOrderByStartDateDesc();
}
