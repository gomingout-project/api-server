package com.gloomy.server.application.notice;

import com.gloomy.server.application.AbstractControllerTest;
import com.gloomy.server.application.comment.CommentService;
import com.gloomy.server.application.comment.TestCommentDTO;
import com.gloomy.server.application.feed.FeedService;
import com.gloomy.server.application.feed.TestFeedDTO;
import com.gloomy.server.application.feed.TestUserDTO;
import com.gloomy.server.application.feedlike.FeedLikeDTO;
import com.gloomy.server.application.feedlike.FeedLikeService;
import com.gloomy.server.application.reply.ReplyDTO;
import com.gloomy.server.application.reply.ReplyService;
import com.gloomy.server.domain.comment.Comment;
import com.gloomy.server.domain.feed.Feed;
import com.gloomy.server.domain.jwt.JWTSerializer;
import com.gloomy.server.domain.notice.Notice;
import com.gloomy.server.domain.user.User;
import com.gloomy.server.domain.user.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        "spring.config.location=classpath:test-application.yml,classpath:aws.yml"
})
public class NoticeRestControllerTest extends AbstractControllerTest {
    @Autowired
    private UserService userService;
    @Autowired
    private FeedService feedService;
    @Autowired
    private FeedLikeService feedLikeService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private ReplyService replyService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private JWTSerializer jwtSerializer;

    private User testUser;
    private String token;

    @BeforeEach
    void beforeEach() {
        testUser = userService.createUser(TestUserDTO.makeTestUser());
        token = jwtSerializer.jwtFromUser(testUser);
        TestFeedDTO testFeedDTO = new TestFeedDTO(testUser, 0);
        Feed testFeed = feedService.createFeed(testUser.getId(), testFeedDTO.makeUserFeedDTO());
        TestCommentDTO testCommentDTO = new TestCommentDTO(testFeed.getId(), null);
        Comment testComment = commentService.createComment(null, testCommentDTO.makeNonUserCommentDTO());
        replyService.createReply(null, new ReplyDTO.Request("?????????", testComment.getId(), "12345"));
        feedLikeService.createFeedLike(null, new FeedLikeDTO.Request(testFeed.getId()));
    }

    @AfterEach
    void afterEach() {
    }

