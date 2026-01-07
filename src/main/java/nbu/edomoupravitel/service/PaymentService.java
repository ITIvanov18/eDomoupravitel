package nbu.edomoupravitel.service;

import nbu.edomoupravitel.dto.PaymentDto;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {
    void createPayment(PaymentDto paymentDto);

    List<PaymentDto> getAllPayments();
    // експортира всички плащания в CSV файл
    void exportPaidFeesToFile(String filePath);
}
