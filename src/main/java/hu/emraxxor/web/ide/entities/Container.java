package hu.emraxxor.web.ide.entities;

import java.time.LocalDateTime;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import hu.emraxxor.web.ide.data.type.IgnoreField;
import hu.emraxxor.web.ide.data.type.docker.ContainerStatus;
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
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "containers" ,
        uniqueConstraints = {
                @UniqueConstraint(name = "container_bind_ux", columnNames = "bind"),
                @UniqueConstraint(name = "container_name_ux", columnNames = "name"),
        },
        indexes = {
                @Index(name = "idx_container_name", columnList = "name"),
                @Index(name = "idx_container_id", columnList = "container_id"),
        }
)
@Builder
public class Container {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
	private Long id;
    
    @Enumerated
    private DockerContainerImage image;
   
    private String name;
    
    private Integer bind;
    
    private Integer exposed;
    
    private String userdir;
    
    private String appdir;
    
    @Column(name = "container_id")
    private String containerId;
    
    @Enumerated
    private ContainerStatus status;
    
    private String ip;
    
    @OneToOne
    @JoinColumn(name = "project")
    private Project project;
	
	@UpdateTimestamp
	private LocalDateTime updatedOn;
	
	@CreationTimestamp
	private LocalDateTime createdOn;
	
	
}
