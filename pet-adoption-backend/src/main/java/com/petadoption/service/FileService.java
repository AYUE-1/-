package com.petadoption.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface FileService {
    String uploadFile(MultipartFile file);
    List<String> uploadFiles(List<MultipartFile> files);
    void addPetImage(Long petId, String url, int sortOrder);
    void deletePetImage(Long imageId);
}
