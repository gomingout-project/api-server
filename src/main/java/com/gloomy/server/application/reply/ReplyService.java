package com.gloomy.server.application.reply;

import com.gloomy.server.application.comment.CommentService;
import com.gloomy.server.application.notice.NoticeService;
import com.gloomy.server.domain.comment.Comment;
import com.gloomy.server.domain.common.entity.Status;
import com.gloomy.server.domain.feed.Content;
import com.gloomy.server.domain.notice.Type;
import com.gloomy.server.domain.reply.Reply;
import com.gloomy.server.domain.user.User;
import com.gloomy.server.domain.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReplyService {
    private final UserService userService;
    private final CommentService commentService;
    private final NoticeService noticeService;
    private final ReplyRepository replyRepository;

    public ReplyService(UserService userService, CommentService commentService, NoticeService noticeService, ReplyRepository replyRepository) {
        this.userService = userService;
        this.commentService = commentService;
        this.noticeService = noticeService;
        this.replyRepository = replyRepository;
    }

    @Transactional
    public Reply createReply(Long userId, ReplyDTO.Request replyDTO) {
        validateReplyDTO(userId, replyDTO);
        Reply reply = replyRepository.save(makeReply(userId, replyDTO));
        noticeService.createNotice(reply.getCommentId().getFeedId(), reply, Type.REPLY);
        return reply;
    }

    private Reply makeReply(Long userId, ReplyDTO.Request replyDTO) {
        Comment foundComment = commentService.findComment(replyDTO.getCommentId());
        if (userId != null) {
            User foundUser = userService.findUser(userId);
            return replyRepository.save(Reply.of(replyDTO.getContent(), foundComment, foundUser));
        }
        return replyRepository.save(Reply.of(replyDTO.getContent(), foundComment, replyDTO.getPassword()));
    }

    private void validateReplyDTO(Long userId, ReplyDTO.Request replyDTO) {
        if ((userId == null) == (replyDTO.getPassword() == null)
                || !validateCommonElements(replyDTO)) {
            throw new IllegalArgumentException("[ReplyService] ????????? ?????? ?????? ???????????? ?????????????????????.");
        }
        if (userId != null) {
            validateUser(userId);
            return;
        }
        validateNonUserReplyDTO(replyDTO);
    }

    private boolean validateCommonElements(ReplyDTO.Request replyDTO) {
        return (replyDTO.getContent() != null && replyDTO.getContent().length() > 0)
                && validateId(replyDTO.getCommentId());
    }

    private void validateUser(Long userId) {
        if (userId <= 0L) {
            throw new IllegalArgumentException("[ReplyService] ?????? ????????? ?????? ?????? ???????????? ?????????????????????.");
        }
    }

    private void validateNonUserReplyDTO(ReplyDTO.Request replyDTO) {
        if (replyDTO.getPassword().length() <= 0) {
            throw new IllegalArgumentException("[ReplyService] ????????? ????????? ?????? ?????? ???????????? ?????????????????????.");
        }
    }

    @Transactional(readOnly = true)
    public Reply findReply(Long replyId) {
        if (!validateId(replyId)) {
            throw new IllegalArgumentException("[ReplyService] ?????? ????????? ID??? ???????????? ????????????.");
        }
        return replyRepository.findById(replyId).orElseThrow(() -> {
            throw new IllegalArgumentException("[ReplyService] ?????? ????????? ID??? ???????????? ????????????.");
        });
    }

    public Page<Reply> getCommentAllReplies(Pageable pageable, Long commentId) {
        if (pageable == null) {
            throw new IllegalArgumentException("[ReplyService] Pageable??? ???????????? ????????????.");
        }
        if (commentId == null || commentId <= 0L) {
            throw new IllegalArgumentException("[ReplyService] ?????? ?????? ID??? ???????????? ????????????.");
        }
        List<Reply> feedAllReplies = findAllReplies(commentId);
        return new PageImpl<>(feedAllReplies, pageable, feedAllReplies.size());
    }

    @Transactional(readOnly = true)
    public List<Reply> findAllReplies(Long commentId) {
        Comment foundComment = commentService.findComment(commentId);
        return replyRepository.findAllByCommentId(foundComment);
    }

    @Transactional(readOnly = true)
    public Page<Reply> getCommentAllActiveReplies(Pageable pageable, Long commentId) {
        if (pageable == null) {
            throw new IllegalArgumentException("[ReplyService] Pageable??? ???????????? ????????????.");
        }
        if (!validateId(commentId)) {
            throw new IllegalArgumentException("[ReplyService] ?????? ?????? ID??? ???????????? ????????????.");
        }
        Comment foundComment = commentService.findComment(commentId);
        return replyRepository.findAllByCommentIdAndStatus(pageable, foundComment, Status.active());
    }

    public boolean validateId(Long id) {
        return id != null && id > 0L;
    }

    @Transactional
    public Reply updateReply(Long replyId, UpdateReplyDTO.Request updateReplyDTO) {
        validateUpdateReplyRequest(replyId, updateReplyDTO);
        Reply foundReply = findReply(replyId);
        foundReply.setContent(new Content(updateReplyDTO.getContent()));
        return replyRepository.save(foundReply);
    }

    private void validateUpdateReplyRequest(Long replyId, UpdateReplyDTO.Request updateReplyDTO) {
        if (!validateId(replyId)) {
            throw new IllegalArgumentException("[ReplyService] ?????? ????????? ID??? ???????????? ????????????.");
        }
        if (updateReplyDTO == null) {
            throw new IllegalArgumentException("[ReplyService] ????????? ?????? ?????? ???????????? ???????????? ????????????.");
        }
        if (updateReplyDTO.getContent() == null || updateReplyDTO.getContent().length() <= 0) {
            throw new IllegalArgumentException("[ReplyService] ????????? ?????? ?????? ???????????? ?????????????????????.");
        }
    }

    @Transactional
    public Reply deleteReply(Long replyId) {
        Reply foundReply = findReply(replyId);
        foundReply.delete();
        return replyRepository.save(foundReply);
    }

    @Transactional
    public void deleteAll() {
        replyRepository.deleteAll();
    }
}
