package com.civiccomplaint.user;

import com.civiccomplaint.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/citizen")
@RequiredArgsConstructor
public class CitizenController {

    private final UserService userService;

    @GetMapping("/admin-poster")
    @PreAuthorize("hasAuthority('ROLE_CITIZEN')")
    public ResponseEntity<byte[]> getAdminPoster(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("GET /api/citizen/admin-poster - Fetching poster for citizen: {}", userDetails.getUsername());
        return userService.getAdminPosterForCitizenByEmail(userDetails.getUsername());
    }
}
