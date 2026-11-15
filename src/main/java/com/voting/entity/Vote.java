package com.voting.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(name = "votes", uniqueConstraints = @UniqueConstraint(name = "uk_votes_voter_campaign", columnNames = {"voter_id", "campaign_id"}))
public class Vote {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "voter_id", nullable = false)
    private User voter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    // The free SQL host reports a MySQL 5.5-compatible server, which does not
    // support fractional seconds (datetime(6)). Keep this column compatible with it.
    @Column(nullable = false, updatable = false, columnDefinition = "datetime not null")
    private Instant createdAt;

    protected Vote() {}

    public Vote(User voter, Candidate candidate, Campaign campaign, Instant createdAt) {
        this.voter = voter;
        this.candidate = candidate;
        this.campaign = campaign;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public User getVoter() { return voter; }
    public Candidate getCandidate() { return candidate; }
    public Campaign getCampaign() { return campaign; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCampaign(Campaign campaign) { this.campaign = campaign; }
}
