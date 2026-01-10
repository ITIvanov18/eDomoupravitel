package nbu.edomoupravitel.service.impl;

import lombok.RequiredArgsConstructor;
import nbu.edomoupravitel.dto.PaymentDto;
import nbu.edomoupravitel.entity.Apartment;
import nbu.edomoupravitel.entity.MonthlyFee;
import nbu.edomoupravitel.entity.Payment;
import nbu.edomoupravitel.exception.LogicOperationException;
import nbu.edomoupravitel.exception.ResourceNotFoundException;
import nbu.edomoupravitel.repository.ApartmentRepository;
import nbu.edomoupravitel.repository.MonthlyFeeRepository;
import nbu.edomoupravitel.repository.PaymentRepository;
import nbu.edomoupravitel.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ApartmentRepository apartmentRepository;
    private final MonthlyFeeRepository monthlyFeeRepository;

    @Override
    @Transactional
    public void createPayment(PaymentDto paymentDto) {
        Apartment apartment = apartmentRepository.findById(paymentDto.getApartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found"));

        // --- НОВА ЗАЩИТА ---
        // проверява дали изобщо има какво да се плаща
        List<MonthlyFee> unpaidFees = monthlyFeeRepository.findByApartmentIdAndIsPaidFalse(apartment.getId());

        if (unpaidFees.isEmpty()) {
            throw new LogicOperationException(
                    "Всички такси за апартамент №" + apartment.getApartmentNumber()
                            + " вече са платени! Плащането е отказано.");
        }

        // сортира: първо старите дългове
        unpaidFees.sort(Comparator.comparingInt(MonthlyFee::getYear)
                .thenComparingInt(MonthlyFee::getMonth));

        // създава плащането само ако е дължимо
        Payment payment = new Payment();
        payment.setApartment(apartment);
        payment.setAmount(paymentDto.getAmount());
        payment.setPaymentDate(LocalDate.now());
        paymentRepository.save(payment);

        // алгоритъм за покриване
        BigDecimal remainingMoney = paymentDto.getAmount();

        for (MonthlyFee fee : unpaidFees) {
            if (remainingMoney.compareTo(fee.getAmount()) >= 0) {
                fee.setPaid(true);
                monthlyFeeRepository.save(fee);
                remainingMoney = remainingMoney.subtract(fee.getAmount());
            } else {
                break;
            }
        }
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
        sb.append("Company,Employee,Building,Floor,Apartment,Amount,Date\n");

        // форматер за датата (dd.MM.yyyy)
        DateTimeFormatter dateFormatter =
        DateTimeFormatter.ofPattern("dd.MM.yyyy");

        for (Payment payment : payments) {
            String companyName = "N/A";
            String employeeName = "N/A";
            String buildingAddress = "N/A";
            int aptNumber = 0;
            int floor = 0;

            // defensive coding: проверява всяко ниво за null, за да избегне NullPointerException
            if (payment.getApartment() != null) {
                aptNumber = payment.getApartment().getApartmentNumber();
                floor = payment.getApartment().getFloor();

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
            // форматиране на датата
            String formattedDate = payment.getPaymentDate() != null ?
                    payment.getPaymentDate().format(dateFormatter) : "N/A";

            // форматира реда с %.2f за 2 знака след запетаята
            sb.append(String.format("%s,%s,%s,%d,%d,%.2f,%s\n",
                    escapeSpecialCharacters(companyName), // Добра практика: ескейпване на запетаи в имената
                    escapeSpecialCharacters(employeeName),
                    escapeSpecialCharacters(buildingAddress),
                    floor,
                    aptNumber,
                    payment.getAmount(),
                    formattedDate
            ));
        }

        try {
            // BOM (Byte Order Mark) за Excel, за да разчете кирилицата правилно
            byte[] bom = {(byte)0xEF, (byte)0xBB, (byte)0xBF};
            byte[] content = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);

            // обединяват се BOM + съдържанието
            byte[] finalBytes = new byte[bom.length + content.length];
            System.arraycopy(bom, 0, finalBytes, 0, bom.length);
            System.arraycopy(content, 0, finalBytes, bom.length, content.length);

            Files.write(Paths.get(filePath), finalBytes);
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
