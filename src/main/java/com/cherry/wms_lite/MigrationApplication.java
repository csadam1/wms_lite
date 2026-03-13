package com.cherry.wms_lite;

import com.cherry.wms_lite.model.entity.ContainerEntity;
import com.cherry.wms_lite.model.entity.ContainerTypeEntity;
import com.cherry.wms_lite.model.enumerate.ContainerStatusEnum;
import com.cherry.wms_lite.repository.container.ContainerRepository;
import com.cherry.wms_lite.repository.container.ContainerTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Instant;

@SpringBootApplication
public class MigrationApplication {

    @Bean
    CommandLineRunner initData(ContainerTypeRepository typeRepo,
                               ContainerRepository containerRepo) {
        return args -> {
            ContainerTypeEntity box = ContainerTypeEntity
                    .builder()
                    .name("Box")
                    .description("Standard cardboard box")
                    .build();
            box = typeRepo.save(box);

            ContainerTypeEntity crate = ContainerTypeEntity
                    .builder()
                    .name("Crate")
                    .description("Wooden crate")
                    .build();
            crate = typeRepo.save(crate);

            ContainerEntity box001 = ContainerEntity
                    .builder()
                    .serialNumber("BOX-001")
                    .containerType(box)
                    .createdAt(Instant.now())
                    .status(ContainerStatusEnum.OPEN)
                    .build();
            containerRepo.save(box001);

            ContainerEntity crt999 = ContainerEntity
                    .builder()
                    .serialNumber("CRT-999")
                    .containerType(crate)
                    .createdAt(Instant.now())
                    .status(ContainerStatusEnum.CLOSED)
                    .build();
            containerRepo.save(crt999);
        };
    }
}
