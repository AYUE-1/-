package com.petadoption.controller;

import com.petadoption.exception.Result;
import com.petadoption.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        return Result.success(fileService.uploadFile(file));
    }

    @PostMapping("/upload/batch")
    public Result<List<String>> uploadBatch(@RequestParam("files") List<MultipartFile> files) {
        return Result.success(fileService.uploadFiles(files));
    }

    @PostMapping("/pet-image")
    public Result<Void> addPetImage(@RequestBody Map<String, Object> body) {
        Long petId = Long.valueOf(body.get("petId").toString());
        String url = (String) body.get("url");
        int sortOrder = body.get("sortOrder") != null ? Integer.parseInt(body.get("sortOrder").toString()) : 0;
        fileService.addPetImage(petId, url, sortOrder);
        return Result.success();
    }

    @DeleteMapping("/pet-image/{imageId}")
    public Result<Void> deletePetImage(@PathVariable Long imageId) {
        fileService.deletePetImage(imageId);
        return Result.success();
    }
}
