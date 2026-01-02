package nbu.edomoupravitel.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // unique = true гарантира, че няма да има две фирми с едно и също име в базата
    @Column(nullable = false, unique = true)
    private String name;

    // orphanRemoval = true: каквото се изтрие от тук се трие и от базата
    // CascadeType.ALL: Всички CRUD операции по фирмата се предават на служителите
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude // предотвратява безкрайна рекурсия (StackOverflow) при двупосочни връзки
    @Builder.Default // защита от NullPointerException
    private List<Employee> employees = new ArrayList<>();


    @Column(nullable = false)
    @Builder.Default
    private Double taxPerSqM = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Double elevatorTax = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Double petTax = 0.0;
}
