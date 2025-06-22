package com.gt11.RECUVA.Resources;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Importar Query
import org.springframework.data.repository.query.Param; // Importar Param
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long>{

    // Consulta para obtener recursos públicos
    List<Resource> findByVisibilidad(VisibilityStatus visibilidad);

    // Consulta para obtener recursos privados de un usuario específico
    List<Resource> findByVisibilidadAndUser_Id(VisibilityStatus visibilidad, Long userId);

    // Consulta personalizada para obtener todos los recursos visibles para un usuario (públicos + propios + admin si es admin)
    // Usaremos un método en el servicio/controlador para combinar estas lógicas de forma más limpia.
    // Estos métodos serán utilizados por el ResourceService o directamente por el ResourceController.

    // Ejemplo de consulta para buscar por título y visibilidad
    // @Query("SELECT r FROM Resource r WHERE r.titulo LIKE %:keyword% AND r.visibilidad = :visibilidad")
    // List<Resource> searchByTitleAndVisibility(@Param("keyword") String keyword, @Param("visibilidad") VisibilityStatus visibilidad);
}

/*
package com.gt11.RECUVA.Resources;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long>{

}
*/
