package com.gloomy.server.application.comment;

import com.gloomy.server.application.feed.FeedService;
import com.gloomy.server.application.feed.TestFeedDTO;
import com.gloomy.server.application.feed.TestUserDTO;
import com.gloomy.server.domain.comment.Comment;
import com.gloomy.server.domain.feed.Feed;
import com.gloomy.server.domain.user.User;
import com.gloomy.server.domain.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        "spring.config.location=classpath:application.yml,classpath:aws.yml"
})
class CommentServiceTest {
    @Autowired
    private FeedService feedService;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;

    private TestCommentDTO testCommentDTO;

    @BeforeEach
    void beforeEach() {
        User testUser = new TestUserDTO().makeTestUser();
        userService.createUser(testUser);
        TestFeedDTO testFeedDTO = new TestFeedDTO(testUser, 1);
        Feed testFeed = feedService.createFeed(testFeedDTO.makeNonUserFeedDTO());
        testCommentDTO = new TestCommentDTO(testUser.getId(), testFeed.getId());
    }

    @Test
    void 댓글_생성_비회원_성공() {
        CommentDTO.Request nonUserCommentDTO = testCommentDTO.makeNonUserCommentDTO();

        Comment createdComment = commentService.createComment(nonUserCommentDTO);
        Comment foundComment = commentService.findComment(createdComment.getId());
        assertEquals(foundComment, createdComment);
    }

    @Test
    void 댓글_생성_비회원_실패() {
        CommentDTO.Request nonUserCommentDTOWithoutContent =
                new CommentDTO.Request(null, testCommentDTO.getFeedId(), testCommentDTO.getPassword());
        CommentDTO.Request nonUserCommentDTOWithoutFeedId =
                new CommentDTO.Request(testCommentDTO.getContent(), null, testCommentDTO.getPassword());
        CommentDTO.Request nonUserCommentDTOWithoutPassword =
                new CommentDTO.Request(testCommentDTO.getContent(), testCommentDTO.getFeedId(), (String) null);

        checkCreatedCommentFail(nonUserCommentDTOWithoutContent);
        checkCreatedCommentFail(nonUserCommentDTOWithoutFeedId);
        checkCreatedCommentFail(nonUserCommentDTOWithoutPassword);
    }

    @Test
    void 댓글_생성_회원_성공() {
        CommentDTO.Request userCommentDTO = testCommentDTO.makeUserCommentDTO();

        Comment createdComment = commentService.createComment(userCommentDTO);
        Comment foundComment = commentService.findComment(createdComment.getId());
        assertEquals(foundComment, createdComment);
    }

    @Test
    void 댓글_생성_회원_실패() {
        CommentDTO.Request userCommentDTOWithoutContent =
                new CommentDTO.Request(null, testCommentDTO.getFeedId(), testCommentDTO.getUserId());
        CommentDTO.Request userCommentDTOWithoutFeedId =
                new CommentDTO.Request(testCommentDTO.getContent(), null, testCommentDTO.getUserId());
        CommentDTO.Request userCommentDTOWithoutUserId =
                new CommentDTO.Request(testCommentDTO.getContent(), testCommentDTO.getFeedId(), (Long) null);

        checkCreatedCommentFail(userCommentDTOWithoutContent);
        checkCreatedCommentFail(userCommentDTOWithoutFeedId);
        checkCreatedCommentFail(userCommentDTOWithoutUserId);
    }

    @Test
    void 댓글_조회_비회원_성공() {
        Comment createdComment = commentService.createComment(testCommentDTO.makeNonUserCommentDTO());

        Comment foundComment = commentService.findComment(createdComment.getId());

        assertEquals(foundComment, createdComment);
    }

    @Test
    void 댓글_조회_비회원_실패() {
        Comment createdComment = commentService.createComment(testCommentDTO.makeNonUserCommentDTO());

        commentService.deleteAll();

        checkFoundCommentFail(createdComment.getId(), "[CommentService] 해당 댓글 ID가 존재하지 않습니다.");
    }

    @Test
    void 댓글_조회_회원_성공() {
        Comment createdComment = commentService.createComment(testCommentDTO.makeUserCommentDTO());

        Comment foundComment = commentService.findComment(createdComment.getId());

        assertEquals(foundComment, createdComment);
    }

    @Test
    void 댓글_조회_회원_실패() {
        Comment createdComment = commentService.createComment(testCommentDTO.makeUserCommentDTO());

        commentService.deleteAll();

        checkFoundCommentFail(createdComment.getId(), "[CommentService] 해당 댓글 ID가 존재하지 않습니다.");
    }

    @Test
    void 댓글_조회_공통_실패() {
        checkFoundCommentFail(0L, "[CommentService] 해당 댓글 ID가 유효하지 않습니다.");
        checkFoundCommentFail(null, "[CommentService] 해당 댓글 ID가 유효하지 않습니다.");
    }

    private void checkCreatedCommentFail(CommentDTO.Request commentDTO) {
        assertThrows(IllegalArgumentException.class, () -> {
            commentService.createComment(commentDTO);
        }, "[CommentService] 비회원 댓글 등록 요청 메시지가 잘못되었습니다.");
    }

    private void checkFoundCommentFail(Long commentId, String errorMessage) {
        assertThrows(IllegalArgumentException.class, () -> {
            commentService.findComment(commentId);
        }, errorMessage);
    }
}
