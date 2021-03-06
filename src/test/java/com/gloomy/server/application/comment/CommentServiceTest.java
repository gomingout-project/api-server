package com.gloomy.server.application.comment;

import com.gloomy.server.application.feed.FeedService;
import com.gloomy.server.application.feed.TestFeedDTO;
import com.gloomy.server.application.feed.TestUserDTO;
import com.gloomy.server.application.image.ImageService;
import com.gloomy.server.application.notice.NoticeService;
import com.gloomy.server.domain.comment.Comment;
import com.gloomy.server.domain.common.entity.Status;
import com.gloomy.server.domain.feed.Feed;
import com.gloomy.server.domain.user.User;
import com.gloomy.server.domain.user.UserService;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        "spring.config.location=classpath:test-application.yml,classpath:aws.yml"
})
class CommentServiceTest {
    @Autowired
    private FeedService feedService;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private ImageService imageService;
    @Autowired
    private NoticeService noticeService;

    @Value("${cloud.aws.s3.feedTestDir}")
    private String feedTestDir;
    private TestCommentDTO testCommentDTO;

    @BeforeEach
    void beforeEach() {
        User testUser = userService.createUser(TestUserDTO.makeTestUser());
        TestFeedDTO testFeedDTO = new TestFeedDTO(testUser, 1);
        Feed testFeed = feedService.createFeed(null, testFeedDTO.makeNonUserFeedDTO());
        testCommentDTO = new TestCommentDTO(testFeed.getId(), testUser.getId());
    }

    @AfterEach
    void afterEach() {
        noticeService.deleteAll();
        imageService.deleteAll(feedTestDir);
        commentService.deleteAll();
        feedService.deleteAll();
        userService.deleteAll();
    }

    @Transactional
    @Test
    void ??????_??????_?????????_??????() {
        CommentDTO.Request nonUserCommentDTO = testCommentDTO.makeNonUserCommentDTO();

        Comment createdComment = commentService.createComment(null, nonUserCommentDTO);
        Comment foundComment = commentService.findComment(createdComment.getId());
        assertEquals(foundComment, createdComment);
    }

    @Test
    void ??????_??????_?????????_??????() {
        CommentDTO.Request nonUserCommentDTOWithoutContent =
                new CommentDTO.Request(null, testCommentDTO.getFeedId(), testCommentDTO.getPassword());
        CommentDTO.Request nonUserCommentDTOWithoutFeedId =
                new CommentDTO.Request(testCommentDTO.getContent(), null, testCommentDTO.getPassword());
        CommentDTO.Request nonUserCommentDTOWithoutPassword =
                new CommentDTO.Request(testCommentDTO.getContent(), testCommentDTO.getFeedId(), (String) null);

        checkCreatedCommentFail(null, nonUserCommentDTOWithoutContent);
        checkCreatedCommentFail(null, nonUserCommentDTOWithoutFeedId);
        checkCreatedCommentFail(null, nonUserCommentDTOWithoutPassword);
    }

    @Transactional
    @Test
    void ??????_??????_??????_??????() {
        CommentDTO.Request userCommentDTO = testCommentDTO.makeUserCommentDTO();

        Comment createdComment = commentService.createComment(testCommentDTO.getUserId(), userCommentDTO);
        Comment foundComment = commentService.findComment(createdComment.getId());
        assertEquals(foundComment, createdComment);
    }

    @Test
    void ??????_??????_??????_??????() {
        CommentDTO.Request userCommentDTOWithoutContent =
                new CommentDTO.Request(null, testCommentDTO.getFeedId());
        CommentDTO.Request userCommentDTOWithoutFeedId =
                new CommentDTO.Request(testCommentDTO.getContent(), null);

        checkCreatedCommentFail(testCommentDTO.getUserId(), userCommentDTOWithoutContent);
        checkCreatedCommentFail(testCommentDTO.getUserId(), userCommentDTOWithoutFeedId);
    }

    @Transactional
    @Test
    void ??????_??????_?????????_??????() {
        Comment createdComment = commentService.createComment(null, testCommentDTO.makeNonUserCommentDTO());

        Comment foundComment = commentService.findComment(createdComment.getId());

        assertEquals(foundComment, createdComment);
    }

