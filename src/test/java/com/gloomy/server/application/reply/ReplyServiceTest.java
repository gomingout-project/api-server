package com.gloomy.server.application.reply;

import com.gloomy.server.application.comment.CommentService;
import com.gloomy.server.application.comment.TestCommentDTO;
import com.gloomy.server.application.feed.FeedService;
import com.gloomy.server.application.feed.TestFeedDTO;
import com.gloomy.server.application.feed.TestUserDTO;
import com.gloomy.server.application.image.ImageService;
import com.gloomy.server.application.notice.NoticeService;
import com.gloomy.server.domain.comment.Comment;
import com.gloomy.server.domain.common.entity.Status;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        "spring.config.location=classpath:test-application.yml,classpath:aws.yml"
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
    @Autowired
    private NoticeService noticeService;

    @Value("${cloud.aws.s3.feedTestDir}")
    private String feedTestDir;
    private Comment testComment;
    private TestReplyDTO testReplyDTO;

    @BeforeEach
    void beforeEach() {
        User testUser = userService.createUser(TestUserDTO.makeTestUser());
        TestFeedDTO testFeedDTO = new TestFeedDTO(testUser, 1);
        Feed testFeed = feedService.createFeed(null, testFeedDTO.makeNonUserFeedDTO());
        TestCommentDTO testCommentDTO = new TestCommentDTO(testFeed.getId(), testUser.getId());
        testComment = commentService.createComment(null, testCommentDTO.makeNonUserCommentDTO());
        testReplyDTO = new TestReplyDTO(testCommentDTO.getUserId(), testComment.getId());
    }

    @AfterEach
    void afterEach() {
        noticeService.deleteAll();
        replyService.deleteAll();
        imageService.deleteAll(feedTestDir);
        commentService.deleteAll();
        feedService.deleteAll();
        userService.deleteAll();
    }

    @Test
    void ?????????_??????_?????????_??????() {
        ReplyDTO.Request nonUserReplyDTO = testReplyDTO.makeNonUserReplyDTO();

        Reply createdNonUserReply = replyService.createReply(null, nonUserReplyDTO);
        Reply foundNonUserReply = replyService.findReply(createdNonUserReply.getId());

        Assertions.assertEquals(foundNonUserReply, createdNonUserReply);
    }

    @Test
    void ?????????_??????_?????????_??????() {
        String errorMessage = "[ReplyService] ????????? ????????? ?????? ?????? ???????????? ?????????????????????.";

        ReplyDTO.Request nonUserReplyWithPasswordBlank =
                new ReplyDTO.Request(testReplyDTO.getContent(), testComment.getId(), "");

        checkCreatedReplyFail(null, nonUserReplyWithPasswordBlank, errorMessage);
    }

    @Test
    void ?????????_??????_??????_??????() {
        ReplyDTO.Request userReplyDTO = testReplyDTO.makeUserReplyDTO();

        Reply createdUserReply = replyService.createReply(testReplyDTO.getUserId(), userReplyDTO);
        Reply foundUserReply = replyService.findReply(createdUserReply.getId());

        Assertions.assertEquals(foundUserReply, createdUserReply);
    }

    @Test
    void ?????????_??????_??????_??????() {
        String errorMessage = "[ReplyService] ?????? ????????? ?????? ?????? ???????????? ?????????????????????.";

        ReplyDTO.Request userReplyDTO = testReplyDTO.makeUserReplyDTO();

        checkCreatedReplyFail(0L, userReplyDTO, errorMessage);
    }

    @Test
    void ?????????_??????_??????_??????() {
        String errorMessage = "[ReplyService] ????????? ?????? ?????? ???????????? ?????????????????????.";

        ReplyDTO.Request userReplyDTO = testReplyDTO.makeUserReplyDTO();
        ReplyDTO.Request replyDTOWithContentNull =
                new ReplyDTO.Request(null, testComment.getId(), testReplyDTO.getPassword());
        ReplyDTO.Request replyDTOWithContentBlank =
                new ReplyDTO.Request("", testComment.getId(), testReplyDTO.getPassword());
        ReplyDTO.Request replyDTOWithCommentIdNull =
                new ReplyDTO.Request(testReplyDTO.getContent(), null, testReplyDTO.getPassword());
        ReplyDTO.Request replyDTOWithCommentIdZeroOrLess =
                new ReplyDTO.Request(testReplyDTO.getContent(), 0L, testReplyDTO.getPassword());

        checkCreatedReplyFail(null, userReplyDTO, errorMessage);
        checkCreatedReplyFail(null, replyDTOWithContentNull, errorMessage);
        checkCreatedReplyFail(null, replyDTOWithContentBlank, errorMessage);
        checkCreatedReplyFail(null, replyDTOWithCommentIdNull, errorMessage);
        checkCreatedReplyFail(null, replyDTOWithCommentIdZeroOrLess, errorMessage);
    }

    @Test
    void ?????????_??????_?????????_??????() {
        ReplyDTO.Request nonUserReplyDTO = testReplyDTO.makeNonUserReplyDTO();

        Reply createdNonUserReply = replyService.createReply(null, nonUserReplyDTO);
        Reply foundNonUserReply = replyService.findReply(createdNonUserReply.getId());

        assertEquals(foundNonUserReply, createdNonUserReply);
    }

    @Test
    void ?????????_??????_?????????_??????() {
        ReplyDTO.Request nonUserReplyDTO = testReplyDTO.makeNonUserReplyDTO();

        Reply deletedNonUserReply = replyService.createReply(null, nonUserReplyDTO);
        noticeService.deleteAll();
        replyService.deleteAll();

        checkFoundReplyFail(deletedNonUserReply.getId(), "[ReplyService] ?????? ????????? ID??? ???????????? ????????????.");
    }

    @Test
    void ?????????_??????_??????_??????() {
        ReplyDTO.Request userReplyDTO = testReplyDTO.makeUserReplyDTO();

        Reply createdUserReply = replyService.createReply(testReplyDTO.getUserId(), userReplyDTO);
        Reply foundUserReply = replyService.findReply(createdUserReply.getId());

        assertEquals(foundUserReply, createdUserReply);
    }

    @Test
    void ?????????_??????_??????_??????() {
        ReplyDTO.Request userReplyDTO = testReplyDTO.makeUserReplyDTO();

        Reply deletedUserReply = replyService.createReply(testReplyDTO.getUserId(), userReplyDTO);
        noticeService.deleteAll();
        replyService.deleteAll();

        checkFoundReplyFail(deletedUserReply.getId(), "[ReplyService] ?????? ????????? ID??? ???????????? ????????????.");
    }

    @Test
    void ?????????_??????_??????_??????() {
        checkFoundReplyFail(0L, "[ReplyService] ?????? ????????? ID??? ???????????? ????????????.");
        checkFoundReplyFail(null, "[ReplyService] ?????? ????????? ID??? ???????????? ????????????.");
    }

    @Test
    void ?????????_??????_??????_??????() {
        List<Reply> createdReplies = createReplies(3);

        Page<Reply> foundAllReplies = replyService.getCommentAllReplies(
                PageRequest.of(0, 10), testReplyDTO.getCommentId());

        checkFoundAllRepliesSuccess(foundAllReplies, createdReplies);
    }

    @Test
    void ?????????_??????_??????_??????() {
        PageRequest pageable = PageRequest.of(0, 10);

        noticeService.deleteAll();
        commentService.deleteAll();

        checkFoundAllRepliesFail(pageable, 0L, "[ReplyService] ?????? ?????? ID??? ???????????? ????????????.");
        checkFoundAllRepliesFail(pageable, null, "[ReplyService] ?????? ?????? ID??? ???????????? ????????????.");
        checkFoundAllRepliesFail(pageable, testReplyDTO.getCommentId(), "[CommentService] ?????? ?????? ID??? ???????????? ????????????.");
        checkFoundAllRepliesFail(null, testReplyDTO.getCommentId(), "[ReplyService] Pageable??? ???????????? ????????????.");
    }

    @Test
    void ??????_?????????_??????_??????_??????() {
        Reply activeReply = replyService.createReply(null, testReplyDTO.makeNonUserReplyDTO());
        Reply inactiveReply = replyService.createReply(null, testReplyDTO.makeNonUserReplyDTO());

        replyService.deleteReply(inactiveReply.getId());
        Page<Reply> commentAllActiveReplies = replyService.getCommentAllActiveReplies(
                PageRequest.of(0, 10), testReplyDTO.getCommentId());

        assertEquals(commentAllActiveReplies.getContent().size(), 1);
        assertEquals(commentAllActiveReplies.getContent().get(0), activeReply);
    }

    @Test
    void ??????_?????????_??????_??????_??????() {
        Pageable pageable = PageRequest.of(0, 10);

        checkFoundAllActiveRepliesFail(null, testReplyDTO.getCommentId(),
                "[ReplyService] Pageable??? ???????????? ????????????.");
        checkFoundAllActiveRepliesFail(pageable, 0L,
                "[ReplyService] ?????? ?????? ID??? ???????????? ????????????.");
        checkFoundAllActiveRepliesFail(pageable, null,
                "[ReplyService] ?????? ?????? ID??? ???????????? ????????????.");
    }

    @Test
    void ?????????_??????_??????() {
        Reply userReply = replyService.createReply(testReplyDTO.getUserId(), testReplyDTO.makeUserReplyDTO());
        Reply nonUserReply = replyService.createReply(null, testReplyDTO.makeNonUserReplyDTO());

        checkUpdatedReplySuccess(userReply);
        checkUpdatedReplySuccess(nonUserReply);
    }

    @Test
    void ?????????_??????_??????() {
        String updateContent = "??? ?????????";
        UpdateReplyDTO.Request updateReplyDTO = new UpdateReplyDTO.Request();
        updateReplyDTO.setContent(updateContent);
        UpdateReplyDTO.Request updateReplyDTOWithContentNull = new UpdateReplyDTO.Request();
        UpdateReplyDTO.Request updateReplyDTOWithContentBlank = new UpdateReplyDTO.Request();
        updateReplyDTOWithContentBlank.setContent("");

        Reply deletedReply = replyService.createReply(null, testReplyDTO.makeNonUserReplyDTO());
        noticeService.deleteAll();
        replyService.deleteAll();
        Reply createdReply = replyService.createReply(null, testReplyDTO.makeNonUserReplyDTO());

        checkUpdatedReplyFail(0L, updateReplyDTO, "[ReplyService] ?????? ????????? ID??? ???????????? ????????????.");
        checkUpdatedReplyFail(null, updateReplyDTO, "[ReplyService] ?????? ????????? ID??? ???????????? ????????????.");
        checkUpdatedReplyFail(deletedReply.getId(), updateReplyDTO, "[ReplyService] ?????? ????????? ID??? ???????????? ????????????.");
        checkUpdatedReplyFail(createdReply.getId(), null, "[ReplyService] ????????? ?????? ?????? ???????????? ???????????? ????????????.");
        checkUpdatedReplyFail(createdReply.getId(), updateReplyDTOWithContentNull, "[ReplyService] ????????? ?????? ?????? ???????????? ?????????????????????.");
        checkUpdatedReplyFail(createdReply.getId(), updateReplyDTOWithContentBlank, "[ReplyService] ????????? ?????? ?????? ???????????? ?????????????????????.");
    }

    @Test
    void ?????????_??????_?????????_??????() {
        ReplyDTO.Request nonUserReplyDTO = testReplyDTO.makeNonUserReplyDTO();

        Reply createdNonUserReply = replyService.createReply(null, nonUserReplyDTO);
        Reply deletedNonUserReply = replyService.deleteReply(createdNonUserReply.getId());

        assertEquals(deletedNonUserReply.getStatus(), Status.INACTIVE);
    }

    @Test
    void ?????????_??????_?????????_??????() {
        ReplyDTO.Request nonUserReplyDTO = testReplyDTO.makeNonUserReplyDTO();

        Reply deletedNonUserReply = replyService.createReply(null, nonUserReplyDTO);
        noticeService.deleteAll();
        replyService.deleteAll();

        checkDeletedReplyFail(deletedNonUserReply.getId(), "[ReplyService] ?????? ????????? ID??? ???????????? ????????????.");
    }

    @Test
    void ?????????_??????_??????_??????() {
        ReplyDTO.Request userReplyDTO = testReplyDTO.makeUserReplyDTO();

        Reply createdUserReply = replyService.createReply(testReplyDTO.getUserId(), userReplyDTO);
        Reply deletedUserReply = replyService.deleteReply(createdUserReply.getId());

        assertEquals(deletedUserReply.getStatus(), Status.INACTIVE);
    }

    @Test
    void ?????????_??????_??????_??????() {
        ReplyDTO.Request userReplyDTO = testReplyDTO.makeUserReplyDTO();

        Reply deletedUserReply = replyService.createReply(testReplyDTO.getUserId(), userReplyDTO);
        noticeService.deleteAll();
        replyService.deleteAll();

        checkDeletedReplyFail(deletedUserReply.getId(), "[ReplyService] ?????? ????????? ID??? ???????????? ????????????.");
    }

    @Test
    void ?????????_??????_??????_??????() {
        checkDeletedReplyFail(0L, "[ReplyService] ?????? ????????? ID??? ???????????? ????????????.");
        checkDeletedReplyFail(null, "[ReplyService] ?????? ????????? ID??? ???????????? ????????????.");
    }

    private List<Reply> createReplies(int replySize) {
        List<Reply> createdReplies = new ArrayList<>();
        for (int num = 0; num < replySize; num++) {
            Reply createdReply = replyService.createReply(null, testReplyDTO.makeNonUserReplyDTO());
            createdReplies.add(createdReply);
        }
        return createdReplies;
    }

    private void checkCreatedReplyFail(Long userId, ReplyDTO.Request replyDTO, String errorMessage) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            replyService.createReply(userId, replyDTO);
        });
        assertEquals(exception.getMessage(), errorMessage);
    }

    private void checkFoundReplyFail(Long replyId, String errorMessage) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            replyService.findReply(replyId);
        });
        assertEquals(exception.getMessage(), errorMessage);
    }

    private void checkFoundAllRepliesSuccess(Page<Reply> foundAllReplies, List<Reply> createdReplies) {
        assertEquals(foundAllReplies.getContent().size(), createdReplies.size());
        for (int num = 0; num < createdReplies.size(); num++) {
            assertEquals(foundAllReplies.getContent().get(num), createdReplies.get(num));
        }
    }

    private void checkFoundAllRepliesFail(Pageable pageable, Long commentId, String errorMessage) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            replyService.getCommentAllReplies(pageable, commentId);
        });
        assertEquals(exception.getMessage(), errorMessage);
    }

    private void checkFoundAllActiveRepliesFail(Pageable pageable, Long commentId, String errorMessage) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            replyService.getCommentAllActiveReplies(pageable, commentId);
        });
        assertEquals(exception.getMessage(), errorMessage);
    }

    private void checkUpdatedReplySuccess(Reply reply) {
        String updateContent = "??? ?????????";
        UpdateReplyDTO.Request updateReplyDTO = new UpdateReplyDTO.Request();
        updateReplyDTO.setContent(updateContent);

        Reply updatedUserReply = replyService.updateReply(reply.getId(), updateReplyDTO);
        Reply foundUserReply = replyService.findReply(reply.getId());

        assertEquals(foundUserReply, updatedUserReply);
        assertEquals(foundUserReply.getContent().getContent(), updateContent);
    }

    private void checkUpdatedReplyFail(Long replyId, UpdateReplyDTO.Request updateReplyDTO, String errorMessage) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            replyService.updateReply(replyId, updateReplyDTO);
        });
        assertEquals(exception.getMessage(), errorMessage);
    }

    private void checkDeletedReplyFail(Long replyId, String errorMessage) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            replyService.deleteReply(replyId);
        });
        assertEquals(exception.getMessage(), errorMessage);
    }
}
