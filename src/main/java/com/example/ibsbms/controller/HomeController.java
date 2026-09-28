package com.example.ibsbms.controller;

import com.example.ibsbms.repository.ShareholderListProjection;
import com.example.ibsbms.repository.ShareholderRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import com.example.ibsbms.entity.AccountCdbl;
import com.example.ibsbms.repository.AccountCdblRepository;

@Controller
public class HomeController {

    private final ShareholderRepository shareholderRepository;
    private final AccountCdblRepository accountCdblRepository;

    public HomeController(
            ShareholderRepository shareholderRepository,
            AccountCdblRepository accountCdblRepository) {

        this.shareholderRepository = shareholderRepository;
        this.accountCdblRepository = accountCdblRepository;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/shareholders")
    public String shareholders(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        if (size != 10 && size != 25 && size != 50 && size != 100) {
            size = 10;
        }

        String searchValue = search;

        if (searchValue != null) {
            searchValue = searchValue.trim();

            if (searchValue.isEmpty()) {
                searchValue = null;
            }
        }

        if (page < 1) {
            page = 1;
        }

        AccountCdbl boRecord = null;

        if (searchValue != null) {
            boRecord = accountCdblRepository
                    .findByBoNo(searchValue)
                    .orElse(null);
        }

        long totalItems =
                shareholderRepository.countShareholders(searchValue);

        int totalPages =
                (int) Math.ceil((double) totalItems / size);

        if (totalPages > 0 && page > totalPages) {
            page = totalPages;
        }

        int startRow = ((page - 1) * size) + 1;
        int endRow = page * size;

        List<ShareholderListProjection> shareholders =
                shareholderRepository.findShareholderList(
                        searchValue,
                        startRow,
                        endRow
                );

        long startEntry = 0;
        long endEntry = 0;

        if (totalItems > 0) {
            startEntry = ((long) (page - 1) * size) + 1;
            endEntry = Math.min(
                    (long) page * size,
                    totalItems
            );
        }

        model.addAttribute("shareholders", shareholders);
        model.addAttribute("search", search);
        model.addAttribute("page", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startEntry", startEntry);
        model.addAttribute("endEntry", endEntry);

        model.addAttribute("boRecord", boRecord);

        return "shareholder/shareholder-list";
    }
}