    @Transactional
    @Test
    void ??????_??????_?????????_??????() {
        Comment createdComment = commentService.createComment(null, testCommentDTO.makeNonUserCommentDTO());

        noticeService.deleteAll();
        commentService.deleteAll();

        checkFoundCommentFail(createdComment.getId(), "[CommentService] ?????? ?????? ID??? ???????????? ????????????.");
    }

    @Transactional
    @Test
    void ??????_??????_??????_??????() {
        Comment createdComment = commentService.createComment(testCommentDTO.getUserId(), testCommentDTO.makeUserCommentDTO());

        Comment foundComment = commentService.findComment(createdComment.getId());

        assertEquals(foundComment, createdComment);
    }

    @Test
    void ??????_??????_??????_??????() {
        Comment createdComment = commentService.createComment(testCommentDTO.getUserId(), testCommentDTO.makeUserCommentDTO());

        noticeService.deleteAll();
        commentService.deleteAll();

        checkFoundCommentFail(createdComment.getId(), "[CommentService] ?????? ?????? ID??? ???????????? ????????????.");
    }

    @Test
    void ??????_??????_??????_??????() {
        checkFoundCommentFail(0L, "[CommentService] ?????? ?????? ID??? ???????????? ????????????.");
        checkFoundCommentFail(null, "[CommentService] ?????? ?????? ID??? ???????????? ????????????.");
    }

    @Transactional
    @Test
    void ??????_??????_??????_??????_??????() {
        createComments(3);

        Page<Comment> foundAllComments = commentService.getFeedAllComments(
                PageRequest.of(0, 10), testCommentDTO.getFeedId());

        assertEquals(foundAllComments.getContent().size(), 3);
    }

    @Test
    void ??????_??????_??????_??????_??????() {
        PageRequest pageable = PageRequest.of(0, 10);

        imageService.deleteAll(feedTestDir);
        feedService.deleteAll();

        checkFoundAllCommentFail(pageable, 0L, "[CommentService] ?????? ?????? ID??? ???????????? ????????????.");
        checkFoundAllCommentFail(pageable, null, "[CommentService] ?????? ?????? ID??? ???????????? ????????????.");
        checkFoundAllCommentFail(pageable, testCommentDTO.getFeedId(), "[CommentService] ?????? ?????? ID??? ???????????? ????????????.");
        checkFoundAllCommentFail(null, testCommentDTO.getFeedId(), "[CommentService] pageable??? ???????????? ????????????.");
    }

    @Transactional
    @Test
    void ??????_??????_??????_??????_??????_??????() {
        Comment activeComment = commentService.createComment(null, testCommentDTO.makeNonUserCommentDTO());
        Comment inactiveComment = commentService.createComment(null, testCommentDTO.makeNonUserCommentDTO());

        commentService.deleteComment(inactiveComment.getId());
        Page<Comment> feedAllActiveComments = commentService.getFeedAllActiveComments(
                PageRequest.of(0, 10), testCommentDTO.getFeedId());

        assertEquals(feedAllActiveComments.getContent().size(), 1);
        assertEquals(feedAllActiveComments.getContent().get(0), activeComment);
    }

    @Test
    void ??????_??????_??????_??????_??????_??????() {
        Pageable pageable = PageRequest.of(0, 10);

        checkFoundAllActiveCommentFail(null, testCommentDTO.getFeedId(),
                "[CommentService] Pageable??? ???????????? ????????????.");
        checkFoundAllActiveCommentFail(pageable, 0L,
                "[CommentService] Pageable??? ???????????? ????????????.");
        checkFoundAllActiveCommentFail(pageable, null,
                "[CommentService] Pageable??? ???????????? ????????????.");
    }

    @Transactional
    @Test
    void ??????_??????_??????() {
        Comment createdComment = commentService.createComment(null, testCommentDTO.makeNonUserCommentDTO());
        String updateContent = "??? ??????";
        UpdateCommentDTO.Request updateCommentDTO = new UpdateCommentDTO.Request();
        updateCommentDTO.setContent(updateContent);

        Comment updatedComment = commentService.updateComment(createdComment.getId(), updateCommentDTO);
        Comment foundComment = commentService.findComment(createdComment.getId());

        assertEquals(updatedComment, foundComment);
    }

