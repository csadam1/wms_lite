package com.cherry.wms_lite.repository.container;

import com.cherry.wms_lite.model.entity.ContainerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContainerRepository extends JpaRepository<ContainerEntity, Long> {
    Optional<ContainerEntity> findBySerialNumber(String serialNumber);
}
