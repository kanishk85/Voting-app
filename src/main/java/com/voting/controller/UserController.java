package com.voting.controller;

import com.voting.repository.UserRepository;
import com.voting.service.CampaignService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {
    private final UserRepository users;
    private final CampaignService campaigns;
    public UserController(UserRepository users, CampaignService campaigns) {
        this.users = users;
        this.campaigns = campaigns;
    }

    @GetMapping("/user")
    public String dashboard(java.security.Principal principal, Model model) {
        var user = users.findByEmailIgnoreCase(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Signed in account could not be found."));
        model.addAttribute("voter", user);
        model.addAttribute("campaigns", campaigns.findAll().stream().filter(com.voting.entity.Campaign::isOpen).toList());
        return "user-dashboard";
    }
}
