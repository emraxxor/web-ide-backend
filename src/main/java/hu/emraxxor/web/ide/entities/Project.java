package hu.emraxxor.web.ide.entities;

import java.time.LocalDateTime;

import javax.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import hu.emraxxor.web.ide.data.type.docker.DockerContainerImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 
 * @author Attila Barna
 *
 */
@Entity
@Data
@Builder
@NoArgsConstructor
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
   
    @OneToOne
    @JoinColumn( name = "user_id")
    private User user;
    
	@UpdateTimestamp
	private LocalDateTime updatedOn;
	
	@CreationTimestamp
	private LocalDateTime createdOn;
}
