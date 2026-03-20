package com.cherry.wms_lite.repository;

import com.cherry.wms_lite.model.entity.StorageLocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StorageLocationRepository extends JpaRepository<StorageLocationEntity, Long> {
    Optional<StorageLocationEntity> findByName(String name);
}
