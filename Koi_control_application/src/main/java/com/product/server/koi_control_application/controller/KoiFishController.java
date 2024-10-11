package com.product.server.koi_control_application.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.product.server.koi_control_application.model.KoiFish;
import com.product.server.koi_control_application.model.KoiGrowthHistory;
import com.product.server.koi_control_application.pojo.KoiFishDTO;
import com.product.server.koi_control_application.pojo.KoiFishUpdateRequest;
import com.product.server.koi_control_application.pojo.response.BaseResponse;
import com.product.server.koi_control_application.repository.KoiFishRepository;
import com.product.server.koi_control_application.service_interface.IImageService;
import com.product.server.koi_control_application.service_interface.IKoiFishService;
import com.product.server.koi_control_application.ultil.ResponseUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/koifishs")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MEMBER', 'ROLE_SHOP')")
@Tag(name = "KoiFish", description = "API for KoiFish")
public class KoiFishController {

    private final IKoiFishService iKoiFishService;
    private final IImageService iImageService;
    private final KoiFishRepository koiFishRepository;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse> createKoi(
            @Schema(type = "string", format = "json", implementation = KoiFishDTO.class)
            @RequestPart("fish") String koiFishJson,
            @RequestParam(value = "image", required = false) MultipartFile file) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        @Valid KoiFish koiFish = mapper.readValue(koiFishJson, KoiFish.class);


        if (file != null && !file.isEmpty()) {
            String filename = iImageService.uploadImage(file);
            koiFish.setImageUrl(filename);
        }

        if (koiFish.getWeight() == null || koiFish.getLength() == null)
            throw new IllegalArgumentException("Weight and Length must not be null");
        if (koiFish.getWeight().compareTo(BigDecimal.valueOf(0.00)) <= 0 || koiFish.getLength().compareTo(BigDecimal.valueOf(0.00)) <= 0)
            throw new IllegalArgumentException("Weight and Length must be greater than 0");

        KoiFish koiFish1 = iKoiFishService.addKoiFish(koiFish);

