package com.voting.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@Table(name = "candidates", uniqueConstraints = @UniqueConstraint(name = "uk_candidates_campaign_code", columnNames = {"campaign_id", "code"}))
public class Candidate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 120)
    private String affiliation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    public Candidate() {}

    public Candidate(String code, String name, String affiliation, Campaign campaign) {
        this.code = code;
        this.name = name;
        this.affiliation = affiliation;
        this.campaign = campaign;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getAffiliation() { return affiliation; }
    public Campaign getCampaign() { return campaign; }
    public void setCode(String code) { this.code = code; }
    public void setName(String name) { this.name = name; }
    public void setAffiliation(String affiliation) { this.affiliation = affiliation; }
    public void setCampaign(Campaign campaign) { this.campaign = campaign; }
}
