package com.example.demo.controller;

import com.example.demo.canva.model.*;
import com.example.demo.service.CanvaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/canva")
public class CanvaController {

    private final CanvaService canvaService;

    public CanvaController(CanvaService canvaService) {
        this.canvaService = canvaService;
    }

    /**
     * GET /api/canva/user/profile
     * Get the current user's profile
     */
    @GetMapping("/user/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile() {
        UserProfileResponse profile = canvaService.getUserProfile();
        return ResponseEntity.ok(profile);
    }

    /**
     * GET /api/canva/user/capabilities
     * Get the current user's capabilities
     */
    @GetMapping("/user/capabilities")
    public ResponseEntity<GetUserCapabilitiesResponse> getUserCapabilities() {
        GetUserCapabilitiesResponse capabilities = canvaService.getUserCapabilities();
        return ResponseEntity.ok(capabilities);
    }

    /**
     * GET /api/canva/designs
     * List all designs for the user
     */
    @GetMapping("/designs")
    public ResponseEntity<GetListDesignResponse> listDesigns(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String continuation,
            @RequestParam(required = false) OwnershipType ownership,
            @RequestParam(required = false) SortByType sortBy) {
        GetListDesignResponse designs = canvaService.listDesigns(query, continuation, ownership, sortBy);
        return ResponseEntity.ok(designs);
    }

    /**
     * GET /api/canva/designs/{designId}
     * Get a specific design
     */
    @GetMapping("/designs/{designId}")
    public ResponseEntity<GetDesignResponse> getDesign(@PathVariable String designId) {
        GetDesignResponse design = canvaService.getDesign(designId);
        return ResponseEntity.ok(design);
    }

    /**
     * POST /api/canva/designs
     * Create a new design
     */
    @PostMapping("/designs")
    public ResponseEntity<CreateDesignResponse> createDesign(@RequestBody CreateDesignRequest request) {
        CreateDesignResponse design = canvaService.createDesign(request);
        return ResponseEntity.ok(design);
    }

    /**
     * GET /api/canva/designs/{designId}/pages
     * Get pages of a design
     */
    @GetMapping("/designs/{designId}/pages")
    public ResponseEntity<GetDesignPagesResponse> getDesignPages(
            @PathVariable String designId,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit) {
        GetDesignPagesResponse pages = canvaService.getDesignPages(designId, offset, limit);
        return ResponseEntity.ok(pages);
    }

    /**
     * GET /api/canva/folders/{folderId}/items
     * List items in a folder
     */
    @GetMapping("/folders/{folderId}/items")
    public ResponseEntity<ListFolderItemsResponse> listFolderItems(
            @PathVariable String folderId,
            @RequestParam(required = false) String continuation,
            @RequestParam(required = false) List<FolderItemType> itemTypes,
            @RequestParam(required = false) FolderItemSortBy sortBy) {
        ListFolderItemsResponse items = canvaService.listFolderItems(folderId, continuation, itemTypes, sortBy);
        return ResponseEntity.ok(items);
    }

    /**
     * GET /api/canva/assets/{assetId}
     * Get asset information
     */
    @GetMapping("/assets/{assetId}")
    public ResponseEntity<GetAssetResponse> getAsset(@PathVariable String assetId) {
        GetAssetResponse asset = canvaService.getAsset(assetId);
        return ResponseEntity.ok(asset);
    }
}
