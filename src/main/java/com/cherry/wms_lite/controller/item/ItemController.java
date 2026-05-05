package com.cherry.wms_lite.controller.item;

import com.cherry.wms_lite.model.request.item.ItemRequest;
import com.cherry.wms_lite.model.response.item.ItemResponse;
import com.cherry.wms_lite.model.validation.OnCreate;
import com.cherry.wms_lite.model.validation.OnUpdate;
import com.cherry.wms_lite.service.item.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/item")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping("/all")
    public List<ItemResponse> getAllItems() {
        return itemService.getAllItems();
    }

    @GetMapping("/{id}")
    public ItemResponse getItemById(@PathVariable(name = "id") final Long itemId) {
        return itemService.getItemById(itemId);
    }

    @PostMapping
    public ItemResponse createItem(@Validated(OnCreate.class) @RequestBody final ItemRequest request) {
        return itemService.createItem(request);
    }

    @PutMapping("/{id}")
    public ItemResponse updateItem(@PathVariable(name = "id") final Long itemId,
                                   @Validated(OnUpdate.class) @RequestBody final ItemRequest request)
    {
        return itemService.updateItem(itemId, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeItemId(@PathVariable(name = "id") final Long itemId) {
        itemService.deleteItemById(itemId);
        return ResponseEntity.noContent().build();
    }
}
