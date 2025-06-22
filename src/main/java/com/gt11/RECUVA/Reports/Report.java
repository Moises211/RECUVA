
package com.gt11.RECUVA.Reports;

import com.gt11.RECUVA.Resources.Resource;
import com.gt11.RECUVA.Users.User; // Importa tu entidad User existente

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist; 
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name="report") 
public class Report {
    @Id
    @Column(name="reportID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dateReport", nullable = false) 
    private LocalDateTime fecha; 

    @Column(name = "motive", nullable = false) 
    private String motivo;

    @ManyToOne
    @JoinColumn(name = "userID", nullable = false) 
    private User user; 

    @ManyToOne
    @JoinColumn(name = "resourceID") 
    private Resource resource; 

    
    public Report() {
    }

    
    public Report(Long id) {
        this.id = id;
    }

    
    @PrePersist
    protected void onCreate() {
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
    }

    // --- Getters y Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFecha() { // Cambiado a LocalDateTime
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) { // Cambiado a LocalDateTime
        this.fecha = fecha;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    // --- Métodos hashCode, equals, toString ---
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Report other = (Report) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Report [id=" + id + ", fecha=" + fecha + ", motivo=" + motivo + ", user=" + (user != null ? user.getId() : "null") + ", resource=" + (resource != null ? resource.getId() : "null") + "]";
    }
}
