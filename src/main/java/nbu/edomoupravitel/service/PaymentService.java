package nbu.edomoupravitel.service;

import nbu.edomoupravitel.dto.PaymentDto;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {
    PaymentDto payFee(Long apartmentId, BigDecimal amount);

    List<PaymentDto> getAllPayments();
    // експортира всички плащания в CSV файл
    void exportPaidFeesToFile(String filePath);
}
