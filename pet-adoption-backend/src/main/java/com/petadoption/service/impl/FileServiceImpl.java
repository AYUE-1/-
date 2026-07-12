package com.petadoption.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petadoption.entity.PetImage;
import com.petadoption.exception.BusinessException;
import com.petadoption.mapper.PetImageMapper;
import com.petadoption.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    @Value("${file.upload.dir}")
    private String uploadDir;

    @Value("${file.upload.max-size}")
    private long maxSize;

    private final PetImageMapper petImageMapper;

    @Override
    public String uploadFile(MultipartFile file) {
        validateFile(file);
        try {
            // 解析上传目录：如果是相对路径，则相对于当前工作目录
            Path baseDir = Paths.get(uploadDir);
            if (!baseDir.isAbsolute()) {
                baseDir = Paths.get(System.getProperty("user.dir")).resolve(uploadDir);
            }
            // 按日期分目录
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path uploadPath = baseDir.resolve(dateDir);
            Files.createDirectories(uploadPath);

            // 生成唯一文件名
            String originalName = file.getOriginalFilename();
            String extension = originalName != null && originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : "";
            String fileName = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = uploadPath.resolve(fileName);
            file.transferTo(filePath.toFile());

            return "/files/" + dateDir + "/" + fileName;
        } catch (IOException e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public List<String> uploadFiles(List<MultipartFile> files) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(uploadFile(file));
        }
        return urls;
    }

    @Override
    public void addPetImage(Long petId, String url, int sortOrder) {
        PetImage image = new PetImage();
        image.setPetId(petId);
        image.setUrl(url);
        image.setSortOrder(sortOrder);
        petImageMapper.insert(image);
    }

    @Override
    public void deletePetImage(Long imageId) {
        petImageMapper.deleteById(imageId);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件为空");
        }
        if (file.getSize() > maxSize) {
            throw new BusinessException("文件大小超过限制(" + maxSize / 1024 / 1024 + "MB)");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("只支持图片格式");
        }
    }
}
