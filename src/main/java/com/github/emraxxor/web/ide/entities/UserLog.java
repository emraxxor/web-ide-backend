package com.github.emraxxor.web.ide.entities;

import com.github.emraxxor.web.ide.data.type.IgnoreField;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;

@Entity
@Table(name = "users_log", indexes = {
        @Index(name = "IDX_USERSLOG_ip", columnList = "ip")
})
@XmlRootElement
@NamedQueries({})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserLog {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="created_on") @IgnoreField
    @CreationTimestamp
    private LocalDateTime createdOn;

    private String ip;

    @ManyToOne
    @JoinColumn( name = "user_id")
    private User user;

}
