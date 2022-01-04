package com.gloomy.server.application.reply;

import com.gloomy.server.application.comment.CommentService;
import com.gloomy.server.application.comment.TestCommentDTO;
import com.gloomy.server.application.feed.FeedService;
import com.gloomy.server.application.feed.TestFeedDTO;
import com.gloomy.server.application.feed.TestUserDTO;
import com.gloomy.server.application.image.ImageService;
import com.gloomy.server.domain.comment.Comment;
import com.gloomy.server.domain.feed.Feed;
import com.gloomy.server.domain.reply.Reply;
import com.gloomy.server.domain.user.User;
import com.gloomy.server.domain.user.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        "spring.config.location=classpath:application.yml,classpath:aws.yml"
})
public class ReplyServiceTest {
    @Autowired
    private UserService userService;
    @Autowired
    private FeedService feedService;
    @Autowired
    private ImageService imageService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private ReplyService replyService;

    private Comment testComment;
    private TestReplyDTO testReplyDTO;

    @BeforeEach
    void beforeEach() {
        TestUserDTO testUserDTO = new TestUserDTO();
        User testUser = userService.createUser(testUserDTO.makeTestUser());
        TestFeedDTO testFeedDTO = new TestFeedDTO(testUser, 1);
        Feed testFeed = feedService.createFeed(testFeedDTO.makeNonUserFeedDTO());
        TestCommentDTO testCommentDTO = new TestCommentDTO(testFeed.getId(), testUser.getId());
        testComment = commentService.createComment(testCommentDTO.makeNonUserCommentDTO());
        testReplyDTO = new TestReplyDTO(testFeed, testComment);
    }

    @AfterEach
    void afterEach() {
        replyService.deleteAll();
        imageService.deleteAll();
        commentService.deleteAll();
        feedService.deleteAll();
        userService.deleteAll();
    }

    @Test
    void 대댓글_생성_회원_성공() {
        User replyUser = userService.createUser(new TestUserDTO().makeTestUser());
        ReplyDTO.Request userReplyDTO = testReplyDTO.makeUserReplyDTO(replyUser);

        Reply createdUserReply = replyService.createReply(userReplyDTO);
        Reply foundUserReply = replyService.findReply(createdUserReply.getId());

        Assertions.assertEquals(foundUserReply, createdUserReply);
    }

    @Test
    void 대댓글_생성_회원_실패() {
        String errorMessage = "[ReplyService] 회원 대댓글 등록 요청 메시지가 잘못되었습니다.";

        ReplyDTO.Request userReplyWithUserIdZeroOrLess =
                new ReplyDTO.Request(testReplyDTO.content, testComment.getFeedId().getId(), testComment.getId(), 0L);

        checkCreatedReplyFail(userReplyWithUserIdZeroOrLess, errorMessage);
    }

    @Test
    void 대댓글_생성_비회원_성공() {
        ReplyDTO.Request nonUserReplyDTO = testReplyDTO.makeNonUserReplyDTO();

        Reply createdNonUserReply = replyService.createReply(nonUserReplyDTO);
        Reply foundNonUserReply = replyService.findReply(createdNonUserReply.getId());

        Assertions.assertEquals(foundNonUserReply, createdNonUserReply);
    }

    @Test
    void 대댓글_생성_비회원_실패() {
        String errorMessage = "[ReplyService] 비회원 대댓글 등록 요청 메시지가 잘못되었습니다.";

        ReplyDTO.Request nonUserReplyWithPasswordBlank =
                new ReplyDTO.Request(testReplyDTO.content, testComment.getFeedId().getId(), testComment.getId(), "");

        checkCreatedReplyFail(nonUserReplyWithPasswordBlank, errorMessage);
    }

    @Test
    void 대댓글_생성_공통_실패() {
        String errorMessage = "[ReplyService] 대댓글 등록 요청 메시지가 잘못되었습니다.";

        ReplyDTO.Request replyWithContentNull =
                new ReplyDTO.Request(null, testComment.getFeedId().getId(), testComment.getId(), testReplyDTO.password);
        ReplyDTO.Request replyWithContentBlank =
                new ReplyDTO.Request("", testComment.getFeedId().getId(), testComment.getId(), testReplyDTO.password);
        ReplyDTO.Request replyWithFeedIdNull =
                new ReplyDTO.Request(testReplyDTO.content, null, testComment.getId(), testReplyDTO.password);
        ReplyDTO.Request replyWithFeedIdZeroOrLess =
                new ReplyDTO.Request(testReplyDTO.content, 0L, testComment.getId(), testReplyDTO.password);
        ReplyDTO.Request replyWithCommentIdNull =
                new ReplyDTO.Request(testReplyDTO.content, testComment.getFeedId().getId(), null, testReplyDTO.password);
        ReplyDTO.Request replyWithCommentIdZeroOrLess =
                new ReplyDTO.Request(testReplyDTO.content, testComment.getFeedId().getId(), 0L, testReplyDTO.password);

        checkCreatedReplyFail(replyWithContentNull, errorMessage);
        checkCreatedReplyFail(replyWithContentBlank, errorMessage);
        checkCreatedReplyFail(replyWithFeedIdNull, errorMessage);
        checkCreatedReplyFail(replyWithFeedIdZeroOrLess, errorMessage);
        checkCreatedReplyFail(replyWithCommentIdNull, errorMessage);
        checkCreatedReplyFail(replyWithCommentIdZeroOrLess, errorMessage);
    }

    private void checkCreatedReplyFail(ReplyDTO.Request replyDTO, String errorMessage) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            replyService.createReply(replyDTO);
        });
        assertEquals(exception.getMessage(), errorMessage);
    }

    static class TestReplyDTO {
        private final String content;
        private final String password;
        private final Feed feedId;
        private final Comment commentId;

        TestReplyDTO(Feed feedId, Comment commentId) {
            this.content = "새 대댓글";
            this.password = "12345";
            this.feedId = feedId;
            this.commentId = commentId;
        }

        ReplyDTO.Request makeUserReplyDTO(User user) {
            return new ReplyDTO.Request(content, feedId.getId(), commentId.getId(), user.getId());
        }

        ReplyDTO.Request makeNonUserReplyDTO() {
            return new ReplyDTO.Request(content, feedId.getId(), commentId.getId(), password);
        }
    }
}
