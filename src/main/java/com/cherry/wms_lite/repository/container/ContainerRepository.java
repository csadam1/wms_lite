package com.cherry.wms_lite.repository.container;

import com.cherry.wms_lite.model.entity.ContainerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContainerRepository extends JpaRepository<ContainerEntity, Long> {
    List<ContainerEntity> findAllByContainerType_IdAndRemovedFalse(@NonNull Long id);

    List<ContainerEntity> findAllByRemovedFalse();

    Optional<ContainerEntity> findByIdAndRemovedFalse(@NonNull Long id);

    Optional<ContainerEntity> findBySerialNumberAndRemovedFalse(@NonNull String serialNumber);

    boolean existsByContainerType_Id(@NonNull Long id);

    boolean existsByRemovedFalseAndAttachedToInventoryEntity_StorageLocation_Id(@NonNull Long id);
}
