package com.cherry.wms_lite.controller.container;

import com.cherry.wms_lite.WmsLiteApplication;
import com.cherry.wms_lite.config.SecurityConfig;
import com.cherry.wms_lite.model.enumerate.ContainerStatusEnum;
import com.cherry.wms_lite.model.enumerate.LocationTypeEnum;
import com.cherry.wms_lite.model.request.container.ContainerRequest;
import com.cherry.wms_lite.model.response.container.ContainerResponse;
import com.cherry.wms_lite.service.container.ContainerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ContainerController.class)
@ContextConfiguration(classes = WmsLiteApplication.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.security.admin.username=admin",
        "app.security.admin.password=admin",
        "app.security.admin.role=ADMIN"
})
@WithMockUser
class ContainerControllerTest {
    private static final String CONTAINER_TYPE_1 = "Container Type 1";
    private static final String CONTAINER_TYPE_2 = "Container Type 2";
    private static final String SERIAL_NUMBER_1 = "Serial Number 1";
    private static final String SERIAL_NUMBER_2 = "Serial Number 2";
    private static final String EMPTY_SERIAL_NUMBER = "";
    private static final ContainerStatusEnum STATUS_1 = ContainerStatusEnum.OPEN;
    private static final ContainerStatusEnum STATUS_2 = ContainerStatusEnum.OPEN;
    private static final String LOCATION_NAME_1 = "Wooden Box";
    private static final String LOCATION_NAME_2 = "Gold Storage";
    private static final LocationTypeEnum LOCATION_TYPE_ENUM_1 = LocationTypeEnum.CONTAINER;
    private static final LocationTypeEnum LOCATION_TYPE_ENUM_2 = LocationTypeEnum.STORAGE_LOCATION;
    private static final String CONTAINER_NOT_FOUND_WITH_ID = "Container not found with id: 2";
    private static final String SERIAL_NUMBER_IS_REQUIRED = "Serial number is required";
    private static final String CONTAINER_WITH_SERIAL_NUMBER_EXIST =
            "Container with serial number already exists: Serial Number 1";
    private static final String CONTAINER_NOT_EMPTY =
            "Cannot remove container with non-empty inventory. Container id: 2";
    private static final String STORAGE_LOCATION_NOT_FOUND_WITH_NAME =
            "Storage Location not found with name: Gold Storage";
    private static final String CONTAINER_OVERLOADED = "Container Overloaded";
    private static final String ERROR_MESSAGE_PATH = "$.message";
    private static final String ERROR_STATUS_PATH = "$.status";
    private static final String ERROR_STATUS_NAME_PATH = "$.error";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ContainerService containerService;

    @Test
    void getAllContainers_returnsOk() throws Exception {
        // Arrange
        List<ContainerResponse> containerResponseList = List.of(
                new ContainerResponse(1L, CONTAINER_TYPE_1, SERIAL_NUMBER_1, Instant.now(), STATUS_1, LOCATION_NAME_1),
                new ContainerResponse(2L, CONTAINER_TYPE_2, SERIAL_NUMBER_2, Instant.now(), STATUS_2, LOCATION_NAME_1)
        );
        when(containerService.getAllContainers()).thenReturn(containerResponseList);

        // Act and Assert
        mockMvc.perform(get("/container/all"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(containerResponseList)));
    }

