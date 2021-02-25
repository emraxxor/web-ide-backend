package hu.emraxxor.web.ide.entities;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
    
    @OneToOne(mappedBy = "project")
    private Container container;
   
    @OneToOne
    @JoinColumn( name = "user_id")
    private User user;
    
	@UpdateTimestamp
	private LocalDateTime updatedOn;
	
	@CreationTimestamp
	private LocalDateTime createdOn;
}
