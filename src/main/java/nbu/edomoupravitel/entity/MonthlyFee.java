package nbu.edomoupravitel.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "monthly_fees", uniqueConstraints = {
        // защита да няма 2 такси за 1 месец
        @UniqueConstraint(columnNames = {"apartment_id", "month", "year"})
})
public class MonthlyFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;

    @Column(nullable = false)
    private int month;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private BigDecimal amount; // сумата, която е била актуална към момента на начисляване

    @Column(nullable = false)
    private boolean isPaid;
}
