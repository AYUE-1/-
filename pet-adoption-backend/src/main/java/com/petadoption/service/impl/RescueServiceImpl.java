package com.petadoption.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petadoption.dto.RescuePostDTO;
import com.petadoption.dto.RescueQueryDTO;
import com.petadoption.entity.*;
import com.petadoption.enums.RescueStatusEnum;
import com.petadoption.exception.BusinessException;
import com.petadoption.mapper.*;
import com.petadoption.service.RescueService;
import com.petadoption.vo.RescueDetailVO;
import com.petadoption.vo.RescuePostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RescueServiceImpl implements RescueService {

    private final RescuePostMapper rescueMapper;
    private final RescueImageMapper imageMapper;
    private final UserMapper userMapper;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public RescuePostVO create(Long userId, RescuePostDTO dto) {
        RescuePost post = new RescuePost();
        post.setUserId(userId);
        post.setTitle(dto.getTitle());
        post.setDescription(dto.getDescription());
        post.setLatitude(dto.getLatitude());
        post.setLongitude(dto.getLongitude());
        post.setAddressDesc(dto.getAddressDesc());
        post.setAnimalType(dto.getAnimalType());
        post.setAnimalDesc(dto.getAnimalDesc());
        post.setEmergencyLevel(dto.getEmergencyLevel() != null ? dto.getEmergencyLevel() : "NORMAL");
        post.setStatus("PENDING");
        post.setViewCount(0);
        rescueMapper.insert(post);

        // 保存图片
        if (dto.getImages() != null) {
            for (int i = 0; i < dto.getImages().size(); i++) {
                RescueImage img = new RescueImage();
                img.setRescueId(post.getId());
                img.setUrl(dto.getImages().get(i));
                img.setSortOrder(i);
                imageMapper.insert(img);
            }
        }

        // 附近用户推送通知
        notifyNearbyUsers(post);

        return toRescuePostVO(post);
    }

    private void notifyNearbyUsers(RescuePost post) {
        // 简化版：通知所有用户有新救助信息
        // 生产环境应根据经纬度查询附近用户
        List<User> users = userMapper.selectList(
            new LambdaQueryWrapper<User>().eq(User::getStatus, 1).last("LIMIT 100")
        );
        for (User user : users) {
            if (user.getId().equals(post.getUserId())) continue;
            Notification notif = new Notification();
            notif.setUserId(user.getId());
            notif.setTitle("附近有新的流浪动物求助");
            notif.setContent(post.getTitle() + " - " + post.getAddressDesc());
            notif.setType("RESCUE_NEARBY");
            notif.setRelatedId(post.getId());
            notif.setIsRead(0);
            notificationMapper.insert(notif);
        }
    }

    @Override
    public RescueDetailVO getDetail(Long id) {
        RescuePost post = rescueMapper.selectById(id);
        if (post == null) throw new BusinessException("求助信息不存在");
        // 增加浏览量
        post.setViewCount((post.getViewCount() != null ? post.getViewCount() : 0) + 1);
        rescueMapper.updateById(post);
        return toRescueDetailVO(post);
    }

    @Override
    public List<RescuePostVO> queryList(RescueQueryDTO dto) {
        LambdaQueryWrapper<RescuePost> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getAnimalType())) wrapper.eq(RescuePost::getAnimalType, dto.getAnimalType());
        if (StringUtils.hasText(dto.getEmergencyLevel())) wrapper.eq(RescuePost::getEmergencyLevel, dto.getEmergencyLevel());
        if (StringUtils.hasText(dto.getStatus())) wrapper.eq(RescuePost::getStatus, dto.getStatus());
        wrapper.orderByDesc(RescuePost::getCreatedAt);

        Page<RescuePost> page = new Page<>(dto.getPage(), dto.getSize());
        rescueMapper.selectPage(page, wrapper);

        List<RescuePostVO> vos = page.getRecords().stream()
            .map(this::toRescuePostVO)
            .collect(Collectors.toList());

        // 如果有经纬度，计算距离并排序
        if (dto.getLatitude() != null && dto.getLongitude() != null) {
            for (RescuePostVO vo : vos) {
                vo.setDistance(haversine(dto.getLatitude(), dto.getLongitude(), vo.getLatitude(), vo.getLongitude()));
            }
            if ("distance".equals(dto.getSortBy())) {
                vos.sort(Comparator.comparing(RescuePostVO::getDistance));
            }
            // 按半径筛选
            if (dto.getRadius() != null && dto.getRadius() > 0) {
                vos = vos.stream().filter(v -> v.getDistance() <= dto.getRadius()).collect(Collectors.toList());
            }
        }

        return vos;
    }

    @Override
    @Transactional
    public RescuePostVO update(Long id, Long userId, RescuePostDTO dto) {
        RescuePost post = rescueMapper.selectById(id);
        if (post == null) throw new BusinessException("求助信息不存在");
        if (!post.getUserId().equals(userId)) throw new BusinessException("只能修改自己的求助信息");
        if (!"PENDING".equals(post.getStatus())) throw new BusinessException("只能修改待救助状态的帖子");

        post.setTitle(dto.getTitle());
        post.setDescription(dto.getDescription());
        post.setLatitude(dto.getLatitude());
        post.setLongitude(dto.getLongitude());
        post.setAddressDesc(dto.getAddressDesc());
        post.setAnimalType(dto.getAnimalType());
        post.setAnimalDesc(dto.getAnimalDesc());
        post.setEmergencyLevel(dto.getEmergencyLevel());
        rescueMapper.updateById(post);

        // 更新图片
        imageMapper.delete(new LambdaQueryWrapper<RescueImage>().eq(RescueImage::getRescueId, id));
        if (dto.getImages() != null) {
            for (int i = 0; i < dto.getImages().size(); i++) {
                RescueImage img = new RescueImage();
                img.setRescueId(post.getId());
                img.setUrl(dto.getImages().get(i));
                img.setSortOrder(i);
                imageMapper.insert(img);
            }
        }
        return toRescuePostVO(post);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        RescuePost post = rescueMapper.selectById(id);
        if (post == null) throw new BusinessException("求助信息不存在");
        if (!post.getUserId().equals(userId)) throw new BusinessException("只能删除自己的求助信息");
        imageMapper.delete(new LambdaQueryWrapper<RescueImage>().eq(RescueImage::getRescueId, id));
        rescueMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void claim(Long id, Long userId) {
        RescuePost post = rescueMapper.selectById(id);
        if (post == null) throw new BusinessException("求助信息不存在");
        if (!"PENDING".equals(post.getStatus())) throw new BusinessException("该求助已被认领或已处理");
        if (post.getUserId().equals(userId)) throw new BusinessException("不能认领自己发布的求助");

        post.setStatus("PROCESSING");
        post.setClaimedBy(userId);
        post.setClaimedAt(LocalDateTime.now());
        rescueMapper.updateById(post);

        // 通知发布者
        User claimer = userMapper.selectById(userId);
        Notification notif = new Notification();
        notif.setUserId(post.getUserId());
        notif.setTitle("您的求助已被认领");
        notif.setContent(claimer != null ? claimer.getNickname() + " 已认领您的求助" : "有志愿者认领了您的求助");
        notif.setType("RESCUE_NEARBY");
        notif.setRelatedId(id);
        notif.setIsRead(0);
        notificationMapper.insert(notif);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Long userId, String status) {
        RescuePost post = rescueMapper.selectById(id);
        if (post == null) throw new BusinessException("求助信息不存在");
        if (!RescueStatusEnum.isValidTransition(post.getStatus(), status))
            throw new BusinessException("无效的状态变更");

        boolean isOwner = post.getUserId().equals(userId);
        boolean isClaimer = post.getClaimedBy() != null && post.getClaimedBy().equals(userId);
        if (!isOwner && !isClaimer) throw new BusinessException("无权操作");

        post.setStatus(status);
        if ("RESCUED".equals(status)) post.setRescuedAt(LocalDateTime.now());
        rescueMapper.updateById(post);
    }

    @Override
    @Transactional
    public void addFollowUp(Long id, Long userId, String followUpText) {
        RescuePost post = rescueMapper.selectById(id);
        if (post == null) throw new BusinessException("求助信息不存在");
        boolean isOwner = post.getUserId().equals(userId);
        boolean isClaimer = post.getClaimedBy() != null && post.getClaimedBy().equals(userId);
        if (!isOwner && !isClaimer) throw new BusinessException("无权操作");
        post.setFollowUpText(followUpText);
        rescueMapper.updateById(post);
    }

    @Override
    public List<RescuePostVO> getMyRescues(Long userId) {
        List<RescuePost> posts = rescueMapper.selectList(
            new LambdaQueryWrapper<RescuePost>().eq(RescuePost::getUserId, userId)
                .orderByDesc(RescuePost::getCreatedAt)
        );
        return posts.stream().map(this::toRescuePostVO).collect(Collectors.toList());
    }

    @Override
    public List<RescuePostVO> getMyClaims(Long userId) {
        List<RescuePost> posts = rescueMapper.selectList(
            new LambdaQueryWrapper<RescuePost>().eq(RescuePost::getClaimedBy, userId)
                .orderByDesc(RescuePost::getClaimedAt)
        );
        return posts.stream().map(this::toRescuePostVO).collect(Collectors.toList());
    }

    // ===== Haversine 距离计算 =====
    private double haversine(double lat1, double lon1, Double lat2, Double lon2) {
        if (lat2 == null || lon2 == null) return Double.MAX_VALUE;
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(R * c * 10.0) / 10.0;
    }

    // ===== VO 转换 =====
    private RescuePostVO toRescuePostVO(RescuePost post) {
        RescuePostVO vo = new RescuePostVO();
        copyFields(post, vo);
        return vo;
    }

    private RescueDetailVO toRescueDetailVO(RescuePost post) {
        RescueDetailVO vo = new RescueDetailVO();
        copyFields(post, vo);
        User user = userMapper.selectById(post.getUserId());
        if (user != null) vo.setContactPhone(user.getPhone());
        return vo;
    }

    private void copyFields(RescuePost post, RescuePostVO vo) {
        vo.setId(post.getId());
        vo.setUserId(post.getUserId());
        vo.setTitle(post.getTitle());
        vo.setDescription(post.getDescription());
        vo.setLatitude(post.getLatitude());
        vo.setLongitude(post.getLongitude());
        vo.setAddressDesc(post.getAddressDesc());
        vo.setAnimalType(post.getAnimalType());
        vo.setAnimalDesc(post.getAnimalDesc());
        vo.setEmergencyLevel(post.getEmergencyLevel());
        vo.setStatus(post.getStatus());
        vo.setStatusDesc(RescueStatusEnum.getDesc(post.getStatus()));
        vo.setClaimedBy(post.getClaimedBy());
        vo.setClaimedAt(post.getClaimedAt());
        vo.setRescuedAt(post.getRescuedAt());
        vo.setFollowUpText(post.getFollowUpText());
        vo.setViewCount(post.getViewCount());
        vo.setCreatedAt(post.getCreatedAt());

        // 用户信息
        User user = userMapper.selectById(post.getUserId());
        if (user != null) {
            vo.setUsername(user.getNickname() != null ? user.getNickname() : user.getUsername());
            vo.setUserAvatar(user.getAvatar());
        }

        // 认领人信息
        if (post.getClaimedBy() != null) {
            User claimer = userMapper.selectById(post.getClaimedBy());
            if (claimer != null) {
                vo.setClaimedByName(claimer.getNickname() != null ? claimer.getNickname() : claimer.getUsername());
            }
        }

        // 图片
        List<RescueImage> images = imageMapper.selectList(
            new LambdaQueryWrapper<RescueImage>().eq(RescueImage::getRescueId, post.getId())
                .orderByAsc(RescueImage::getSortOrder)
        );
        vo.setImages(images.stream().map(RescueImage::getUrl).collect(Collectors.toList()));
    }
}
