package nbu.edomoupravitel;

import nbu.edomoupravitel.dto.PaymentDto;
import nbu.edomoupravitel.entity.Apartment;
import nbu.edomoupravitel.entity.MonthlyFee;
import nbu.edomoupravitel.entity.Payment;
import nbu.edomoupravitel.exception.LogicOperationException;
import nbu.edomoupravitel.repository.ApartmentRepository;
import nbu.edomoupravitel.repository.MonthlyFeeRepository;
import nbu.edomoupravitel.repository.PaymentRepository;
import nbu.edomoupravitel.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServiceLayerTests {

    @Mock
    private ApartmentRepository apartmentRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private MonthlyFeeRepository monthlyFeeRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void testCreatePayment_VerifiesRepositoryCall() {
        // Given
        Long aptId = 1L;
        BigDecimal amount = new BigDecimal("50.00");

        // създава апартамент
        Apartment apt = new Apartment();
        apt.setId(aptId);
        apt.setApartmentNumber(12);

        // подготвя DTO-то
        PaymentDto dto = new PaymentDto();
        dto.setApartmentId(aptId);
        dto.setAmount(amount);

        // --- Mocking ---

        // намира на апартамента
        when(apartmentRepository.findById(aptId)).thenReturn(Optional.of(apt));

        // трябва да има неплатени такси, иначе логиката хвърля грешка!
        MonthlyFee fee = new MonthlyFee();
        fee.setAmount(new BigDecimal("20.00")); // Такса от 20 лв
        fee.setYear(2025);
        fee.setMonth(1);
        fee.setPaid(false);

        List<MonthlyFee> unpaidFees = new ArrayList<>();
        unpaidFees.add(fee);

        // когато сървисът пита за такси, връща списъка
        when(monthlyFeeRepository.findByApartmentIdAndIsPaidFalse(aptId))
                .thenReturn(unpaidFees);

        // симулация на успешно записване на плащането
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        // използвам assertDoesNotThrow, за да е сигурно, че няма грешки
        assertDoesNotThrow(() -> paymentService.createPayment(dto));

        // Then
        // проверка дали плащането е записано
        verify(paymentRepository, times(1)).save(any(Payment.class));

        // проверка дали таксата е маркирана като платена (понеже плащаме 50 лв, а таксата е 20)
        verify(monthlyFeeRepository, atLeastOnce()).save(any(MonthlyFee.class));
    }


    @Test
    void testCreatePayment_ThrowsException_WhenNoUnpaidFees() {
        // Given
        Long aptId = 2L;
        BigDecimal amount = new BigDecimal("50.00");

        Apartment apt = new Apartment();
        apt.setId(aptId);
        apt.setApartmentNumber(5);

        PaymentDto dto = new PaymentDto();
        dto.setApartmentId(aptId);
        dto.setAmount(amount);

        // --- MOCKING ---
        // апартаментът съществува
        when(apartmentRepository.findById(aptId)).thenReturn(Optional.of(apt));

        // връща празен списък (няма дългове)
        when(monthlyFeeRepository.findByApartmentIdAndIsPaidFalse(aptId))
                .thenReturn(new ArrayList<>());

        // When и Then
        // очаква се, че извикването на метода ще хвърли LogicOperationException
        assertThrows(LogicOperationException.class, () -> paymentService.createPayment(dto),
                "Трябва да хвърли грешка, ако няма неплатени такси!");

        // проверка, че НИЩО не е записано в PaymentRepository (защото е хвърлена грешка преди това)
        verify(paymentRepository, never()).save(any(Payment.class));
    }
}