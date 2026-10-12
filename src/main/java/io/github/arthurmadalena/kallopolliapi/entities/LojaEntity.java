package io.github.arthurmadalena.kallopolliapi.entities;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "loja")
public class LojaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String lojaName;
    private String lojaCity;

    public LojaEntity(Long id, String lojaName, String lojaCity) {
        this.id = id;
        this.lojaName = lojaName;
        this.lojaCity = lojaCity;
    }

    public LojaEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLojaName() {
        return lojaName;
    }

    public void setLojaName(String lojaName) {
        this.lojaName = lojaName;
    }

    public String getLojaCity() {
        return lojaCity;
    }

    public void setLojaCity(String lojaCity) {
        this.lojaCity = lojaCity;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LojaEntity that = (LojaEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(lojaName, that.lojaName) && Objects.equals(lojaCity, that.lojaCity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, lojaName, lojaCity);
    }

    @Override
    public String toString() {
        return "LojaEntity{" +
                "id=" + id +
                ", lojaName='" + lojaName + '\'' +
                ", lojaCity='" + lojaCity + '\'' +
                '}';
    }

}
