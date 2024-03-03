package com.hong.ForPaw.service;

import com.hong.ForPaw.controller.DTO.GroupRequest;
import com.hong.ForPaw.controller.DTO.GroupResponse;
import com.hong.ForPaw.core.errors.CustomException;
import com.hong.ForPaw.core.errors.ExceptionCode;
import com.hong.ForPaw.domain.Group.Group;
import com.hong.ForPaw.domain.Group.GroupUser;
import com.hong.ForPaw.domain.Group.Role;
import com.hong.ForPaw.domain.User.User;
import com.hong.ForPaw.repository.GroupRepository;
import com.hong.ForPaw.repository.GroupUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupUserRepository groupUserRepository;
    private final EntityManager entityManager;

    @Transactional
    public void createGroup(GroupRequest.CreateGroupDTO requestDTO, Long userId){
        // 이름 중복 체크
        if(groupRepository.findByName(requestDTO.name()).isPresent()){
            throw new CustomException(ExceptionCode.GROUP_NAME_EXIST);
        }

        Group group = Group.builder()
                .name(requestDTO.name())
                .region(requestDTO.region())
                .subRegion(requestDTO.subRegion())
                .description(requestDTO.description())
                .category(requestDTO.category())
                .profileURL(requestDTO.profileURL())
                .build();

        groupRepository.save(group);

        // 그룹장 설정
        User userRef = entityManager.getReference(User.class, userId);
        GroupUser groupUser = GroupUser.builder()
                .group(group)
                .user(userRef)
                .role(Role.ADMIN)
                .build();

        groupUserRepository.save(groupUser);
    }

    @Transactional
    public GroupResponse.FindGroupByIdDTO findGroupById(Long groupId, Long userId){
        // 조회 권한 체크 (수정을 위해 가져오는 정보니 권한 체크 필요)
        groupUserRepository.findByGroupIdAndUserId(groupId, userId)
                .ifPresentOrElse(groupUser -> {
                    if (!groupUser.getRole().equals(Role.ADMIN)) {
                        throw new CustomException(ExceptionCode.USER_FORBIDDEN);
                    }
                }, () -> {
                    throw new CustomException(ExceptionCode.USER_FORBIDDEN);
                });

        Group group = groupRepository.findById(groupId).orElseThrow(
                () -> new CustomException(ExceptionCode.GROUP_NOT_FOUND)
        );

        return new GroupResponse.FindGroupByIdDTO(group.getName(), group.getRegion(), group.getSubRegion(), group.getDescription(), group.getCategory(), group.getProfileURL());
    }

    @Transactional
    public void updateGroup(GroupRequest.UpdateGroupDTO requestDTO, Long groupId, Long userId){
        // 수정 권한 체크
        groupUserRepository.findByGroupIdAndUserId(groupId, userId)
                .ifPresentOrElse(groupUser -> {
                    if (!groupUser.getRole().equals(Role.ADMIN)) {
                        throw new CustomException(ExceptionCode.USER_FORBIDDEN);
                    }
                }, () -> {
                    throw new CustomException(ExceptionCode.USER_FORBIDDEN);
                });

        // 이름 중복 체크
        if(groupRepository.findByName(requestDTO.name()).isPresent()){
            throw new CustomException(ExceptionCode.GROUP_NAME_EXIST);
        }

        Group group = groupRepository.findById(groupId).orElseThrow(
                () -> new CustomException(ExceptionCode.GROUP_NOT_FOUND)
        );

        group.updateInfo(requestDTO.name(), requestDTO.region(), requestDTO.subRegion(), requestDTO.description(), requestDTO.category(), requestDTO.profileURL());
    }

    @Transactional
    public GroupResponse.FindAllGroupDTO findGroupList(Long userId, String region){
        // 이 API의 페이지네이션은 0페이지인 5개만 보내줄 것이다.
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"));

        // 추천 그룹 찾기
        // 1. 같은 지역의 그룹  2. 좋아요, 사용자 순  3. 비슷한 연관관계 (카테고리, 설명) => 3번은 AI를 사용해야 하기 때문에 일단은 1과 2의 기준으로 추천
        Page<Group> recommendGroups = groupRepository.findByRegionWithSort(region, Sort.by(Sort.Order.desc("likeNum"), Sort.Order.desc("participationNum")));
        List<GroupResponse.RecommendGroupDTO> allRecommendGroupDTOS = recommendGroups.getContent().stream()
                .map(group -> new GroupResponse.RecommendGroupDTO(group.getId(), group.getName(), group.getDescription(), group.getParticipationNum(), group.getCategory() ,group.getRegion(), group.getSubRegion(), group.getProfileURL(), group.getLikeNum()))
                .collect(Collectors.toList());

        // 매번 동일하게 추천을 할 수는 없으니, 간추린 추천 목록 중에서 5개를 랜덤으로 보내준다.
        Collections.shuffle(allRecommendGroupDTOS);

        List<GroupResponse.RecommendGroupDTO> recommendGroupDTOS = allRecommendGroupDTOS.stream()
                .limit(5)
                .collect(Collectors.toList());

        // 지역 그룹 찾기
        Page<Group> localGroups = groupRepository.findByRegionWithPage(region, pageable);
        List<GroupResponse.LocalGroupDTO> localGroupDTOS = localGroups.getContent().stream()
                .map(group -> new GroupResponse.LocalGroupDTO(group.getId(), group.getName(), group.getDescription(), group.getParticipationNum(), group.getCategory(), group.getRegion(), group.getSubRegion(), group.getProfileURL(), group.getLikeNum()))
                .collect(Collectors.toList());

        // 새 그룹 찾기 => 지역의 새 그룹이 아닌 전체 새 그룹을 보여줌
        Page<Group> newGroups = groupRepository.findAll(pageable);
        List<GroupResponse.NewGroupDTO> newGroupDTOS = newGroups.getContent().stream()
                .map(group -> new GroupResponse.NewGroupDTO(group.getId(), group.getName(), group.getCategory(), group.getRegion(), group.getSubRegion(), group.getProfileURL()))
                .collect(Collectors.toList());

        // 내 그룹 찾기
        Page<GroupUser> groupUsers = groupUserRepository.findByUserId(userId, pageable);
        List<Group> myGroups = groupUsers.getContent().stream()
                .map(GroupUser::getGroup)
                .collect(Collectors.toList());

        List<GroupResponse.MyGroupDTO> myGroupDTOS = myGroups.stream()
                .map(group -> new GroupResponse.MyGroupDTO(group.getId(), group.getName(), group.getDescription(),
                        group.getParticipationNum(), group.getCategory(), group.getRegion(), group.getSubRegion(), group.getProfileURL(), group.getLikeNum()))
                .collect(Collectors.toList());

        return new GroupResponse.FindAllGroupDTO(recommendGroupDTOS, newGroupDTOS, localGroupDTOS, myGroupDTOS);
    }

    @Transactional
    public GroupResponse.FindLocalGroupDTO findLocalGroup(String region, Integer page, Integer size){

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<Group> localGroups = groupRepository.findByRegionWithPage(region, pageable);
        List<GroupResponse.LocalGroupDTO> localGroupDTOS = localGroups.getContent().stream()
                .map(group -> new GroupResponse.LocalGroupDTO(group.getId(), group.getName(), group.getDescription(), group.getParticipationNum(), group.getCategory(), group.getRegion(), group.getSubRegion(), group.getProfileURL(), group.getLikeNum()))
                .collect(Collectors.toList());

        return new GroupResponse.FindLocalGroupDTO(localGroupDTOS);
    }

    @Transactional
    public GroupResponse.FindNewGroupDTO findNewGroup(Integer page, Integer size){

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<Group> newGroups = groupRepository.findAll(pageable);
        List<GroupResponse.NewGroupDTO> newGroupDTOS = newGroups.getContent().stream()
                .map(group -> new GroupResponse.NewGroupDTO(group.getId(), group.getName(), group.getCategory(), group.getRegion(), group.getSubRegion(), group.getProfileURL()))
                .collect(Collectors.toList());

        return new GroupResponse.FindNewGroupDTO(newGroupDTOS);
    }

    @Transactional
    public GroupResponse.FindMyGroupDTO findMyGroup(Long userId, Integer page, Integer size){

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<GroupUser> groupUsers = groupUserRepository.findByUserId(userId, pageable);
        List<Group> myGroups = groupUsers.getContent().stream()
                .map(GroupUser::getGroup)
                .collect(Collectors.toList());

        List<GroupResponse.MyGroupDTO> myGroupDTOS = myGroups.stream()
                .map(group -> new GroupResponse.MyGroupDTO(group.getId(), group.getName(), group.getDescription(),
                        group.getParticipationNum(), group.getCategory(), group.getRegion(), group.getSubRegion(), group.getProfileURL(), group.getLikeNum()))
                .collect(Collectors.toList());

        return new GroupResponse.FindMyGroupDTO(myGroupDTOS);
    }
}