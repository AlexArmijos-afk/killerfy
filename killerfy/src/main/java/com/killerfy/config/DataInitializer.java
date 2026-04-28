package com.killerfy.config;

import com.killerfy.model.Dispositivo;
import com.killerfy.model.Dispositivo.TipoDispositivo;
import com.killerfy.model.Rol;
import com.killerfy.model.Rol.NombreRol;
import com.killerfy.repository.DispositivoRepository;
import com.killerfy.repository.RolRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    private final DispositivoRepository dispositivoRepository;
    private final RolRepository rolRepository;

    public DataInitializer(DispositivoRepository dispositivoRepository,
                           RolRepository rolRepository) {
        this.dispositivoRepository = dispositivoRepository;
        this.rolRepository = rolRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        inicializarDispositivos();
        inicializarRoles();
    }

    // ─────────────────────────────────────────────────────
    // Crea los 3 tipos de dispositivo si no existen
    // ─────────────────────────────────────────────────────
    private void inicializarDispositivos() {
        for (TipoDispositivo tipo : TipoDispositivo.values()) {
            dispositivoRepository.findByTipo(tipo)
                    .orElseGet(() -> {
                        Dispositivo d = new Dispositivo(tipo);
                        dispositivoRepository.save(d);
                        System.out.println("[Killerfy] Dispositivo creado: " + tipo);
                        return d;
                    });
        }
    }

    // ─────────────────────────────────────────────────────
    // Crea los roles USER y ADMIN si no existen
    // (centralizamos aquí lo que antes hacía UsuarioService)
    // ─────────────────────────────────────────────────────
    private void inicializarRoles() {
        for (NombreRol nombre : NombreRol.values()) {
            rolRepository.findByNombreRol(nombre)
                    .orElseGet(() -> {
                        Rol r = new Rol(nombre);
                        rolRepository.save(r);
                        System.out.println("[Killerfy] Rol creado: " + nombre);
                        return r;
                    });
        }
    }
}