package com.gt11.RECUVA.Resources;

import java.time.LocalDate;
import java.util.List;

import com.gt11.RECUVA.Ratings.Ratings;
import com.gt11.RECUVA.Reports.Report;
import com.gt11.RECUVA.Subjects.Subject;
import com.gt11.RECUVA.Users.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType; // Importar EnumType
import jakarta.persistence.Enumerated; // Importar Enumerated
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

// Define el Enum para los estados de visibilidad
enum VisibilityStatus {
    PUBLIC,        // Cualquier usuario logueado puede verlo
    ADMIN_ONLY,    // Solo usuarios con rol ADMIN pueden verlo
    PRIVATE        // Solo el usuario que lo subió puede verlo
}

@Entity
@Table(name = "resource")
public class Resource {

    @Id
    @Column(name ="resourceID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String titulo;

    @Column(name = "description")
    private String descripcion;

    @Column(name = "type")
    private String tipoDoc;

    @Column(name= "url")
    private String url;

    @Column(name = "create_at")
    private LocalDate fechaCreacion;

    @Enumerated(EnumType.STRING) // Mapea el Enum a un String en la BD
    @Column(name = "visibility", nullable = false) // Asegúrate de que no pueda ser nulo
    private VisibilityStatus visibilidad; // Cambiado a VisibilityStatus

    @JoinColumn(name = "subjectsID")
    @ManyToOne()
    private Subject subject;
    
    @JoinColumn(name = "usersID") // Esta es la columna para el creador del recurso
    @ManyToOne()
    private User user; // El usuario que subió el recurso

    @OneToMany(mappedBy = "resource", cascade = {CascadeType.REMOVE, CascadeType.MERGE}, orphanRemoval = true) // Añadido orphanRemoval para Ratings
    private List<Ratings> ratings;

    @OneToMany(mappedBy = "resource", cascade={CascadeType.REMOVE, CascadeType.MERGE}, orphanRemoval = true) // Añadido orphanRemoval para Reports
    private List<Report> report;

    //Constructores
    public Resource() {
    }      

    public Resource(Long id) {
        this.id = id;
    }

    // Constructor con todos los campos incluyendo visibilidad (útil para la creación)
    public Resource(String titulo, String descripcion, String tipoDoc, String url, LocalDate fechaCreacion, VisibilityStatus visibilidad, Subject subject, User user) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.tipoDoc = tipoDoc;
        this.url = url;
        this.fechaCreacion = fechaCreacion;
        this.visibilidad = visibilidad;
        this.subject = subject;
        this.user = user;
    }

    //Get & Set

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipoDoc() {
        return tipoDoc;
    }

    public void setTipoDoc(String tipoDoc) {
        this.tipoDoc = tipoDoc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Ratings> getRatings() {
        return ratings;
    }

    public void setRatings(List<Ratings> ratings) {
        this.ratings = ratings;
    }    

    // Getter y Setter para visibilidad, usando el tipo Enum
    public VisibilityStatus getVisibilidad() {
        return visibilidad;
    }

    public void setVisibilidad(VisibilityStatus visibilidad) {
        this.visibilidad = visibilidad;
    }

    public List<Report> getReport() {
        return report;
    }

    public void setReport(List<Report> report) {
        this.report = report;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    //Hast & Equals
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Resource other = (Resource) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }      
}


/*
package com.gt11.RECUVA.Resources;

import java.time.LocalDate;
import java.util.List;

import com.gt11.RECUVA.Ratings.Ratings;
import com.gt11.RECUVA.Reports.Report;
import com.gt11.RECUVA.Subjects.Subject;
import com.gt11.RECUVA.Users.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "resource")
public class Resource {

    @Id
    @Column(name ="resourceID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String titulo;

    @Column(name = "description")
    private String descripcion;

    @Column(name = "type")
    private String tipoDoc;

    @Column(name= "url")
    private String url;

    @Column(name = "create_at")
    private LocalDate fechaCreacion;

    @Column(name = "visibility")
    private Enum visibilidad;

    @JoinColumn(name = "subjectsID")
    @ManyToOne()
    private Subject subject;
    
    @JoinColumn(name = "usersID")
    @ManyToOne()
    private User user;

    @OneToMany(mappedBy = "resource", cascade = {CascadeType.REMOVE, CascadeType.MERGE})
    private List<Ratings> ratings;

    @OneToMany(mappedBy = "resource", cascade={CascadeType.REMOVE, CascadeType.MERGE}) 
    private List<Report> report;

    //Constructores
    public Resource() {
    }    

    public Resource(Long id) {
        this.id = id;
    }

    //Get & Set

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipoDoc() {
        return tipoDoc;
    }

    public void setTipoDoc(String tipoDoc) {
        this.tipoDoc = tipoDoc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    
    public List<Ratings> getRatings() {
        return ratings;
    }

    public void setRatings(List<Ratings> ratings) {
        this.ratings = ratings;
    }  

    

    public Enum getVisibilidad() {
        return visibilidad;
    }

    public void setVisibilidad(Enum visibilidad) {
        this.visibilidad = visibilidad;
    }

    public List<Report> getReport() {
        return report;
    }

    public void setReport(List<Report> report) {
        this.report = report;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    //Hast & Equals

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Resource other = (Resource) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

     
}
*/