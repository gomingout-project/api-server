package com.gloomy.server.domain.feed;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@EqualsAndHashCode
@Getter
@Embeddable
public class Content {
    @Column(name = "content", nullable = false)
    private String content;

    protected Content() {
    }

    public Content(String content) {
        this.content = content;
    }

    void setContent(String content) {
        this.content = content;
    }
}
