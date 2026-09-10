package com.example.ibsbms.config;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute
    public void addCurrentUser(Model model) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return;
        }

        boolean isMaker = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MAKER"));

        boolean isChecker = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CHECKER"));

        model.addAttribute("currentUsername", authentication.getName());
        model.addAttribute("isMaker", isMaker);
        model.addAttribute("isChecker", isChecker);
    }
}