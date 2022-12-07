package toyproject.noticeBoard.domain.post;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import toyproject.noticeBoard.domain.BaseTimeEntity;
import toyproject.noticeBoard.domain.comment.Comment;
import toyproject.noticeBoard.domain.member.Member;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static javax.persistence.CascadeType.ALL;

@Table(name = "POST")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Post extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id")
    private Member writer;

    @Column(length = 40, nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = true)
    private String filePath;

    // == 게시글을 삭제하면 달려있는 댓글 모두 삭제 == //
    @OneToMany(mappedBy = "post", cascade = ALL, orphanRemoval = true)
    private List<Comment> commentList = new ArrayList<>();

    // == 연관관계 편의 메서드 == //
    public void confirmWriter(Member writer) {
        // writer 는 변경이 불가능하므로 이렇게만 해주어도 될 듯
        this.writer = writer;
        writer.addPost(this);
    }

    public void addComment(Comment comment) {
        // comment 의 Post 설정은 comment 에서 함
        commentList.add(comment);
    }

    // == 내용 수정 == //
    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateFilePath(String filePath) {
        this.filePath = filePath;
    }
}
