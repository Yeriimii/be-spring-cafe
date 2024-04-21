package codesquad.springcafe.service.article;

import codesquad.springcafe.controller.article.UpdateArticle;
import codesquad.springcafe.domain.article.Article;
import codesquad.springcafe.domain.comment.Comment;
import codesquad.springcafe.repository.article.ArticleRepository;
import codesquad.springcafe.repository.comment.CommentRepository;
import codesquad.springcafe.service.exception.DataDeletionException;
import codesquad.springcafe.service.exception.ResourceNotFoundException;
import codesquad.springcafe.service.exception.UnauthorizedException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class ArticleManager implements ArticleService {
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public ArticleManager(ArticleRepository articleRepository, CommentRepository commentRepository) {
        this.articleRepository = articleRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public Article publish(Article article) {
        return articleRepository.save(article);
    }

    @Override
    public Article findArticle(long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("게시물을 찾을 수 없습니다. 게시물 아이디: " + id));
    }

    @Override
    public List<Article> findAllArticle() {
        return articleRepository.findAll();
    }

    @Override
    public void validateAuthor(String loginId, String author) {
        if (!loginId.equals(author)) {
            throw new UnauthorizedException("작성자와 일치하지 않습니다. 로그인 아이디: " + loginId);
        }
    }

    @Override
    public void editArticle(String loginId, UpdateArticle updateParam) {
        /* 작성자 검증: 403 에러 */
        validateAuthor(loginId, updateParam.getCreatedBy());

        articleRepository.update(updateParam);
    }

    @Override
    public void unpublish(long id) {
        /* 게시물 작성자 확인 */
        Article article = findArticle(id);
        String author = article.getCreatedBy();

        /* 해당 게시물의 댓글들 확인 */
        List<Comment> comments = commentRepository.findAllByArticleId(id);

        /* 모든 댓글 작성자가 게시물 작성자와 같은지 확인 */
        boolean allMatch = comments.stream()
                .allMatch(comment -> comment.isSameAuthor(author));

        /* 모든 댓글 작성자가 게시물 작성자와 일치하지 않으면 예외 발생 */
        if (!allMatch) {
            throw new DataDeletionException("데이터를 삭제 할 수 없습니다. 제약조건을 확인해야 합니다. 게시물 아이디: " + id);
        }

        /* 모든 댓글 먼저 삭제 */
        comments.forEach(comment -> commentRepository.delete(comment.getId())); // TODO: bulk update 메서드 만들기

        /* 삭제 트랜잭션 */
        try {
            articleRepository.delete(id);
        } catch (DataIntegrityViolationException e) {
            throw new DataDeletionException("데이터를 삭제 할 수 없습니다. 제약조건을 확인해야 합니다. 게시물 아이디: " + id, e);
        }
    }
}
