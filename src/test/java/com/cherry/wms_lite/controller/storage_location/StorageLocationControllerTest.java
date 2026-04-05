package com.cherry.wms_lite.controller.storage_location;

import com.cherry.wms_lite.WmsLiteApplication;
import com.cherry.wms_lite.model.request.storage_location.StorageLocationRequest;
import com.cherry.wms_lite.model.response.storage_location.StorageLocationResponse;
import com.cherry.wms_lite.service.storage_location.StorageLocationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(StorageLocationController.class)
@ContextConfiguration(classes = WmsLiteApplication.class)
class StorageLocationControllerTest {

    private static final String STORAGE_LOCATION_1 = "Storage Location 1";
    private static final String STORAGE_LOCATION_2 = "Storage Location 2";
    private static final String EMPTY_STORAGE_LOCATION = "";
    private static final String UPDATED_STORAGE_LOCATION = "Updated Location";
    private static final String DESCRIPTION_1 = "Description 1";
    private static final String DESCRIPTION_2 = "Description 2";
    private static final String UPDATED_DESCRIPTION = "Updated Description";
    private static final String STORAGE_LOCATION_NOT_FOUND_WITH_ID = "Storage Location not found with id: 2";
    private static final String NAME_IS_REQUIRED = "Name is required";
    private static final String STORAGE_LOCATION_WITH_NAME_EXIST_EXCEPTION = "Storage Location with name already exists: Storage Location 1";
    private static final String ERROR_MESSAGE_PATH = "$.message";
    private static final String ERROR_STATUS_PATH = "$.status";
    private static final String ERROR_STATUS_NAME_PATH = "$.error";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StorageLocationService storageLocationService;

    @Test
    void getAllStorageLocations_returnsOk() throws Exception {
        // Arrange
        List<StorageLocationResponse> storageLocationResponseList = List.of(
                new StorageLocationResponse(1L, STORAGE_LOCATION_1, DESCRIPTION_1),
                new StorageLocationResponse(2L, STORAGE_LOCATION_2, DESCRIPTION_2)
        );
        when(storageLocationService.getAllStorageLocations()).thenReturn(storageLocationResponseList);

        // Act and Assert
        mockMvc.perform(get("/storageLocation/all"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(storageLocationResponseList)));
    }

