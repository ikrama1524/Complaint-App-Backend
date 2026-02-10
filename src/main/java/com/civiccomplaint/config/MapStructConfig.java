package com.civiccomplaint.config;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * Global MapStruct configuration.
 * Applied to all mappers using @Mapper(config = MapStructConfig.class)
 */
@MapperConfig(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface MapStructConfig {
}
