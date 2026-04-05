package com.cherry.wms_lite.controller.container_type;

import com.cherry.wms_lite.WmsLiteApplication;
import com.cherry.wms_lite.model.request.container_type.ContainerTypeRequest;
import com.cherry.wms_lite.model.response.container_type.ContainerTypeResponse;
import com.cherry.wms_lite.service.container_type.ContainerTypeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ContainerTypeController.class)
@ContextConfiguration(classes = WmsLiteApplication.class)
class ContainerTypeControllerTest {

    private static final String EMPTY_CONTAINER_TYPE = "";
    private static final String CONTAINER_TYPE_1 = "Container Type 1";
    private static final String CONTAINER_TYPE_2 = "Container Type 2";
    private static final String UPDATED_CONTAINER_TYPE = "Updated Container Type";
    private static final String DESCRIPTION_1 = "Description 1";
    private static final String DESCRIPTION_2 = "Description 2";
    private static final String UPDATED_DESCRIPTION = "Updated Description";
    private static final BigDecimal CAPACITY_1 = BigDecimal.valueOf(42.0);
    private static final BigDecimal CAPACITY_2 = BigDecimal.valueOf(33.0);
    private static final BigDecimal UPDATED_CAPACITY = BigDecimal.valueOf(999.0);
    private static final String CONTAINER_TYPE_NOT_FOUND_WITH_ID = "Container type not found with id: 2";
    private static final String CONTAINERS_STILL_HAVE_THIS_CONTAINER_TYPE_EXCEPTION = "Cannot delete container type with id 2 because there are existing containers of this type";
    private static final String NAME_IS_REQUIRED = "Name is required";
    private static final String CONTAINER_TYPE_WITH_NAME_EXIST_EXCEPTION = "Container Type with name already exists: Container Type 1";
    private static final String CONTAINERS_EXCEED_NEW_CAPACITY_EXCEPTION =
            "Cannot update container type capacity. There are containers with occupied quantity exceeding new capacity. "
                    + "Container Serial Numbers: CONT-001";
    private static final String ERROR_MESSAGE_PATH = "$.message";
    private static final String ERROR_STATUS_PATH = "$.status";
    private static final String ERROR_STATUS_NAME_PATH = "$.error";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ContainerTypeService containerTypeService;

    @Test
    void getAllContainerTypes_returnsOk() throws Exception {
        // Arrange
        List<ContainerTypeResponse> containerTypeResponseList = List.of(
                new ContainerTypeResponse(1L, CONTAINER_TYPE_1, DESCRIPTION_1, CAPACITY_1),
                new ContainerTypeResponse(2L, CONTAINER_TYPE_2, DESCRIPTION_2, CAPACITY_2)
        );
        when(containerTypeService.getAllContainerTypes()).thenReturn(containerTypeResponseList);

        // Act and Assert
        mockMvc.perform(get("/containerType/all"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(containerTypeResponseList)));
    }

