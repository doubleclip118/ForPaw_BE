package com.hong.ForPaw.controller;

import com.hong.ForPaw.controller.DTO.ShelterResponse;
import com.hong.ForPaw.core.security.CustomUserDetails;
import com.hong.ForPaw.core.utils.ApiUtils;
import com.hong.ForPaw.service.ShelterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ShelterController {

    private final ShelterService shelterService;

    @GetMapping("/shelters/import")
    public ResponseEntity<?> loadShelter(@AuthenticationPrincipal CustomUserDetails userDetails) {
        shelterService.loadShelterData();
        return ResponseEntity.ok().body(ApiUtils.success(HttpStatus.OK, null));
    }

    @GetMapping("/shelters")
    public ResponseEntity<?> findShelterList(@RequestParam("lat") Double lat, @RequestParam("lng") Double lng){
        ShelterResponse.FindShelterListDTO responseDTO = shelterService.findShelterList(lat, lng);
        return ResponseEntity.ok().body(ApiUtils.success(HttpStatus.CREATED, responseDTO));
    }

    @GetMapping("/shelters/{shelterId}")
    public ResponseEntity<?> findShelterById(@PathVariable Long shelterId, @RequestParam("page") Integer page, @RequestParam(value = "sort", defaultValue = "noticeSdt") String sort, @AuthenticationPrincipal CustomUserDetails userDetails){
        ShelterResponse.FindShelterByIdDTO responseDTO = shelterService.findShelterById(shelterId, userDetails.getUser().getId(), page, sort);
        return ResponseEntity.ok().body(ApiUtils.success(HttpStatus.OK, responseDTO));
    }

    @DeleteMapping("/shelters")
    public ResponseEntity<?> deleteZeroShelter(@AuthenticationPrincipal CustomUserDetails userDetails){
        shelterService.deleteZeroShelter(userDetails.getUser().getRole());
        return ResponseEntity.ok().body(ApiUtils.success(HttpStatus.OK, null));
    }
}