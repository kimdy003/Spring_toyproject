package toyproject.noticeBoard.domain.comment.service;

import toyproject.noticeBoard.domain.comment.Comment;

import java.util.List;

public interface CommentService {
    void save(Comment comment);

    Comment findById(Long id) throws Exception;

    List<Comment> findAll();

    void remove(Long id) throws Exception;
}