        BaseResponse response = BaseResponse.builder()
                .data(koiFish1)
                .message("Create fish successfully")
                .statusCode(HttpStatus.CREATED.value())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("{koiFishId}")
    public ResponseEntity<BaseResponse> getKoi(@PathVariable("koiFishId") int koiFishId) {
        KoiFish koiFish1 = iKoiFishService.getKoiFishsaved(koiFishId);
        BaseResponse response = BaseResponse.builder()
                .data(koiFish1)
                .message("Get fish  successfully")
                .statusCode(HttpStatus.OK.value())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PutMapping(value = "/{koiFishId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse> updateKoiFish(@PathVariable("koiFishId") int koiFishId,
                                                      @Schema(type = "string", format = "json", implementation = KoiFishUpdateRequest.class)
                                                      @RequestPart("fish") @Valid String koiFishJson, @RequestParam(value = "image", required = false) MultipartFile file) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        @Valid KoiFish koiFish = mapper.readValue(koiFishJson, KoiFish.class);
        JsonNode rootNode = mapper.readTree(koiFishJson);
        Boolean isNew = rootNode.path("isNew").asBoolean();
        BaseResponse response = BaseResponse.builder()
                .data(iKoiFishService.updateKoiFish(koiFishId, koiFish, file, isNew))
                .message("Update fish successfully")
                .statusCode(HttpStatus.OK.value())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("{koiFishId}")
    public ResponseEntity<BaseResponse> deleteKoiFish(@PathVariable("koiFishId") int koiFishId) {
        KoiFish koiFish1 = iKoiFishService.getKoiFishsaved(koiFishId);
        iKoiFishService.deleteKoiFish(koiFishId);

        BaseResponse response = BaseResponse.builder()
                .data(koiFish1)
                .message("Delete fish successfully")
                .statusCode(HttpStatus.OK.value())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/listkoi/bypondid/{pondId}/page")
    public ResponseEntity<BaseResponse> getKoisByPondId(@PathVariable("pondId") int pondId, @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        Page<KoiFish> koiFishs = iKoiFishService.getKoiFishsByPondId(pondId, page, size);
        String mess = "Get koifishs by pondId succesfully";
        if (koiFishs.isEmpty())
            mess = "List is empty";
        BaseResponse response = BaseResponse.builder()
                .data(koiFishs)
                .message(mess)
                .statusCode(HttpStatus.OK.value())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/listkoi/bypondid/{pondId}")
    public ResponseEntity<BaseResponse> getKoisByPondId(@PathVariable("pondId") int pondId) {
        List<KoiFish> koiFishs = iKoiFishService.getKoiFishsByPondId(pondId);
        for (KoiFish koiFish : koiFishs) {
            koiFish.countageMonth();
            koiFishRepository.save(koiFish);
        }
        String mess = "Get koifishs by pondId succesfully";
        if (koiFishs.isEmpty())
            mess = "List is empty";
        return ResponseUtil.createSuccessResponse(koiFishs, mess);
    }

//    @GetMapping("/withoutPond/byUserId/{userId}")
//    public ResponseEntity<BaseResponse> getKoiNoPondByUserId(@PathVariable("userId") int userId) {
//        List<KoiFish> koiFishs = iKoiFishService.getFishByUserNoPond(userId);
//        for (KoiFish koiFish : koiFishs) {
//            koiFish.countageMonth();
//            koiFishRepository.save(koiFish);
//        }
//        String mess = "Get koifishs by pondId succesfully";
//        if (koiFishs.isEmpty())
//            mess = "List is empty";
//        return ResponseUtil.createSuccessResponse(koiFishs, mess);
//    }

    @GetMapping("/listkoi/byuserid/{userId}/page")
    public ResponseEntity<BaseResponse> getKoiByUserIdPage(@PathVariable("userId") int userId, @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        Page<KoiFish> koiFishs = iKoiFishService.getKoiFishsByUserId(userId, page, size);

        String mess = "Get fish by userId successfully";
        if (koiFishs.isEmpty())
            mess = "List is empty";
        return ResponseUtil.createSuccessResponse(koiFishs, mess);
    }

    @GetMapping("/listkoi/byuserid/{userId}")
    public ResponseEntity<BaseResponse> getKoisByUserId(@PathVariable("userId") int userId) {
        List<KoiFish> koiFishs = iKoiFishService.getKoiFishsByUserId(userId);
        String mess = "Get fish by userId successfully";
        if (koiFishs.isEmpty())
            mess = "List is empty";

        return ResponseUtil.createSuccessResponse(koiFishs, mess);
    }


//    @GetMapping("/listkoi")
//    public ResponseEntity<BaseResponse> getKois() {
//        Page<KoiFish> koiFishs = iKoiFishService.getKoiFishs(0, 10);
//        for (KoiFish koiFish : koiFishs) {
//            koiFish.countageMonth();
//            koiFishRepository.save(koiFish);
//        }
//        String mess = "Get all koifish succesfully";
//        if (koiFishs.isEmpty())
//            mess = "List is empty";
//
//        return ResponseUtil.createSuccessResponse(koiFishs, mess);
//    }
    //========================================

    @GetMapping("/growthUpHistory/{koiFishId}/page")
    public ResponseEntity<BaseResponse> getGrowthHistory(@PathVariable("koiFishId") int koiFishId, @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        Page<KoiGrowthHistory> koiGrowthHistorys = iKoiFishService.getGrowthHistorys(koiFishId, page, size);
        String mess = "Get growth history successfully";
        if (koiGrowthHistorys.isEmpty())
            mess = "List is empty";

        return ResponseUtil.createSuccessResponse(koiGrowthHistorys, mess);

    }

    @GetMapping("/growthUpHistory/{koiFishId}")
    public ResponseEntity<BaseResponse> getGrowthHistory(@PathVariable("koiFishId") int koiFishId) {
        List<KoiGrowthHistory> koGrowthHistory = iKoiFishService.getGrowthHistorys(koiFishId);
        String mess = "Get growth history successfully";
        if (koGrowthHistory.isEmpty())
            mess = "List is empty";

        return ResponseUtil.createSuccessResponse(koGrowthHistory, mess);

    }

    @PostMapping(value = "/growthUpHistory/{koiFishId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse> addGrowthHistory(@PathVariable("koiFishId") int koiFishId,
                                                         @Schema(type = "string", format = "json", implementation = KoiFishDTO.class)
                                                         @RequestPart("fish") @Valid String koiFishJson, @RequestParam(value = "image", required = false) MultipartFile file) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        @Valid KoiFish koiFish = mapper.readValue(koiFishJson, KoiFish.class);

        BaseResponse response = BaseResponse.builder()
                .data(iKoiFishService.addGrowthHistory(koiFishId, koiFish, file))
                .message("Update fish successfully")
                .statusCode(HttpStatus.OK.value())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
