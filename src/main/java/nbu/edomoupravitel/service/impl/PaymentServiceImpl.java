package nbu.edomoupravitel.service.impl;

import lombok.RequiredArgsConstructor;
import nbu.edomoupravitel.dto.PaymentDto;
import nbu.edomoupravitel.entity.Apartment;
import nbu.edomoupravitel.entity.Payment;
import nbu.edomoupravitel.exception.ResourceNotFoundException;
import nbu.edomoupravitel.repository.ApartmentRepository;
import nbu.edomoupravitel.repository.PaymentRepository;
import nbu.edomoupravitel.service.PaymentService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ApartmentRepository apartmentRepository;

    @Override
    public PaymentDto payFee(Long apartmentId, BigDecimal amount) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found with id: " + apartmentId));

        Payment payment = Payment.builder()
                .amount(amount)
                .paymentDate(LocalDate.now())
                .apartment(apartment)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        return PaymentDto.fromEntity(savedPayment);
    }

    @Override
    public List<PaymentDto> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(PaymentDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void exportPaidFeesToFile(String filePath) {
        List<Payment> payments = paymentRepository.findAll();
        StringBuilder sb = new StringBuilder();
        // CSV Header
        sb.append("Company, Employee, Building, Apartment, Amount, Date\n");

        for (Payment payment : payments) {
            String companyName = "N/A";
            String employeeName = "N/A";
            String buildingAddress = "N/A";
            int aptNumber = 0;

            // defensive coding: проверява всяко ниво за null, за да избегне NullPointerException
            if (payment.getApartment() != null) {
                aptNumber = payment.getApartment().getApartmentNumber();
                if (payment.getApartment().getBuilding() != null) {
                    buildingAddress = payment.getApartment().getBuilding().getAddress();
                    if (payment.getApartment().getBuilding().getEmployee() != null) {
                        employeeName = payment.getApartment().getBuilding().getEmployee().getFirstName() + " " +
                                payment.getApartment().getBuilding().getEmployee().getLastName();
                        if (payment.getApartment().getBuilding().getEmployee().getCompany() != null) {
                            companyName = payment.getApartment().getBuilding().getEmployee().getCompany().getName();
                        }
                    }
                }
            }

            // форматира реда с %.2f за 2 знака след запетаята
            sb.append(String.format("%s,%s,%s,%d,%.2f,%s\n",
                    escapeSpecialCharacters(companyName), // Добра практика: ескейпване на запетаи в имената
                    escapeSpecialCharacters(employeeName),
                    escapeSpecialCharacters(buildingAddress),
                    aptNumber,
                    payment.getAmount(),
                    payment.getPaymentDate()));
        }

        // използва UTF-8 изрично, за да поддържа кирилица
        try {
            Files.write(Paths.get(filePath), sb.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to export fees to file: " + e.getMessage(), e);
        }
    }

    // помощен метод за CSV (ако името съдържа запетая, чупи CSV формата)
    private String escapeSpecialCharacters(String data) {
        if (data == null) return "";
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }
}
