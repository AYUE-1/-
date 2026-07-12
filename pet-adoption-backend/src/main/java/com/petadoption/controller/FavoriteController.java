package com.petadoption.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petadoption.entity.Favorite;
import com.petadoption.entity.Pet;
import com.petadoption.exception.Result;
import com.petadoption.mapper.FavoriteMapper;
import com.petadoption.mapper.PetMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteMapper favoriteMapper;
    private final PetMapper petMapper;

    @PostMapping("/{petId}")
    public Result<Map<String, Object>> toggle(@PathVariable Long petId, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        Favorite existing = favoriteMapper.selectOne(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getPetId, petId)
        );
        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            return Result.success(Map.of("favorited", false));
        } else {
            Favorite fav = new Favorite();
            fav.setUserId(userId);
            fav.setPetId(petId);
            favoriteMapper.insert(fav);
            return Result.success(Map.of("favorited", true));
        }
    }

    @GetMapping
    public Result<Map<String, Object>> myFavorites(Authentication auth,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "12") int size) {
        Long userId = (Long) auth.getPrincipal();
        Page<Favorite> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreatedAt);
        favoriteMapper.selectPage(pageObj, wrapper);

        List<Map<String, Object>> records = new ArrayList<>();
        for (Favorite fav : pageObj.getRecords()) {
            Pet pet = petMapper.selectById(fav.getPetId());
            if (pet != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", pet.getId());
                map.put("name", pet.getName());
                map.put("breed", pet.getBreed());
                map.put("age", pet.getAge());
                map.put("gender", pet.getGender());
                map.put("size", pet.getSize());
                map.put("coverImage", pet.getCoverImage());
                map.put("status", pet.getStatus());
                map.put("favoriteId", fav.getId());
                map.put("favoritedAt", fav.getCreatedAt());
                records.add(map);
            }
        }

        return Result.success(Map.of(
                "records", records,
                "total", pageObj.getTotal(),
                "page", pageObj.getCurrent(),
                "size", pageObj.getSize(),
                "pages", pageObj.getPages()
        ));
    }

    @GetMapping("/check/{petId}")
    public Result<Map<String, Object>> check(@PathVariable Long petId, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        Long count = favoriteMapper.selectCount(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getPetId, petId)
        );
        return Result.success(Map.of("favorited", count > 0));
    }
}
