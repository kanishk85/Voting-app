package com.voting.controller;

import com.voting.entity.Candidate;
import com.voting.repository.UserRepository;
import com.voting.repository.VoteRepository;
import com.voting.service.CampaignService;
import com.voting.service.CandidateService;
import com.voting.service.VoteService;
import java.security.Principal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CampaignController {
    private final CampaignService campaigns;
    private final CandidateService candidates;
    private final VoteRepository votes;
    private final UserRepository users;
    private final VoteService voteService;

    public CampaignController(CampaignService campaigns, CandidateService candidates, VoteRepository votes,
                              UserRepository users, VoteService voteService) {
        this.campaigns = campaigns;
        this.candidates = candidates;
        this.votes = votes;
        this.users = users;
        this.voteService = voteService;
    }

    @GetMapping("/campaigns/{campaignId}")
    public String detail(@PathVariable Long campaignId, Principal principal, Model model) {
        var campaign = campaigns.findById(campaignId);
        var campaignCandidates = candidates.findByCampaign(campaignId);
        Map<Long, Long> candidateVotes = new LinkedHashMap<>();
        Candidate leadingCandidate = null;
        long leadingVotes = 0;
        boolean hasVoted = false;
        boolean isAdmin = false;
        if (principal != null) {
            var voter = users.findByEmailIgnoreCase(principal.getName()).orElse(null);
            if (voter != null) {
                isAdmin = voter.getRole() == com.voting.entity.Role.ROLE_ADMIN;
                hasVoted = votes.existsByVoterIdAndCampaignId(voter.getId(), campaignId);
            }
        }

        boolean showResults = isAdmin || campaign.getEndDate().isBefore(LocalDate.now());
        long totalVotes = 0;
        if (showResults) {
            for (var candidate : campaignCandidates) {
                long count = votes.countByCandidateId(candidate.getId());
                candidateVotes.put(candidate.getId(), count);
                totalVotes += count;
                if (leadingCandidate == null || count > leadingVotes) {
                    leadingCandidate = candidate;
                    leadingVotes = count;
                }
            }
        }

        model.addAttribute("campaign", campaign);
        model.addAttribute("candidates", campaignCandidates);
        model.addAttribute("candidateVotes", candidateVotes);
        model.addAttribute("totalVotes", totalVotes);
        model.addAttribute("leadingCandidate", leadingCandidate);
        model.addAttribute("leadingVotes", leadingVotes);
        model.addAttribute("hasVoted", hasVoted);
        model.addAttribute("showResults", showResults);
        model.addAttribute("voteSuccess", model.asMap().get("voteSuccess"));
        model.addAttribute("voteError", model.asMap().get("voteError"));
        return "campaign";
    }

    @PostMapping("/campaigns/{campaignId}/vote")
    public String vote(@PathVariable Long campaignId, Principal principal,
                       @RequestParam(required = false) Long candidateId,
                       RedirectAttributes redirectAttributes) {
        if (candidateId == null) {
            redirectAttributes.addFlashAttribute("voteError", "Choose a candidate first.");
        } else {
            try {
                voteService.castVote(principal.getName(), campaignId, candidateId);
                redirectAttributes.addFlashAttribute("voteSuccess", "Your vote has been submitted.");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                redirectAttributes.addFlashAttribute("voteError", ex.getMessage());
            }
        }
        return "redirect:/campaigns/" + campaignId;
    }
}
