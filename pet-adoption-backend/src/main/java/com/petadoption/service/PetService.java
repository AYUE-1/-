package com.petadoption.service;

import com.petadoption.dto.PageResult;
import com.petadoption.dto.PetQueryDTO;
import com.petadoption.entity.Pet;
import com.petadoption.vo.PetDetailVO;
import com.petadoption.vo.PetVO;

public interface PetService {
    PageResult<PetVO> queryPets(PetQueryDTO dto);
    PetDetailVO getDetail(Long id);
    Pet getById(Long id);
    PetVO create(Pet pet);
    PetVO update(Pet pet, Long userId, boolean isAdmin);
    void delete(Long id, Long userId, boolean isAdmin);
    void updateStatus(Long id, String status);
    PageResult<PetVO> getUserPets(Long userId, int page, int size);
}