    @DisplayName("?????????_??????_??????")
    @Transactional
    @Test
    void getAllNotices() throws Exception {
        this.mockMvc.perform(get("/notice")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("????????? ??????")),
                        requestParameters(
                                parameterWithName("page").description("????????? ??????")),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("?????? ?????? ??????"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("?????? ?????????"),
                                fieldWithPath("result").description("?????? ?????????"),
                                fieldWithPath("result.content[]").type(JsonFieldType.ARRAY).description("?????? ????????? ?????????"),
                                fieldWithPath("result.content[].id").type(JsonFieldType.NUMBER).description("?????? ID"),
                                fieldWithPath("result.content[].userId").type(JsonFieldType.NUMBER).description("????????? ID"),
                                fieldWithPath("result.content[].feedId").type(JsonFieldType.NUMBER).description("?????? ID"),
                                fieldWithPath("result.content[].commentId").optional().type(JsonFieldType.NUMBER).description("(?????? ????????? ??????) ?????? ID"),
                                fieldWithPath("result.content[].replyId").optional().type(JsonFieldType.NUMBER).description("(????????? ????????? ??????) ?????? ID"),
                                fieldWithPath("result.content[].likeId").optional().type(JsonFieldType.NUMBER).description("(????????? ????????? ??????) ?????? ID"),
                                fieldWithPath("result.content[].type").type(JsonFieldType.STRING).description("?????? ?????? (COMMENT, REPLY, LIKE)"),
                                fieldWithPath("result.content[].isRead").type(JsonFieldType.BOOLEAN).description("?????? ?????? ??????"),
                                fieldWithPath("result.content[].status").type(JsonFieldType.STRING).description("?????? ?????? (ACTIVE, INACTIVE)"),
                                fieldWithPath("result.content[].commentCount").type(JsonFieldType.NUMBER).description("?????? ?????? (ACTIVE, INACTIVE)"),
                                fieldWithPath("result.content[].likeCount").type(JsonFieldType.NUMBER).description("?????? ?????? (ACTIVE, INACTIVE)"),
                                fieldWithPath("result.content[].title").type(JsonFieldType.STRING).description("?????? ?????? (ACTIVE, INACTIVE)"),
                                fieldWithPath("result.content[].createdAt").type(JsonFieldType.STRING).description("?????? ????????????"),
                                fieldWithPath("result.content[].updatedAt").type(JsonFieldType.STRING).description("?????? ????????????"),
                                fieldWithPath("result.content[].deletedAt").type(JsonFieldType.STRING).description("?????? ????????????"),

                                fieldWithPath("result.pageable").type(JsonFieldType.STRING).description("pageable ??????"),
                                fieldWithPath("result.totalPages").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                fieldWithPath("result.totalElements").type(JsonFieldType.NUMBER).description("?????? ????????? ??? ????????? ???"),
                                fieldWithPath("result.last").type(JsonFieldType.BOOLEAN).description("????????? ????????? ??????"),
                                fieldWithPath("result.numberOfElements").type(JsonFieldType.NUMBER).description("?????? ????????? ??? ????????? ???"),
                                fieldWithPath("result.first").type(JsonFieldType.BOOLEAN).description("??? ????????? ??????"),
                                fieldWithPath("result.size").type(JsonFieldType.NUMBER).description("????????? ??? ?????? ??????"),
                                fieldWithPath("result.sort.sorted").type(JsonFieldType.BOOLEAN).description("?????? ??????"),
                                fieldWithPath("result.sort.unsorted").type(JsonFieldType.BOOLEAN).description("????????? ??????"),
                                fieldWithPath("result.sort.empty").type(JsonFieldType.BOOLEAN).description("?????? ??????????????? ??????"),
                                fieldWithPath("result.number").type(JsonFieldType.NUMBER).description("?????? ????????? ?????????"),
                                fieldWithPath("result.empty").type(JsonFieldType.BOOLEAN).description("??????????????? ??????"),
                                fieldWithPath("responseTime").type(JsonFieldType.STRING).description("?????? ??????")
                        )
                ));
    }

    @DisplayName("??????_??????_??????")
    @Transactional
    @Test
    void readNotice() throws Exception {
        Notice notice = noticeService.getOneNotice(testUser);

        this.mockMvc.perform(patch("/notice/{noticeId}", notice.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("????????? ??????")),
                        pathParameters(
                                parameterWithName("noticeId").description("?????? ????????? ?????? ID")),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("?????? ?????? ??????"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("?????? ?????????"),
                                fieldWithPath("result").description("?????? ?????????"),
                                fieldWithPath("result.id").type(JsonFieldType.NUMBER).description("?????? ID"),
                                fieldWithPath("result.userId").type(JsonFieldType.NUMBER).description("????????? ID"),
                                fieldWithPath("result.feedId").type(JsonFieldType.NUMBER).description("?????? ID"),
                                fieldWithPath("result.commentId").optional().type(JsonFieldType.NUMBER).description("(?????? ????????? ??????) ?????? ID"),
                                fieldWithPath("result.replyId").optional().type(JsonFieldType.NUMBER).description("(????????? ????????? ??????) ?????? ID"),
                                fieldWithPath("result.likeId").optional().type(JsonFieldType.NUMBER).description("(????????? ????????? ??????) ?????? ID"),
                                fieldWithPath("result.type").type(JsonFieldType.STRING).description("?????? ?????? (COMMENT, REPLY, LIKE)"),
                                fieldWithPath("result.isRead").type(JsonFieldType.BOOLEAN).description("?????? ?????? ??????"),
                                fieldWithPath("result.status").type(JsonFieldType.STRING).description("?????? ?????? (ACTIVE, INACTIVE)"),
                                fieldWithPath("result.commentCount").type(JsonFieldType.NUMBER).description("?????? ?????? (ACTIVE, INACTIVE)"),
                                fieldWithPath("result.likeCount").type(JsonFieldType.NUMBER).description("?????? ?????? (ACTIVE, INACTIVE)"),
                                fieldWithPath("result.title").type(JsonFieldType.STRING).description("?????? ?????? (ACTIVE, INACTIVE)"),
                                fieldWithPath("result.createdAt").type(JsonFieldType.STRING).description("?????? ????????????"),
                                fieldWithPath("result.updatedAt").type(JsonFieldType.STRING).description("?????? ????????????"),
                                fieldWithPath("result.deletedAt").type(JsonFieldType.STRING).description("?????? ????????????"),
                                fieldWithPath("responseTime").type(JsonFieldType.STRING).description("?????? ??????")
                        )
                ));
    }
}
