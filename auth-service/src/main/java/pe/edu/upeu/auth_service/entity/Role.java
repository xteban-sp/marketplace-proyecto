package pe.edu.upeu.auth_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_rol", unique = true, nullable = false, length = 30)
    private String nombreRol;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_privilegios",
            joinColumns = @JoinColumn(name = "rol_id"),
            inverseJoinColumns = @JoinColumn(name = "privilegio_id")
    )
    private Set<Privilegio> privilegios = new HashSet<>();
}
