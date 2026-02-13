package com.civiccomplaint.master;

import com.civiccomplaint.master.dto.CorporatorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Corporator operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CorporatorService {

    private final CorporatorRepository corporatorRepository;

    /**
     * Get all corporators.
     *
     * @return list of corporator responses
     */
    @Transactional(readOnly = true)
    public List<CorporatorResponse> getAllCorporators() {
        return corporatorRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CorporatorResponse mapToResponse(Corporator corporator) {
        return CorporatorResponse.builder()
                .id(corporator.getId())
                .fullName(corporator.getFullName())
                .email(corporator.getEmail())
                .mobileNumber(corporator.getMobileNumber())
                .prabhagId(corporator.getPrabhag().getId())
                .prabhagName(corporator.getPrabhag().getName())
                .isUserCreated(corporator.getIsUserCreated())
                .build();
    }
}
