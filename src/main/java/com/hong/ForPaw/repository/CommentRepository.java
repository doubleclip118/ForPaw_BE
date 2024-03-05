package com.hong.ForPaw.repository;

import com.hong.ForPaw.domain.Post.Comment;
import com.hong.ForPaw.domain.Post.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.post.id = :postId")
    List<Comment> findByPostIdWithUser(@Param("postId") Long postId);

    boolean existsById(Long id);

    @Query("SELECT c.user.id FROM Comment c WHERE c.id = :commentId")
    Optional<Long> findUserIdByCommentId(@Param("commentId") Long commentId);

    @Modifying
    @Query("UPDATE Comment c SET c.content = :content WHERE c.id = :commentId")
    void updateCommentContent(@Param("commentId") Long commentId, @Param("content") String content);
}