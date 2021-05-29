package com.github.emraxxor.web.ide.entities;

import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;


/**
 * 
 * @author Attila Barna
 *
 */
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
@Table(
        name = "projects" ,
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_project_identifier", columnNames = "identifier"),
        },
        indexes = {
                @Index(name = "idx_identifier", columnList = "identifier"),
        }
)
public class Project {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
    
    @Column(nullable = false)
    private String identifier;
    
    @Column(nullable = false)
    private String name;
    
    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Container container;
   
    @ManyToOne
    @JoinColumn( name = "user_id")
    private User user;
    
	@UpdateTimestamp
	private LocalDateTime updatedOn;
	
	@CreationTimestamp
	private LocalDateTime createdOn;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Project project = (Project) o;

        return id != null && id.equals(project.id);
    }

    @Override
    public int hashCode() {
        return 1545761250;
    }
}
