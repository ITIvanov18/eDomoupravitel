package nbu.edomoupravitel.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate paymentDate;

    // тук се пази одит на стари плащания
    @Column(name = "audit_details")
    private String auditDetails;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude // предотвратява безкрайна рекурсия (StackOverflow) при двупосочни връзки
    private Apartment apartment;
}
