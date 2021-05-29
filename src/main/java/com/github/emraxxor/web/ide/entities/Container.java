package com.github.emraxxor.web.ide.entities;

import com.github.emraxxor.web.ide.data.type.docker.DockerContainerImage;
import com.github.emraxxor.web.ide.data.type.docker.ContainerStatus;
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Container container = (Container) o;

        return id != null && id.equals(container.id);
    }

    @Override
    public int hashCode() {
        return 477728388;
    }
}
