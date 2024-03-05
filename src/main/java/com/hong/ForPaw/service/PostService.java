package com.hong.ForPaw.service;

import com.hong.ForPaw.controller.DTO.PostRequest;
import com.hong.ForPaw.controller.DTO.PostResponse;
import com.hong.ForPaw.core.errors.CustomException;
import com.hong.ForPaw.core.errors.ExceptionCode;
import com.hong.ForPaw.domain.Alarm;
import com.hong.ForPaw.domain.Post.*;
import com.hong.ForPaw.domain.User.User;
import com.hong.ForPaw.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final AlarmRepository alarmRepository;
    private final PostReadStatusRepository postReadStatusRepository;
    private final EntityManager entityManager;

    @Transactional
    public PostResponse.CreatePostDTO createPost(PostRequest.CreatePostDTO requestDTO, Long userId){

        User userRef = entityManager.getReference(User.class, userId);

        Post post = Post.builder()
                .user(userRef)
                .type(requestDTO.type())
                .title(requestDTO.title())
                .content(requestDTO.content())
                .build();

        postRepository.save(post);

        List<PostImage> postImages = requestDTO.images().stream()
                .map(postImageDTO -> PostImage.builder().post(post)
                        .imageURL(postImageDTO.imageURL())
                        .build())
                .collect(Collectors.toList());

        postImageRepository.saveAll(postImages);

        return new PostResponse.CreatePostDTO(post.getId());
    }

    @Transactional
    public PostResponse.FindPostListDTO findPostList(Type type, Pageable pageable){

        Page<Post> postPage = postRepository.findByType(type, pageable);

        List<PostResponse.PostDTO> postDTOS = postPage.getContent().stream()
                .map(post -> {
                    List<PostResponse.PostImageDTO> postImageDTOS = postImageRepository.findByPost(post).stream()
                            .map(postImage -> new PostResponse.PostImageDTO(postImage.getId(), postImage.getImageURL()))
                            .collect(Collectors.toList());

                    return new PostResponse.PostDTO(post.getId(), post.getTitle(), post.getContent(), post.getCreatedDate(), post.getCommentNum(), post.getLikeNum(), postImageDTOS);
                })
                .collect(Collectors.toList());

        return new PostResponse.FindPostListDTO(postDTOS);
    }

    @Transactional
    public PostResponse.FindPostByIdDTO findPostById(Long postId, Long userId){
        // 존재하지 않는 글이면 에러
        if (!postRepository.existsById(postId)) {
            throw new CustomException(ExceptionCode.POST_NOT_FOUND);
        }

        List<Comment> comments = commentRepository.findByPostIdWithUser(postId);
        List<PostResponse.CommentDTO> commentDTOS = comments.stream()
                .map(comment -> new PostResponse.CommentDTO(comment.getId(), comment.getUser().getNickName(), comment.getContent(), comment.getCreatedDate(), comment.getUser().getRegin()))
                .collect(Collectors.toList());

        Post postRef = entityManager.getReference(Post.class, postId);
        User userRef = entityManager.getReference(User.class, userId);

        // 게시글 읽음 처리 (화면에서 게시글 확인 여부가 필요한 곳이 있음)
        PostReadStatus postReadStatus = PostReadStatus.builder()
                .post(postRef)
                .user(userRef)
                .build();

        postReadStatusRepository.save(postReadStatus);

        return new PostResponse.FindPostByIdDTO(commentDTOS);
    }

    @Transactional
    public void updatePostById(PostRequest.UpdatePostDTO requestDTO, Long userId, Long postId){
        // 존재하지 않는 게시글이면 에러 발생
        if(!postRepository.existsById(postId)) {
            throw new CustomException(ExceptionCode.POST_NOT_FOUND);
        }

        // 수정 권한 체크
        Long postUserId = postRepository.findUserIdByPostId(postId).get(); // 앞에서 존재하는 게시글임을 체크
        if(!postUserId.equals(userId)){
            throw new CustomException(ExceptionCode.USER_FORBIDDEN);
        }

        postRepository.updatePostTitleAndContent(postId, requestDTO.title(), requestDTO.content());

        // 유지할 이미지를 제외한 모든 이미지 삭제
        if (requestDTO.retainedImageIds() != null && !requestDTO.retainedImageIds().isEmpty()) {
            postImageRepository.deleteByPostIdAndIdNotIn(postId, requestDTO.retainedImageIds());
        } else {
            postImageRepository.deleteByPostId(postId);
        }

        // 새 이미지 추가
        Post postRef = entityManager.getReference(Post.class, postId);

        List<PostImage> newImages = requestDTO.newImages().stream()
                .map(postImageDTO -> PostImage.builder()
                        .post(postRef)
                        .imageURL(postImageDTO.imageURL())
                        .build())
                .collect(Collectors.toList());

        postImageRepository.saveAll(newImages);
    }

    @Transactional
    public void likePost(Long postId, Long userId){
        // 존재하지 않는 글이면 에러
        if (!postRepository.existsById(postId)) {
            throw new CustomException(ExceptionCode.POST_NOT_FOUND);
        }

        // 자기 자신의 글에는 좋아요를 할 수 없다.
        if (postRepository.isOwnPost(postId, userId)) {
            throw new CustomException(ExceptionCode.POST_CANT_LIKE);
        }

        Optional<PostLike> postLikeOP = postLikeRepository.findByUserIdAndPostId(userId, postId);

        // 이미 좋아요를 눌렀다면, 취소하는 액션이니 게시글의 좋아요 수를 감소시키고 하고, postLike 엔티티 삭제
        if(postLikeOP.isPresent()){
            postRepository.decrementLikeNumById(postId);
            postLikeRepository.delete(postLikeOP.get());
        }
        else { // 좋아요를 누르지 않았다면, 좋아요 수를 증가키고, 엔티티 저장
            User userRef = entityManager.getReference(User.class, userId);
            Post postRef = entityManager.getReference(Post.class, postId);

            PostLike postLike = PostLike.builder().user(userRef).post(postRef).build();

            postRepository.incrementLikeNumById(postId);
            postLikeRepository.save(postLike);
        }
    }

    @Transactional
    public PostResponse.CreateCommentDTO createComment(PostRequest.CreateCommentDTO requestDTO, Long userId, Long postId){
        // 존재하지 않는 글이면 에러
        if (!postRepository.existsById(postId)) {
            throw new CustomException(ExceptionCode.POST_NOT_FOUND);
        }

        User userRef = entityManager.getReference(User.class, userId);
        Post postRef = entityManager.getReference(Post.class, postId);

        Comment comment = Comment.builder()
                .user(userRef)
                .post(postRef)
                .content(requestDTO.content())
                .build();

        commentRepository.save(comment);

        // 게시글의 댓글 수 증가
        postRepository.incrementCommentNumById(postId);

        // 게시글 작성자의 userId를 구해서, 프록시 객체 생성
        Long postUserId = postRepository.findUserIdByPostId(postId).get(); // 이미 앞에서 존재하는 글임을 체크함
        User postUserRef = entityManager.getReference(User.class, postUserId);

        // 알람 생성
        Alarm alarm = Alarm.builder()
                .user(postUserRef)
                .content("새로운 댓글: " + requestDTO.content())
                .build();

        alarmRepository.save(alarm);

        return new PostResponse.CreateCommentDTO(comment.getId());
    }
}