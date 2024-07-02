package ro.ase.ism.clientapp.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Bank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String name;
}
