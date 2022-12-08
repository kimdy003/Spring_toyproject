package toyproject.noticeBoard.domain.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import toyproject.noticeBoard.domain.comment.Comment;
import toyproject.noticeBoard.domain.comment.repository.CommentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    @Override
    public void save(Comment comment) {
        commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public Comment findById(Long id) throws Exception {
        return commentRepository.findById(id).orElseThrow(() -> new Exception("댓글이 없습니다."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> findAll() {
        return commentRepository.findAll();
    }

    @Override
    public void remove(Long id) throws Exception {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new Exception("댓글이 없습니다."));
        comment.remove();

        List<Comment> removableCommentList = comment.findRemovableList();
        removableCommentList.forEach(removableComment -> commentRepository.delete(removableComment));
    }
}
