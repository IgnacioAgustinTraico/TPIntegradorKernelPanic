package com.tuti.grupo.kernelpanic.trabajo;

import com.tuti.grupo.kernelpanic.trabajo.entities.Persona;
import com.tuti.grupo.kernelpanic.trabajo.repositories.PersonaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final PersonaRepository personaRepository;
    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(PersonaRepository personaRepository, JdbcTemplate jdbcTemplate) {
        this.personaRepository = personaRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        migrarEsquemaPropiedad();

        if (personaRepository.count() == 0) {
            Persona persona1 = new Persona();
            persona1.setNombre("María López");
            personaRepository.save(persona1);

            Persona persona2 = new Persona();
            persona2.setNombre("Carlos Ruiz");
            personaRepository.save(persona2);

            Persona persona3 = new Persona();
            persona3.setNombre("Ana Gómez");
            personaRepository.save(persona3);
        }
    }

    private void migrarEsquemaPropiedad() {
        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS ciudad (id BIGINT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(200) NOT NULL UNIQUE)");

            Integer ciudadColumn = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'propiedad' AND column_name = 'ciudad'",
                    Integer.class);
            Integer ciudadIdColumn = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'propiedad' AND column_name = 'ciudad_id'",
                    Integer.class);

            if (ciudadIdColumn == null || ciudadIdColumn == 0) {
                jdbcTemplate.execute("ALTER TABLE propiedad ADD COLUMN ciudad_id BIGINT NULL");
            }

            Integer provinciaColumn = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'ciudad' AND column_name = 'provincia_id'",
                    Integer.class);
            if (provinciaColumn != null && provinciaColumn > 0) {
                jdbcTemplate.queryForList(
                        "SELECT constraint_name FROM information_schema.key_column_usage WHERE table_schema = DATABASE() AND table_name = 'ciudad' AND column_name = 'provincia_id' AND referenced_table_name IS NOT NULL",
                        String.class)
                        .forEach(constraint -> jdbcTemplate.execute("ALTER TABLE ciudad DROP FOREIGN KEY " + constraint));
                jdbcTemplate.execute("ALTER TABLE ciudad DROP COLUMN provincia_id");
            }

            if (ciudadColumn != null && ciudadColumn > 0) {
                jdbcTemplate.queryForList(
                        "SELECT DISTINCT TRIM(ciudad) AS nombre FROM propiedad WHERE ciudad IS NOT NULL AND TRIM(ciudad) <> ''",
                        String.class)
                        .stream()
                        .filter(nombre -> nombre != null && !nombre.isBlank())
                        .forEach(nombre -> jdbcTemplate.update("INSERT IGNORE INTO ciudad(nombre) VALUES (?)", nombre));

                jdbcTemplate.execute("UPDATE propiedad p JOIN ciudad c ON lower(trim(p.ciudad)) = lower(c.nombre) SET p.ciudad_id = c.id WHERE p.ciudad IS NOT NULL");
                jdbcTemplate.execute("ALTER TABLE propiedad DROP COLUMN ciudad");
            }
        } catch (DataAccessException ignored) {
            
        }
    }
}
