package pe.edu.upeu.auth_service.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.edu.upeu.auth_service.entity.Privilegio;
import pe.edu.upeu.auth_service.entity.Role;
import pe.edu.upeu.auth_service.repository.PrivilegioRepository;
import pe.edu.upeu.auth_service.repository.RoleRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class SeguridadSeedConfig {

    @Bean
    CommandLineRunner inicializarRolesYPrivilegios(RoleRepository roleRepository,
                                                   PrivilegioRepository privilegioRepository) {
        return args -> {
            List<Privilegio> privilegiosBase = List.of(
                    crearPrivilegio("PRODUCTO_CREAR", "Crear productos"),
                    crearPrivilegio("PRODUCTO_EDITAR", "Editar productos"),
                    crearPrivilegio("PRODUCTO_ELIMINAR", "Eliminar productos"),
                    crearPrivilegio("CATEGORIA_GESTIONAR", "Crear, editar y eliminar categorias"),
                    crearPrivilegio("PEDIDO_CREAR", "Crear pedidos"),
                    crearPrivilegio("PEDIDO_VER", "Ver pedidos"),
                    crearPrivilegio("PEDIDO_ACTUALIZAR_ESTADO", "Actualizar estado de pedidos"),
                    crearPrivilegio("PAGO_CREAR", "Crear pagos"),
                    crearPrivilegio("PAGO_VER", "Ver pagos"),
                    crearPrivilegio("PAGO_ACTUALIZAR_ESTADO", "Actualizar estado de pagos"),
                    crearPrivilegio("RESENA_CREAR", "Crear resenas"),
                    crearPrivilegio("USUARIO_ELIMINAR", "Eliminar usuarios"),
                    crearPrivilegio("USUARIO_HABILITAR_VENDEDOR", "Habilitar rol vendedor")
            );

            for (Privilegio privilegio : privilegiosBase) {
                privilegioRepository.findByCodigoPrivilegio(privilegio.getCodigoPrivilegio())
                        .orElseGet(() -> privilegioRepository.save(privilegio));
            }

            Set<String> userCodes = Set.of("PEDIDO_CREAR", "PEDIDO_VER", "PAGO_CREAR", "PAGO_VER", "RESENA_CREAR");
            Set<String> sellerCodes = Set.of("PRODUCTO_CREAR", "PRODUCTO_EDITAR", "PEDIDO_VER");
            Set<String> adminCodes = new HashSet<>();
            for (Privilegio p : privilegioRepository.findAll()) {
                adminCodes.add(p.getCodigoPrivilegio());
            }

            crearORActualizarRol("ROLE_USER", userCodes, roleRepository, privilegioRepository);
            crearORActualizarRol("ROLE_SELLER", sellerCodes, roleRepository, privilegioRepository);
            crearORActualizarRol("ROLE_ADMIN", adminCodes, roleRepository, privilegioRepository);
        };
    }

    private void crearORActualizarRol(String nombreRol,
                                      Set<String> codigosPrivilegios,
                                      RoleRepository roleRepository,
                                      PrivilegioRepository privilegioRepository) {
        Role rol = roleRepository.findByNombreRol(nombreRol)
                .orElseGet(() -> Role.builder().nombreRol(nombreRol).build());

        Set<Privilegio> privilegios = new HashSet<>();
        for (String codigo : codigosPrivilegios) {
            privilegioRepository.findByCodigoPrivilegio(codigo).ifPresent(privilegios::add);
        }
        rol.setPrivilegios(privilegios);
        roleRepository.save(rol);
    }

    private Privilegio crearPrivilegio(String codigo, String descripcion) {
        return Privilegio.builder()
                .codigoPrivilegio(codigo)
                .descripcion(descripcion)
                .build();
    }
}
