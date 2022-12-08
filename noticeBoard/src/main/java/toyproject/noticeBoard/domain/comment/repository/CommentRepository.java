package toyproject.noticeBoard.domain.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import toyproject.noticeBoard.domain.comment.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
