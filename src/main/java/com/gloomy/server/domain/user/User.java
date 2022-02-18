package com.gloomy.server.domain.user;

import com.gloomy.server.domain.common.entity.Status;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Objects;

import static javax.persistence.GenerationType.IDENTITY;

@Getter
@ToString
@Table(name = "users")
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Embedded
    private Profile profile;

    @Embedded
    private Password password;

    @Enumerated(EnumType.STRING)
    private Sex sex;

    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Status joinStatus;

    private String kakaoToken;
    private String refreshToken;


    protected User() {
    }

    private User(String email, Profile profile, Password password) {
        this.email = email;
        this.profile = profile;
        this.password = password;
        this.joinStatus=Status.ACTIVE;
    }
    private User(String email, Profile profile,Password password,String kakaoToken,String refreshToken) {
        this.email = email;
        this.profile = profile;
        this.password = password;
        this.kakaoToken=kakaoToken;
        this.joinStatus=Status.ACTIVE;
        this.refreshToken=refreshToken;
    }

    private User(String email, Profile profile,Password password,Sex sex, LocalDate dateOfBirth){
        this.email = email;
        this.profile = profile;
        this.password = password;
        this.sex=sex;
        this.dateOfBirth=dateOfBirth;
        this.joinStatus=Status.ACTIVE;
    }

    public static User of(String email, String nickName, Password password,
                          Sex sex, int year,int month,int day){
        return new User(email, Profile.from(nickName),password,sex,LocalDate.of(year,month,day));
    }

    public static User of(String email, String name, Password password) {
        return new User(email, Profile.from(name), password);
    }

    public static User of(String email, String name) {
        return new User(email, Profile.from(name), null);
    }

    public static User of(String email, String name,String kakaoToken,String refreshToken) {
        return new User(email,Profile.from(name), null,kakaoToken,refreshToken);
    }

    boolean matchesPassword(String rawPassword, PasswordEncoder passwordEncoder) {
        return password.matchesPassword(rawPassword, passwordEncoder);
    }

    public void inactiveUser(){
        changeJoinStatus(Status.INACTIVE);
    }

    /**
     * change*
     */
    public void changeId(Long id) {
        this.id = id;
    }
    public void changeEmail(String email){this.email=email;}
    public void changeJoinStatus(Status status){this.joinStatus=status;}
    public void changeKakaoToken(String kakaoToken) {this.kakaoToken=kakaoToken;}
    public void changeRefreshToken(String refreshToken) {this.refreshToken=refreshToken;}

    public String getName(){ return this.profile.getName();}
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return email.equals(user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}
