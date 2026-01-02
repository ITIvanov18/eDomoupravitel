package nbu.edomoupravitel.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = true, length = 10)
    private String phoneNumber;

    // не се зареждат всички данни за компанията,
    // освен ако не бъдат повикани експлицитно
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Company company;

    @OneToMany(mappedBy = "employee")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude // предотвратява безкрайна рекурсия (StackOverflow) при двупосочни връзки
    @Builder.Default  // защита от NullPointerException
    private List<Building> buildings = new ArrayList<>();
}