    @Test
    void getContainerTypeById_returnsOk() throws Exception {
        // Arrange
        ContainerTypeResponse response = new ContainerTypeResponse(1L, CONTAINER_TYPE_1, DESCRIPTION_1, CAPACITY_1);
        when(containerTypeService.getContainerTypeById(1L)).thenReturn(response);

        // Act and Assert
        mockMvc.perform(get("/containerType/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void getContainerTypeById_storageLocationNotFound() throws Exception {
        // Arrange
        EntityNotFoundException exception = new EntityNotFoundException(CONTAINER_TYPE_NOT_FOUND_WITH_ID);
        when(containerTypeService.getContainerTypeById(2L)).thenThrow(exception);

        // Act and Assert
        mockMvc.perform(get("/containerType/2"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value(CONTAINER_TYPE_NOT_FOUND_WITH_ID))
                .andExpect(jsonPath(ERROR_STATUS_PATH).value(404))
                .andExpect(jsonPath(ERROR_STATUS_NAME_PATH).value("Not Found"));
    }

    @Test
    void createContainerType_returnsOk() throws Exception {
        // Arrange
        ContainerTypeRequest request = new ContainerTypeRequest(CONTAINER_TYPE_1, DESCRIPTION_1, CAPACITY_1);
        ContainerTypeResponse response = new ContainerTypeResponse(1L, CONTAINER_TYPE_1, DESCRIPTION_1, CAPACITY_1);
        when(containerTypeService.createContainerType(request)).thenReturn(response);

        // Act and Assert
        mockMvc.perform(post("/containerType")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void createContainerType_withBlankName_returnsBadRequest() throws Exception {
        // Arrange
        ContainerTypeRequest request = new ContainerTypeRequest(EMPTY_CONTAINER_TYPE, DESCRIPTION_1, CAPACITY_1);

        // Act and Assert
        mockMvc.perform(post("/containerType")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value(NAME_IS_REQUIRED))
                .andExpect(jsonPath(ERROR_STATUS_PATH).value(400))
                .andExpect(jsonPath(ERROR_STATUS_NAME_PATH).value("Bad Request"));
    }

    @Test
    void createContainerType_withNullName_returnsBadRequest() throws Exception {
        // Arrange
        ContainerTypeRequest request = new ContainerTypeRequest(null, DESCRIPTION_1, CAPACITY_1);

        // Act and Assert
        mockMvc.perform(post("/containerType")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value(NAME_IS_REQUIRED))
                .andExpect(jsonPath(ERROR_STATUS_PATH).value(400))
                .andExpect(jsonPath(ERROR_STATUS_NAME_PATH).value("Bad Request"));
    }

    @Test
    void createContainerType_nameAlreadyExist() throws Exception {
        // Arrange
        ContainerTypeRequest request = new ContainerTypeRequest(CONTAINER_TYPE_1, DESCRIPTION_1, CAPACITY_1);

        IllegalArgumentException exception = new IllegalArgumentException(CONTAINER_TYPE_WITH_NAME_EXIST_EXCEPTION);
        when(containerTypeService.createContainerType(request)).thenThrow(exception);

        // Act and Assert
        mockMvc.perform(post("/containerType")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value(CONTAINER_TYPE_WITH_NAME_EXIST_EXCEPTION))
                .andExpect(jsonPath(ERROR_STATUS_PATH).value(400))
                .andExpect(jsonPath(ERROR_STATUS_NAME_PATH).value("Bad Request"));
    }

    @Test
    void updateContainerType_returnsOk() throws Exception {
        // Arrange
        ContainerTypeRequest request = new ContainerTypeRequest(UPDATED_CONTAINER_TYPE, UPDATED_DESCRIPTION, UPDATED_CAPACITY);
        ContainerTypeResponse response =
                new ContainerTypeResponse(1L, UPDATED_CONTAINER_TYPE, UPDATED_DESCRIPTION, UPDATED_CAPACITY);
        when(containerTypeService.updateContainerType(eq(1L), any())).thenReturn(response);

        // Act and Assert
        mockMvc.perform(put("/containerType/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void updateStorageLocation_nameAlreadyExist() throws Exception {
        // Arrange
        ContainerTypeRequest request = new ContainerTypeRequest(CONTAINER_TYPE_1, DESCRIPTION_1, CAPACITY_1);

        IllegalArgumentException exception = new IllegalArgumentException(CONTAINER_TYPE_WITH_NAME_EXIST_EXCEPTION);
        when(containerTypeService.updateContainerType(1L, request)).thenThrow(exception);

        // Act and Assert
        mockMvc.perform(put("/containerType/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value(CONTAINER_TYPE_WITH_NAME_EXIST_EXCEPTION))
                .andExpect(jsonPath(ERROR_STATUS_PATH).value(400))
                .andExpect(jsonPath(ERROR_STATUS_NAME_PATH).value("Bad Request"));
    }

    @Test
    void updateStorageLocation_capacityIsExceededByContainers() throws Exception {
        // Arrange
        ContainerTypeRequest request = new ContainerTypeRequest(CONTAINER_TYPE_1, DESCRIPTION_1, CAPACITY_1);

        IllegalStateException exception = new IllegalStateException(CONTAINERS_EXCEED_NEW_CAPACITY_EXCEPTION);
        when(containerTypeService.updateContainerType(1L, request)).thenThrow(exception);

        // Act and Assert
        mockMvc.perform(put("/containerType/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value(CONTAINERS_EXCEED_NEW_CAPACITY_EXCEPTION))
                .andExpect(jsonPath(ERROR_STATUS_PATH).value(409))
                .andExpect(jsonPath(ERROR_STATUS_NAME_PATH).value("Conflict"));
    }

    @Test
    void removeContainerTypeById_returnsNoContent() throws Exception {
        // Arrange
        doNothing().when(containerTypeService).deleteContainerTypeById(1L);

        // Act and Assert
        mockMvc.perform(delete("/containerType/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeContainerTypeById_containerTypeNotFound() throws Exception {
        // Arrange
        EntityNotFoundException exception = new EntityNotFoundException(CONTAINER_TYPE_NOT_FOUND_WITH_ID);
        doThrow(exception).when(containerTypeService).deleteContainerTypeById(2L);

        // Act and Assert
        mockMvc.perform(delete("/containerType/2"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value(CONTAINER_TYPE_NOT_FOUND_WITH_ID))
                .andExpect(jsonPath(ERROR_STATUS_PATH).value(404))
                .andExpect(jsonPath(ERROR_STATUS_NAME_PATH).value("Not Found"));
    }

    @Test
    void removeContainerTypeById_containersHaveThisContainerType() throws Exception {
        // Arrange
        IllegalStateException exception = new IllegalStateException(CONTAINERS_STILL_HAVE_THIS_CONTAINER_TYPE_EXCEPTION);
        doThrow(exception).when(containerTypeService).deleteContainerTypeById(2L);

        // Act and Assert
        mockMvc.perform(delete("/containerType/2"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value(CONTAINERS_STILL_HAVE_THIS_CONTAINER_TYPE_EXCEPTION))
                .andExpect(jsonPath(ERROR_STATUS_PATH).value(409))
                .andExpect(jsonPath(ERROR_STATUS_NAME_PATH).value("Conflict"));
    }
}
