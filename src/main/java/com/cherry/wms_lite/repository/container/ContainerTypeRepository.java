package com.cherry.wms_lite.repository.container;

import com.cherry.wms_lite.model.entity.ContainerTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContainerTypeRepository extends JpaRepository<ContainerTypeEntity, Long> {
}