    @Transactional
    @Test
    void ??????_??????_??????() {
        Comment deletedComment = commentService.createComment(null, testCommentDTO.makeNonUserCommentDTO());
        String updateContent = "??? ??????";
        UpdateCommentDTO.Request updateCommentDTO = new UpdateCommentDTO.Request();
        updateCommentDTO.setContent(updateContent);
        UpdateCommentDTO.Request updateCommentDTOWithoutContent = new UpdateCommentDTO.Request();

        noticeService.deleteAll();
        commentService.deleteAll();

        Comment createdComment = commentService.createComment(null, testCommentDTO.makeNonUserCommentDTO());

        checkUpdatedCommentFail(createdComment.getId(), updateCommentDTOWithoutContent,
                "[CommentService] ?????? ?????? ?????? ???????????? ?????????????????????.");
        checkUpdatedCommentFail(0L, updateCommentDTO,
                "[CommentService] ?????? ?????? ID??? ???????????? ????????????.");
        checkUpdatedCommentFail(null, updateCommentDTO,
                "[CommentService] ?????? ?????? ID??? ???????????? ????????????.");
        checkUpdatedCommentFail(deletedComment.getId(), updateCommentDTO,
                "[CommentService] ?????? ?????? ID??? ???????????? ????????????.");
    }

    @Transactional
    @Test
    void ??????_??????_??????() {
        Comment createdComment = commentService.createComment(null, testCommentDTO.makeNonUserCommentDTO());
        Comment deletedComment = commentService.deleteComment(createdComment.getId());
        assertEquals(deletedComment.getStatus(), Status.INACTIVE);
    }

    @Transactional
    @Test
    void ??????_??????_??????() {
        Comment createdComment = commentService.createComment(null, testCommentDTO.makeNonUserCommentDTO());

        noticeService.deleteAll();
        commentService.deleteAll();

        checkDeletedCommentFail(0L, "[CommentService] ?????? ?????? ID??? ???????????? ????????????.");
        checkDeletedCommentFail(null, "[CommentService] ?????? ?????? ID??? ???????????? ????????????.");
        checkDeletedCommentFail(createdComment.getId(), "[CommentService] ?????? ?????? ID??? ???????????? ????????????.");
    }

    private void createComments(int commentSize) {
        for (int num = 0; num < commentSize; num++) {
            commentService.createComment(testCommentDTO.getUserId(), testCommentDTO.makeUserCommentDTO());
        }
    }

    private void checkCreatedCommentFail(Long userId, CommentDTO.Request commentDTO) {
        assertThrows(IllegalArgumentException.class, () -> {
            commentService.createComment(userId, commentDTO);
        }, "[CommentService] ????????? ?????? ?????? ?????? ???????????? ?????????????????????.");
    }

    private void checkFoundCommentFail(Long commentId, String errorMessage) {
        assertThrows(IllegalArgumentException.class, () -> {
            commentService.findComment(commentId);
        }, errorMessage);
    }

    private void checkFoundAllCommentFail(Pageable pageable, Long feedId, String errorMessage) {
        assertThrows(IllegalArgumentException.class, () -> {
            commentService.getFeedAllComments(pageable, feedId);
        }, errorMessage);
    }

    private void checkFoundAllActiveCommentFail(Pageable pageable, Long feedId, String errorMessage) {
        assertThrows(IllegalArgumentException.class, () -> {
            commentService.getFeedAllActiveComments(pageable, feedId);
        }, errorMessage);
    }

    private void checkUpdatedCommentFail(Long commentId, UpdateCommentDTO.Request updateCommentDTO, String errorMessage) {
        assertThrows(IllegalArgumentException.class, () -> {
            commentService.updateComment(commentId, updateCommentDTO);
        }, errorMessage);
    }

    private void checkDeletedCommentFail(Long feedId, String errorMessage) {
        assertThrows(IllegalArgumentException.class, () -> {
            commentService.deleteComment(feedId);
        }, errorMessage);
    }
}
