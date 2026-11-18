package com.voting.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.voting.repository.UserRepository;
import com.voting.service.CampaignService;
import com.voting.service.ResultsService;

@Controller
public class UserController {
    private final UserRepository users;
    private final CampaignService campaigns;
    private final ResultsService resultsService;
    public UserController(UserRepository users, CampaignService campaigns, ResultsService resultsService) {
        this.users = users;
        this.campaigns = campaigns;
        this.resultsService = resultsService;
    }

    @GetMapping("/user")
    public String dashboard(java.security.Principal principal, Model model) {
        var user = users.findByEmailIgnoreCase(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Signed in account could not be found."));
        model.addAttribute("voter", user);
        model.addAttribute("campaigns", campaigns.findAll().stream()
                .filter(campaign -> !campaign.getEndDate().isBefore(java.time.LocalDate.now())).toList());
        return "user-dashboard";
    }

    @GetMapping({"/results", "/user/results"})
    public String results(java.security.Principal principal, Model model) {
        var user = users.findByEmailIgnoreCase(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Signed in account could not be found."));
        model.addAttribute("results", resultsService.resultsFor(user.getId()));
        return "results";
    }

    @GetMapping("/results/campaigns/{campaignId}")
    public String campaignResult(@org.springframework.web.bind.annotation.PathVariable Long campaignId,
                                 java.security.Principal principal, Model model) {
        var user = users.findByEmailIgnoreCase(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Signed in account could not be found."));
        var result = resultsService.resultFor(campaignId, user.getId());
        if (result == null) return "redirect:/results";
        model.addAttribute("result", result);
        return "result-detail";
    }
}
