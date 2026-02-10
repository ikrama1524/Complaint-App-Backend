package com.civiccomplaint.master;

import com.civiccomplaint.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/master")
@RequiredArgsConstructor
public class MasterController {

    private final PrabhagRepository prabhagRepository;

    @GetMapping("/prabhags")
    public ResponseEntity<ApiResponse<List<Prabhag>>> getAllPrabhags() {
        log.info("GET /api/master/prabhags - Fetching all prabhags");
        List<Prabhag> prabhags = prabhagRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(prabhags));
    }

    @org.springframework.web.bind.annotation.PostMapping("/prabhags")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Prabhag>> createPrabhag(
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.civiccomplaint.master.dto.CreatePrabhagRequest request) {
        log.info("POST /api/master/prabhags - Creating new prabhag: {}", request.getName());

        if (prabhagRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Prabhag with name '" + request.getName() + "' already exists");
        }

        if (prabhagRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Prabhag with code '" + request.getCode() + "' already exists");
        }

        Prabhag prabhag = new Prabhag();
        prabhag.setName(request.getName());
        prabhag.setCode(request.getCode());
        prabhag.setDescription(request.getDescription());

        prabhag = prabhagRepository.save(prabhag);
        log.info("Prabhag created successfully with ID: {}", prabhag.getId());

        return ResponseEntity.ok(ApiResponse.success(prabhag));
    }
}