    @Test
    void getContainerById_returnsOk() throws Exception {
        // Arrange
        ContainerResponse response =
                new ContainerResponse(1L, CONTAINER_TYPE_1, SERIAL_NUMBER_1, Instant.now(), STATUS_1, LOCATION_NAME_1);
        when(containerService.getContainerById(1L)).thenReturn(response);

        // Act and Assert
        mockMvc.perform(get("/container/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void getContainerById_storageLocationNotFound() throws Exception {
        // Arrange
        EntityNotFoundException exception = new EntityNotFoundException(CONTAINER_NOT_FOUND_WITH_ID);
        when(containerService.getContainerById(2L)).thenThrow(exception);

        // Act and Assert
        mockMvc.perform(get("/container/2"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value(CONTAINER_NOT_FOUND_WITH_ID))
                .andExpect(jsonPath(ERROR_STATUS_PATH).value(404))
                .andExpect(jsonPath(ERROR_STATUS_NAME_PATH).value("Not Found"));
    }

    @Test
    void createContainer_returnsOk() throws Exception {
        // Arrange
        ContainerRequest request =
                new ContainerRequest(SERIAL_NUMBER_1, CONTAINER_TYPE_1, STATUS_1, LOCATION_NAME_1, LOCATION_TYPE_ENUM_1);
        ContainerResponse response =
                new ContainerResponse(1L, CONTAINER_TYPE_1, SERIAL_NUMBER_1, Instant.now(), STATUS_1, LOCATION_NAME_1);
        when(containerService.createContainer(request)).thenReturn(response);

        // Act and Assert
        mockMvc.perform(post("/container")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void createContainer_withBlankSerialNumber_returnsBadRequest() throws Exception {
        // Arrange
        ContainerRequest request =
                new ContainerRequest(EMPTY_SERIAL_NUMBER, CONTAINER_TYPE_1, STATUS_1, LOCATION_NAME_1,
                        LOCATION_TYPE_ENUM_1);

        // Act and Assert
        mockMvc.perform(post("/container")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value(SERIAL_NUMBER_IS_REQUIRED))
                .andExpect(jsonPath(ERROR_STATUS_PATH).value(400))
                .andExpect(jsonPath(ERROR_STATUS_NAME_PATH).value("Bad Request"));
    }

    @Test
    void createContainer_withNullSerialNumber_returnsBadRequest() throws Exception {
        // Arrange
        ContainerRequest request =
                new ContainerRequest(null, CONTAINER_TYPE_1, STATUS_1, LOCATION_NAME_1, LOCATION_TYPE_ENUM_1);

        // Act and Assert
        mockMvc.perform(post("/container")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value(SERIAL_NUMBER_IS_REQUIRED))
                .andExpect(jsonPath(ERROR_STATUS_PATH).value(400))
                .andExpect(jsonPath(ERROR_STATUS_NAME_PATH).value("Bad Request"));
    }

    @Test
    void createContainer_serialNumberAlreadyExist() throws Exception {
        // Arrange
        ContainerRequest request =
                new ContainerRequest(SERIAL_NUMBER_1, CONTAINER_TYPE_1, STATUS_1, LOCATION_NAME_1, LOCATION_TYPE_ENUM_1);

        IllegalArgumentException exception = new IllegalArgumentException(CONTAINER_WITH_SERIAL_NUMBER_EXIST);
        when(containerService.createContainer(request)).thenThrow(exception);

        // Act and Assert
        mockMvc.perform(post("/container")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value(CONTAINER_WITH_SERIAL_NUMBER_EXIST))
                .andExpect(jsonPath(ERROR_STATUS_PATH).value(400))
                .andExpect(jsonPath(ERROR_STATUS_NAME_PATH).value("Bad Request"));
    }

    @Test
    void updateContainer_returnsOk() throws Exception {
        // Arrange
        ContainerRequest request =
                new ContainerRequest(SERIAL_NUMBER_1, CONTAINER_TYPE_1, STATUS_1, LOCATION_NAME_1, LOCATION_TYPE_ENUM_1);
        ContainerResponse response =
                new ContainerResponse(1L, CONTAINER_TYPE_1, SERIAL_NUMBER_1, Instant.now(), STATUS_1, LOCATION_NAME_1);
        when(containerService.updateContainer(eq(1L), any())).thenReturn(response);

        // Act and Assert
        mockMvc.perform(put("/container/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void updateContainer_nameAlreadyExist() throws Exception {
        // Arrange
        ContainerRequest request =
                new ContainerRequest(SERIAL_NUMBER_1, CONTAINER_TYPE_1, STATUS_1, LOCATION_NAME_1, LOCATION_TYPE_ENUM_1);

        IllegalArgumentException exception = new IllegalArgumentException(CONTAINER_WITH_SERIAL_NUMBER_EXIST);
        when(containerService.updateContainer(1L, request)).thenThrow(exception);

        // Act and Assert
        mockMvc.perform(put("/container/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value(CONTAINER_WITH_SERIAL_NUMBER_EXIST))
                .andExpect(jsonPath(ERROR_STATUS_PATH).value(400))
                .andExpect(jsonPath(ERROR_STATUS_NAME_PATH).value("Bad Request"));
    }

    @Test
    void updateContainer_locationNameDoesNotExists() throws Exception {
        // Arrange
        ContainerRequest request =
                new ContainerRequest(SERIAL_NUMBER_1, CONTAINER_TYPE_1, STATUS_1, LOCATION_NAME_2, LOCATION_TYPE_ENUM_2);

        IllegalArgumentException exception = new IllegalArgumentException(STORAGE_LOCATION_NOT_FOUND_WITH_NAME);
        when(containerService.updateContainer(1L, request)).thenThrow(exception);

        // Act and Assert
        mockMvc.perform(put("/container/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value(STORAGE_LOCATION_NOT_FOUND_WITH_NAME))
                .andExpect(jsonPath(ERROR_STATUS_PATH).value(400))
                .andExpect(jsonPath(ERROR_STATUS_NAME_PATH).value("Bad Request"));
    }

    @Test
    void updateContainer_containerOverloaded() throws Exception {
        // Arrange
        ContainerRequest request =
                new ContainerRequest(SERIAL_NUMBER_1, CONTAINER_TYPE_1, STATUS_1, LOCATION_NAME_1, LOCATION_TYPE_ENUM_1);

        IllegalStateException exception = new IllegalStateException(CONTAINER_OVERLOADED);
        when(containerService.updateContainer(1L, request)).thenThrow(exception);

        // Act and Assert
        mockMvc.perform(put("/container/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value(CONTAINER_OVERLOADED))
                .andExpect(jsonPath(ERROR_STATUS_PATH).value(409))
                .andExpect(jsonPath(ERROR_STATUS_NAME_PATH).value("Conflict"));
    }

    @Test
    void removeContainerById_returnsNoContent() throws Exception {
        // Arrange
        doNothing().when(containerService).removeContainerById(1L);

        // Act and Assert
        mockMvc.perform(delete("/container/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeContainerById_containerNotFound() throws Exception {
        // Arrange
        EntityNotFoundException exception = new EntityNotFoundException(CONTAINER_NOT_FOUND_WITH_ID);
        doThrow(exception).when(containerService).removeContainerById(2L);

        // Act and Assert
        mockMvc.perform(delete("/container/2"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value(CONTAINER_NOT_FOUND_WITH_ID))
                .andExpect(jsonPath(ERROR_STATUS_PATH).value(404))
                .andExpect(jsonPath(ERROR_STATUS_NAME_PATH).value("Not Found"));
    }

    @Test
    void removeContainerById_containerNotEmpty() throws Exception {
        // Arrange
        IllegalStateException exception = new IllegalStateException(CONTAINER_NOT_EMPTY);
        doThrow(exception).when(containerService).removeContainerById(2L);

        // Act and Assert
        mockMvc.perform(delete("/container/2"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value(CONTAINER_NOT_EMPTY))
                .andExpect(jsonPath(ERROR_STATUS_PATH).value(409))
                .andExpect(jsonPath(ERROR_STATUS_NAME_PATH).value("Conflict"));
    }
}
