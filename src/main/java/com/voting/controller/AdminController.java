package com.voting.controller;

import com.voting.entity.Campaign;
import com.voting.repository.CampaignRepository;
import com.voting.repository.UserRepository;
import com.voting.repository.VoteRepository;
import com.voting.service.CampaignService;
import com.voting.service.CandidateService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {
    private final CampaignService campaigns;
    private final CandidateService candidates;
    private final CampaignRepository campaignRepository;
    private final UserRepository users;
    private final VoteRepository votes;

    public AdminController(CampaignService campaigns, CandidateService candidates, CampaignRepository campaignRepository,
                           UserRepository users, VoteRepository votes) {
        this.campaigns = campaigns;
        this.candidates = candidates;
        this.campaignRepository = campaignRepository;
        this.users = users;
        this.votes = votes;
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        if (!model.containsAttribute("campaignForm")) model.addAttribute("campaignForm", new CampaignForm());
        model.addAttribute("totalVoters", users.countByRole(com.voting.entity.Role.ROLE_NORMAL));
        model.addAttribute("campaigns", summaries(campaignRepository.findAllByOrderByStartDateDesc()));
        return "admin-dashboard";
    }

    @PostMapping("/admin/campaigns")
    public String createCampaign(@Valid @ModelAttribute("campaignForm") CampaignForm form,
                                 BindingResult binding, Model model, RedirectAttributes redirectAttributes) {
        if (form.getStartDate() != null && form.getEndDate() != null && form.getEndDate().isBefore(form.getStartDate())) {
            binding.rejectValue("endDate", "dateOrder", "End date must be on or after the start date.");
        }
        if (binding.hasErrors()) return dashboard(model);
        campaigns.create(form.getName(), form.getTagline(), form.getStartDate(), form.getEndDate());
        redirectAttributes.addFlashAttribute("campaignMessage", "Campaign created.");
        return "redirect:/admin";
    }

    @GetMapping("/admin/campaigns/{campaignId}")
    public String manageCampaign(@PathVariable Long campaignId, Model model) {
        var campaign = campaigns.findById(campaignId);
        var campaignCandidates = candidates.findByCampaign(campaignId);
        Map<Long, Long> candidateVotes = new LinkedHashMap<>();
        long totalVotes = 0;
        for (var candidate : campaignCandidates) {
            long count = votes.countByCandidateId(candidate.getId());
            candidateVotes.put(candidate.getId(), count);
            totalVotes += count;
        }
        CandidateSummary leader = campaignCandidates.stream()
                .map(candidate -> new CandidateSummary(candidate, candidateVotes.get(candidate.getId())))
                .max(java.util.Comparator.comparingLong(CandidateSummary::votes)).orElse(null);
        model.addAttribute("campaign", campaign);
        model.addAttribute("candidates", campaignCandidates);
        model.addAttribute("candidateVotes", candidateVotes);
        model.addAttribute("totalVotes", totalVotes);
        model.addAttribute("leader", leader);
        return "admin-campaign";
    }

    @PostMapping("/admin/campaigns/{campaignId}/candidates")
    public String addCandidate(@PathVariable Long campaignId, @RequestParam String candidateName,
                               RedirectAttributes redirectAttributes) {
        Campaign campaign = campaigns.findById(campaignId);
        if (votes.countByCampaignId(campaignId) > 0) {
            redirectAttributes.addFlashAttribute("candidateError", "Candidates cannot be changed after voting starts.");
        } else {
            try {
                candidates.addToCampaign(campaign, candidateName);
                redirectAttributes.addFlashAttribute("candidateMessage", "Candidate added.");
            } catch (IllegalArgumentException ex) {
                redirectAttributes.addFlashAttribute("candidateError", ex.getMessage());
            }
        }
        return "redirect:/admin/campaigns/" + campaignId;
    }

    @PostMapping("/admin/campaigns/{campaignId}/delete")
    public String deleteCampaign(@PathVariable Long campaignId, RedirectAttributes redirectAttributes) {
        campaigns.delete(campaignId);
        redirectAttributes.addFlashAttribute("campaignMessage", "Campaign and its votes have been deleted.");
        return "redirect:/admin";
    }

    private List<CampaignSummary> summaries(List<Campaign> allCampaigns) {
        List<CampaignSummary> results = new ArrayList<>();
        for (Campaign campaign : allCampaigns) {
            var list = candidates.findByCampaign(campaign.getId());
            CandidateSummary leader = list.stream()
                    .map(candidate -> new CandidateSummary(candidate, votes.countByCandidateId(candidate.getId())))
                    .max(java.util.Comparator.comparingLong(CandidateSummary::votes)).orElse(null);
            results.add(new CampaignSummary(campaign, votes.countByCampaignId(campaign.getId()), leader));
        }
        return results;
    }

    public record CandidateSummary(com.voting.entity.Candidate candidate, long votes) {}
    public record CampaignSummary(Campaign campaign, long totalVotes, CandidateSummary leader) {}

    public static class CampaignForm {
        @NotBlank(message = "Enter a campaign name.") @Size(max = 120, message = "Name must be at most 120 characters.")
        private String name;
        @NotBlank(message = "Enter a short description.") @Size(max = 255, message = "Description must be at most 255 characters.")
        private String tagline;
        @NotNull(message = "Choose a start date.")
        private LocalDate startDate;
        @NotNull(message = "Choose an end date.")
        private LocalDate endDate;
        public String getName() { return name; }
        public String getTagline() { return tagline; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setName(String name) { this.name = name; }
        public void setTagline(String tagline) { this.tagline = tagline; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    }
}
