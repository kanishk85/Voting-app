package com.voting.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.voting.service.CampaignService;

@Controller
public class HomeController {
    private final CampaignService campaigns;

    public HomeController(CampaignService campaigns) { this.campaigns = campaigns; }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("campaigns", campaigns.findAll());
        return "home";
    }

    @GetMapping("/about")
    public String about() { return "about"; }

    @GetMapping("/login")
    public String login(@org.springframework.web.bind.annotation.RequestParam(required = false) Long campaignId, Model model) {
        model.addAttribute("campaignId", campaignId);
        return "login";
    }

    @GetMapping("/register")
    public String register(org.springframework.ui.Model model) {
        model.addAttribute("registration", new AuthController.RegistrationForm());
        return "register";
    }
}
