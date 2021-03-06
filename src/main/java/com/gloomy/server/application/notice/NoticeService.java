package com.gloomy.server.application.notice;

import com.gloomy.server.domain.comment.Comment;
import com.gloomy.server.domain.common.entity.BaseEntity;
import com.gloomy.server.domain.feed.Feed;
import com.gloomy.server.domain.feedlike.FeedLike;
import com.gloomy.server.domain.notice.Notice;
import com.gloomy.server.domain.notice.Type;
import com.gloomy.server.domain.reply.Reply;
import com.gloomy.server.domain.user.User;
import com.gloomy.server.domain.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NoticeService {
    private final UserService userService;
    private final NoticeRepository noticeRepository;

    public NoticeService(UserService userService, NoticeRepository noticeRepository) {
        this.userService = userService;
        this.noticeRepository = noticeRepository;
    }

    public Notice createNotice(Feed feedId, BaseEntity entityId, Type entityType) {
        validateIdAndType(feedId, entityId, entityType);
        return createEntityNotice(feedId, entityId, entityType);
    }

    private Notice createEntityNotice(Feed feedId, BaseEntity entityId, Type entityType) {
        if (entityType == Type.COMMENT) {
            return createCommentNotice(feedId, (Comment) entityId, entityType);
        }
        if (entityType == Type.REPLY) {
            return createReplyNotice(feedId, (Reply) entityId, entityType);
        }
        return createFeedLikeNotice(feedId, (FeedLike) entityId, entityType);
    }

    private Notice createCommentNotice(Feed feedId, Comment commentId, Type entityType) {
        Notice commentNotice = Notice.of(feedId, commentId, entityType);
        return noticeRepository.save(commentNotice);
    }

    private Notice createReplyNotice(Feed feedId, Reply replyId, Type entityType) {
        Notice replyNotice = Notice.of(feedId, replyId, entityType);
        return noticeRepository.save(replyNotice);
    }

    private Notice createFeedLikeNotice(Feed feedId, FeedLike feedLikeId, Type entityType) {
        Notice feedLikeNotice = Notice.of(feedId, feedLikeId, entityType);
        return noticeRepository.save(feedLikeNotice);
    }

    @Transactional(readOnly = true)
    public Page<Notice> getAllNotices(Pageable pageable, Long userId) {
        validatePageAndUserId(pageable, userId);
        User user = userService.findUser(userId);
        return noticeRepository.findAllByUserIdOrderByIdDesc(pageable, user);
    }

    @Transactional(readOnly = true)
    public Notice getOneNotice(User user) {
        validateNotNull(user, "userId??? ???????????? ????????????.");
        return noticeRepository.findFirstByUserId(user).orElseThrow(() -> {
            throw new IllegalArgumentException("[NoticeService] ?????? ?????? ID??? ????????? ???????????? ????????????.");
        });
    }

    @Transactional(readOnly = true)
    public Integer countAllNotices(Long userId) {
        validateId(userId, "userId??? ???????????? ????????????.");
        User user = userService.findUser(userId);
        return noticeRepository.countAllByUserId(user);
    }

    @Transactional
    public Notice readNotice(Long noticeId) {
        validateId(noticeId, "userId??? ???????????? ????????????.");
        Notice foundNotice = findOneNotice(noticeId);
        foundNotice.read();
        return noticeRepository.save(foundNotice);
    }

    @Transactional(readOnly = true)
    public Notice findOneNotice(Long noticeId) throws IllegalArgumentException {
        validateId(noticeId, "?????? ID??? ???????????? ????????????.");
        return noticeRepository.findById(noticeId).orElseThrow(() -> {
            throw new IllegalArgumentException("[NoticeService] ?????? ID??? ???????????? ????????????.");
        });
    }

    @Transactional(readOnly = true)
    public Notice findOneNotice(Comment commentId) {
        return noticeRepository.findByCommentId(commentId).orElseThrow(() -> {
            throw new IllegalArgumentException("[NoticeService] ?????? ?????? ID??? ????????? ???????????? ????????????.");
        });
    }

    @Transactional(readOnly = true)
    public Notice findOneNotice(Reply replyId) {
        return noticeRepository.findByReplyId(replyId).orElseThrow(() -> {
            throw new IllegalArgumentException("[NoticeService] ?????? ????????? ID??? ????????? ???????????? ????????????.");
        });
    }

    @Transactional(readOnly = true)
    public Notice findOneNotice(FeedLike feedLikeId) {
        return noticeRepository.findByFeedLikeId(feedLikeId).orElseThrow(() -> {
            throw new IllegalArgumentException("[NoticeService] ?????? ????????? ID??? ????????? ???????????? ????????????.");
        });
    }

    @Transactional
    public void deleteAll() {
        noticeRepository.deleteAll();
    }

    private void validateIdAndType(Feed feedId, BaseEntity entity, Type entityType) {
        if (feedId == null || entity == null || entityType == null) {
            throw new IllegalArgumentException("[NoticeService] ?????? ?????? ??????????????? ???????????? ????????????.");
        }
        if (entityType == Type.COMMENT && !(entity instanceof Comment)) {
            throw new IllegalArgumentException("[NoticeService] ????????? ???????????? ????????????.");
        }
        if (entityType == Type.REPLY && !(entity instanceof Reply)) {
            throw new IllegalArgumentException("[NoticeService] ???????????? ???????????? ????????????.");
        }
        if (entityType == Type.LIKE && !(entity instanceof FeedLike)) {
            throw new IllegalArgumentException("[NoticeService] ???????????? ???????????? ????????????.");
        }
    }

    private void validatePageAndUserId(Pageable pageable, Long userId) {
        validateNotNull(pageable, "pageable??? ???????????? ????????????.");
        validateId(userId, "userId??? ???????????? ????????????.");
    }

    private void validateNotNull(Object object, String errorMessage) {
        if (object == null) {
            throw new IllegalArgumentException("[NoticeService] " + errorMessage);
        }
    }

    private void validateId(Long id, String errorMessage) {
        if (id == null || id <= 0L) {
            throw new IllegalArgumentException("[NoticeService] " + errorMessage);
        }
    }
}