    @Test
    void getStorageLocationById_returnsOk() throws Exception {
        // Arrange
        StorageLocationResponse response = new StorageLocationResponse(1L, STORAGE_LOCATION_1, DESCRIPTION_1);
        when(storageLocationService.getStorageLocationById(1L)).thenReturn(response);

        // Act and Assert
        mockMvc.perform(get("/storageLocation/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void getStorageLocationById_storageLocationNotFound() throws Exception {
        // Arrange
        EntityNotFoundException exception = new EntityNotFoundException(STORAGE_LOCATION_NOT_FOUND_WITH_ID);
        when(storageLocationService.getStorageLocationById(2L)).thenThrow(exception);

        // Act and Assert
        mockMvc.perform(get("/storageLocation/2"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value(STORAGE_LOCATION_NOT_FOUND_WITH_ID))
                .andExpect(jsonPath(ERROR_STATUS_PATH).value(404))
                .andExpect(jsonPath(ERROR_STATUS_NAME_PATH).value("Not Found"));
    }

    @Test
    void createStorageLocation_returnsOk() throws Exception {
        // Arrange
        StorageLocationRequest request = new StorageLocationRequest(STORAGE_LOCATION_1, DESCRIPTION_1);
        StorageLocationResponse response = new StorageLocationResponse(1L, STORAGE_LOCATION_1, DESCRIPTION_1);
        when(storageLocationService.createStorageLocation(request)).thenReturn(response);

        // Act and Assert
        mockMvc.perform(post("/storageLocation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void createStorageLocation_withBlankName_returnsBadRequest() throws Exception {
        // Arrange
        StorageLocationRequest request = new StorageLocationRequest(EMPTY_STORAGE_LOCATION, DESCRIPTION_1);

        // Act and Assert
        mockMvc.perform(post("/storageLocation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value(NAME_IS_REQUIRED))
                .andExpect(jsonPath(ERROR_STATUS_PATH).value(400))
                .andExpect(jsonPath(ERROR_STATUS_NAME_PATH).value("Bad Request"));
    }

    @Test
    void createStorageLocation_withNullName_returnsBadRequest() throws Exception {
        // Arrange
        StorageLocationRequest request = new StorageLocationRequest(null, DESCRIPTION_1);

        // Act and Assert
        mockMvc.perform(post("/storageLocation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value(NAME_IS_REQUIRED))
                .andExpect(jsonPath(ERROR_STATUS_PATH).value(400))
                .andExpect(jsonPath(ERROR_STATUS_NAME_PATH).value("Bad Request"));
    }

    @Test
    void createStorageLocation_nameAlreadyExist() throws Exception {
        // Arrange
        StorageLocationRequest request = new StorageLocationRequest(STORAGE_LOCATION_1, DESCRIPTION_1);

        IllegalArgumentException exception = new IllegalArgumentException(STORAGE_LOCATION_WITH_NAME_EXIST_EXCEPTION);
        when(storageLocationService.createStorageLocation(request)).thenThrow(exception);

        // Act and Assert
        mockMvc.perform(post("/storageLocation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value(STORAGE_LOCATION_WITH_NAME_EXIST_EXCEPTION))
                .andExpect(jsonPath(ERROR_STATUS_PATH).value(400))
                .andExpect(jsonPath(ERROR_STATUS_NAME_PATH).value("Bad Request"));
    }

    @Test
    void updateStorageLocation_returnsOk() throws Exception {
        // Arrange
        StorageLocationRequest request = new StorageLocationRequest(UPDATED_STORAGE_LOCATION, UPDATED_DESCRIPTION);
        StorageLocationResponse response =
                new StorageLocationResponse(1L, UPDATED_STORAGE_LOCATION, UPDATED_DESCRIPTION);
        when(storageLocationService.updateStorageLocation(eq(1L), any())).thenReturn(response);

        // Act and Assert
        mockMvc.perform(put("/storageLocation/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void updateStorageLocation_nameAlreadyExist() throws Exception {
        // Arrange
        StorageLocationRequest request = new StorageLocationRequest(STORAGE_LOCATION_1, DESCRIPTION_1);

        IllegalArgumentException exception = new IllegalArgumentException(STORAGE_LOCATION_WITH_NAME_EXIST_EXCEPTION);
        when(storageLocationService.updateStorageLocation(1L, request)).thenThrow(exception);

        // Act and Assert
        mockMvc.perform(put("/storageLocation/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value(STORAGE_LOCATION_WITH_NAME_EXIST_EXCEPTION))
                .andExpect(jsonPath(ERROR_STATUS_PATH).value(400))
                .andExpect(jsonPath(ERROR_STATUS_NAME_PATH).value("Bad Request"));
    }

    @Test
    void removeStorageLocationById_returnsNoContent() throws Exception {
        // Arrange
        doNothing().when(storageLocationService).deleteStorageLocationById(1L);

        // Act and Assert
        mockMvc.perform(delete("/storageLocation/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeStorageLocationById_storageLocationNotFound() throws Exception {
        // Arrange
        EntityNotFoundException exception = new EntityNotFoundException(STORAGE_LOCATION_NOT_FOUND_WITH_ID);
        doThrow(exception).when(storageLocationService).deleteStorageLocationById(2L);

        // Act and Assert
        mockMvc.perform(delete("/storageLocation/2"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value(STORAGE_LOCATION_NOT_FOUND_WITH_ID))
                .andExpect(jsonPath(ERROR_STATUS_PATH).value(404))
                .andExpect(jsonPath(ERROR_STATUS_NAME_PATH).value("Not Found"));
    }
}
