package com.gloomy.server.domain.user;

import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

@Embeddable
@Getter
public class Profile {

    @Column(name = "name")
    private String name;

    @Embedded
    private Image image;

    protected Profile() {
    }

    static Profile from(String name) {
        return new Profile(name, null);
    }

    private Profile(String name, Image image) {
        this.name = name;
        this.image = image;
    }

    public String getUserName() {
        return name;
    }

    void changeName(String name) {
        this.name = name;
    }
    void changeImage(Image image) {
        this.image = image;
    }
}
