/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.emraxxor.web.ide.entities;

import com.google.common.collect.Lists;
import hu.emraxxor.web.ide.config.ApplicationUserRole;
import hu.emraxxor.web.ide.data.type.IgnoreField;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author attila
 */
@Entity
@Table(
        name = "users" ,
        uniqueConstraints = {
                @UniqueConstraint(name = "users_email_unique", columnNames = "email"),
                @UniqueConstraint(name = "users_neptun_unique", columnNames = "neptun_id")
        },
        indexes = {
                @Index(name = "idx_is_active", columnList = "is_active"),
                @Index(name = "idx_first_last", columnList = "first_name,last_name")
        }
)
@XmlRootElement
@NamedQueries({})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

	@IgnoreField
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "uid")
    @IgnoreField
	private Long userId;
    
    @NotNull
    @Size(min = 1, max = 64)
    @Column(name = "neptun_id", unique = true, nullable = false)
    @IgnoreField
    private String neptunId;
    
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "password",nullable = false)
    @IgnoreField
    private String userPassword;
    
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "email", unique = true)
    private String userMail;
    
    @Column(name="first_name")
    private String firstName;
    
    @Column(name="last_name")
    private String lastName;

    private String address;

    private String state;

    private String city;

    private Integer zip;
    
    @IgnoreField
    private String image;

    @Column(name="last_seen")  @IgnoreField
    private LocalDateTime lastSeen;
   
    @Column(name="created_on") @IgnoreField @CreationTimestamp
    private LocalDateTime createdOn;
    
    @NotNull
    @Column(name="is_active") @IgnoreField
    private Boolean isActive;
    
    @Column(nullable = false)
    @NotNull
    @Enumerated(EnumType.STRING)
    @IgnoreField
    private ApplicationUserRole role;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private final List<Project> projects = Lists.newArrayList();
    
    @PrePersist
    public void prePersist() {
        role = ApplicationUserRole.USER;
        isActive = true;
        address = "";
        city = "";
        state = "";
        zip = null;
    }
    
    
    public void addProject(Project p ) {
    	p.setUser(this);
    	this.projects.add(p);
    }
    
    public void removeProject(Project p) {
    	this.projects.remove(p);
	}
}
