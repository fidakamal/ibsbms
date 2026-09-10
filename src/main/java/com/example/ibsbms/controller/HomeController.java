package com.example.ibsbms.controller;

import com.example.ibsbms.repository.ShareholderListProjection;
import com.example.ibsbms.repository.ShareholderRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {

    private final ShareholderRepository shareholderRepository;

    public HomeController(ShareholderRepository shareholderRepository) {
        this.shareholderRepository = shareholderRepository;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/shareholders")
    public String shareholders(
            @RequestParam(required = false) String folioBo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        /*
         * Only allow the page sizes shown in the UI.
         */
        if (size != 10 && size != 25 && size != 50 && size != 100) {
            size = 10;
        }

        /*
         * Clean the Folio search value.
         */
        String searchFolio = folioBo;

        if (searchFolio != null) {
            searchFolio = searchFolio.trim();

            if (searchFolio.isEmpty()) {
                searchFolio = null;
            }
        }

        /*
         * Prevent invalid page numbers.
         */
        if (page < 1) {
            page = 1;
        }

        /*
         * Find total number of matching shareholders.
         */
        long totalItems =
                shareholderRepository.countShareholders(searchFolio);

        /*
         * Calculate total pages.
         */
        int totalPages =
                (int) Math.ceil((double) totalItems / size);

        /*
         * If the requested page is beyond the last page,
         * move to the last available page.
         */
        if (totalPages > 0 && page > totalPages) {
            page = totalPages;
        }

        /*
         * Oracle ROW_NUMBER() uses 1-based row numbers.
         */
        int startRow = ((page - 1) * size) + 1;
        int endRow = page * size;

        /*
         * Get only the records for the current page.
         */
        List<ShareholderListProjection> shareholders =
                shareholderRepository.findShareholderList(
                        searchFolio,
                        startRow,
                        endRow
                );

        /*
         * Calculate the displayed result range.
         */
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
        model.addAttribute("folioBo", folioBo);

        model.addAttribute("page", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startEntry", startEntry);
        model.addAttribute("endEntry", endEntry);

        return "shareholder/shareholder-list";
    }
}