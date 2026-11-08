package com.voting.controller;

import com.voting.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {
    private final UserService users;

    public AuthController(UserService users) { this.users = users; }

    public static class RegistrationForm {
        @NotBlank(message = "Please enter your name.") @Size(max = 100, message = "Name must be at most 100 characters.")
        private String name;
        @NotBlank(message = "Please enter your email.") @Email(message = "Please enter a valid email address.") @Size(max = 254, message = "Email is too long.")
        private String email;
        @NotBlank(message = "Please enter your phone number.") @Pattern(regexp = "[+0-9() .-]{7,30}", message = "Please enter a valid phone number.")
        private String phone;
        @NotBlank(message = "Please enter a password.") @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters.")
        private String password;
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getPassword() { return password; }
        public void setName(String name) { this.name = name; }
        public void setEmail(String email) { this.email = email; }
        public void setPhone(String phone) { this.phone = phone; }
        public void setPassword(String password) { this.password = password; }
    }

    @PostMapping("/register")
    public String createAccount(@Valid @ModelAttribute("registration") RegistrationForm form,
                                BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) return "register";
        try {
            users.register(form.getName(), form.getEmail(), form.getPhone(), form.getPassword());
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("email", "duplicate", ex.getMessage());
            return "register";
        }
        return "redirect:/login?registered";
    }
}
