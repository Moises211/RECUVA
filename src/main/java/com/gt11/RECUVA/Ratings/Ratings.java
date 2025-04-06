package com.gt11.RECUVA.Ratings;

import com.gt11.RECUVA.Resources.Resource;
import com.gt11.RECUVA.Users.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ratings")
public class Ratings {

    @Id
    @Column(name="ratingsID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Si tienen problema con Byte cambien el tipo a Short
    @Column(name = "rating")
    private Byte calificacion;

    @JoinColumn(name = "userID")
    @ManyToOne()
    private User user;

    @JoinColumn(name = "resourceID")
    @ManyToOne()
    private Resource resource;

    // Constructs
    public Ratings() {
    }

    public Ratings(Long id) {
        this.id = id;
    }

    // GET & SET
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Byte getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(Byte calificacion) {
        this.calificacion = calificacion;
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

    // HASH & EQUALS
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
        Ratings other = (Ratings) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
