package nbu.edomoupravitel.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "residents")
public class Resident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private int age;

    private boolean usesElevator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude // предотвратява безкрайна рекурсия (StackOverflow) при двупосочни връзки
    private Apartment apartment;
}
