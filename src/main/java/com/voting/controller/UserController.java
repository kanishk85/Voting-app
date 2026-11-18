package com.voting.controller;

import com.voting.repository.UserRepository;
import com.voting.repository.VoteRepository;
import com.voting.service.CampaignService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {
    private final UserRepository users;
    private final CampaignService campaigns;
    private final VoteRepository votes;
    public UserController(UserRepository users, CampaignService campaigns, VoteRepository votes) {
        this.users = users;
        this.campaigns = campaigns;
        this.votes = votes;
    }

    @GetMapping("/user")
    public String dashboard(java.security.Principal principal, Model model) {
        var user = users.findByEmailIgnoreCase(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Signed in account could not be found."));
        model.addAttribute("voter", user);
        model.addAttribute("campaigns", campaigns.findAll().stream().filter(com.voting.entity.Campaign::isOpen).toList());
        return "user-dashboard";
    }

    @GetMapping("/user/results")
    public String results(java.security.Principal principal, Model model) {
        var user = users.findByEmailIgnoreCase(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Signed in account could not be found."));
        List<CampaignResult> results = new ArrayList<>();
        for (var campaign : campaigns.findAll()) {
            if (campaign.getEndDate().isBefore(java.time.LocalDate.now())) {
                var campaignVotes = votes.findAllByCampaignId(campaign.getId());
                var counts = campaignVotes.stream().collect(java.util.stream.Collectors.groupingBy(
                        vote -> vote.getCandidate(), java.util.stream.Collectors.counting()));
                var winner = counts.entrySet().stream().max(Comparator.<java.util.Map.Entry<com.voting.entity.Candidate, Long>>comparingByValue()
                        .thenComparing(entry -> entry.getKey().getName(), Comparator.reverseOrder())).orElse(null);
                var ownVote = campaignVotes.stream().filter(vote -> vote.getVoter().getId().equals(user.getId())).findFirst().orElse(null);
                results.add(new CampaignResult(campaign, winner == null ? null : winner.getKey(),
                        winner == null ? 0 : winner.getValue(), ownVote == null ? null : ownVote.getCandidate()));
            } else {
                results.add(new CampaignResult(campaign, null, 0, null));
            }
        }
        model.addAttribute("results", results);
        return "results";
    }

    public record CampaignResult(com.voting.entity.Campaign campaign, com.voting.entity.Candidate winner,
                                 long winnerVotes, com.voting.entity.Candidate voterChoice) {
        public boolean isAvailable() { return campaign.getEndDate().isBefore(java.time.LocalDate.now()); }
    }
}
