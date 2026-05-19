package pe.edu.upeu.auth_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String fullName;  // 👈 NUEVO: Nombre completo

    @Column(unique = true, nullable = false, length = 8)
    private String dni;  // 👈 NUEVO: DNI (8 dígitos, único)

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false, length = 9)
    private String universityCode;  // 9 dígitos, único

    @Column(unique = true, nullable = false, length = 9)
    private String phone;  // 👈 NUEVO: Celular (9 dígitos, único)

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Column(nullable = false)
    private boolean enabled = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // === UserDetails para Spring Security ===
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> autoridades = new HashSet<>();
        for (Role rol : roles) {
            autoridades.add(new SimpleGrantedAuthority(rol.getNombreRol()));
            for (Privilegio privilegio : rol.getPrivilegios()) {
                autoridades.add(new SimpleGrantedAuthority(privilegio.getCodigoPrivilegio()));
            }
        }
        return autoridades;
    }

    @Override public String getUsername() { return username; }
    @Override public String getPassword() { return password; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return enabled; }
}
