package nbu.edomoupravitel.service.impl;

import lombok.RequiredArgsConstructor;
import nbu.edomoupravitel.dto.CompanyDto;
import nbu.edomoupravitel.dto.TreasuryReportDto;
import nbu.edomoupravitel.entity.Apartment;
import nbu.edomoupravitel.entity.MonthlyFee;
import nbu.edomoupravitel.repository.ApartmentRepository;
import nbu.edomoupravitel.repository.MonthlyFeeRepository;
import nbu.edomoupravitel.repository.PaymentRepository;
import nbu.edomoupravitel.service.ApartmentService;
import nbu.edomoupravitel.service.CompanyService;
import nbu.edomoupravitel.service.TreasuryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TreasuryServiceImpl implements TreasuryService {

    private final MonthlyFeeRepository monthlyFeeRepository;
    private final ApartmentRepository apartmentRepository;
    private final ApartmentService apartmentService;
    private final CompanyService companyService;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public void generateMonthlyFees(int month, int year) {

        List<Apartment> apartments = apartmentRepository.findAll();

        for (Apartment apartment : apartments) {
            // проверка дали ТОЗИ апартамент вече има такса
            if (monthlyFeeRepository.existsByApartmentAndMonthAndYear(apartment, month, year)) {
                continue; // ако има -> прескача го и продължава към следващия
            }

            // ако няма -> смята я и я записва
            double feeAmount = apartmentService.calculateMonthlyFee(apartment.getId());

            // генерира такса само ако сумата е положителна
            if (feeAmount > 0) {
                MonthlyFee fee = MonthlyFee.builder()
                        .apartment(apartment)
                        .month(month)
                        .year(year)
                        .amount(BigDecimal.valueOf(feeAmount))
                        .isPaid(false)
                        .build();
                monthlyFeeRepository.save(fee);
            }
        }
    }

    @Override
    public TreasuryReportDto getTreasuryReport() {
        BigDecimal globalUnpaid = BigDecimal.ZERO;
        BigDecimal globalPaid = BigDecimal.ZERO;

        List<TreasuryReportDto.CompanyReportRow> rows = new ArrayList<>();

        // итерира през компаниите, за да сметне техните баланси
        List<CompanyDto> companies = companyService.getAllCompanies();

        for (CompanyDto company : companies) {
            // взима колко пари е ПОИСКАЛА фирмата (всички такси)
            BigDecimal totalFees = monthlyFeeRepository.sumTotalFeesByCompany(company.getId());
            if (totalFees == null) totalFees = BigDecimal.ZERO;

            // взима колко пари е ПОЛУЧИЛА фирмата (всички плащания)
            BigDecimal totalPayments = paymentRepository.sumTotalPaymentsByCompany(company.getId());
            if (totalPayments == null) totalPayments = BigDecimal.ZERO;

            // дължимата сума е разликата им
            BigDecimal unpaid = totalFees.subtract(totalPayments);

            BigDecimal paid = totalPayments;

            globalUnpaid = globalUnpaid.add(unpaid);
            globalPaid = globalPaid.add(paid);

            rows.add(new TreasuryReportDto.CompanyReportRow(company.getName(), unpaid, paid));
        }

        // логика са просрочено плащане
        List<TreasuryReportDto.OverdueInfo> overdueList = new ArrayList<>();
        LocalDate today = LocalDate.now();
        int deadlineDay = 5; // срок за плащане (5-то число)

        // взима всички неплатени такси от базата
        List<MonthlyFee> allUnpaid = monthlyFeeRepository.findAll().stream()
                .filter(fee -> !fee.isPaid())
                .toList();

        for (MonthlyFee fee : allUnpaid) {
            boolean isLate = isLate(fee, today, deadlineDay);

            if (isLate) {
                String aptInfo = "Ap. " + fee.getApartment().getApartmentNumber() +
                        " (Floor " + fee.getApartment().getFloor() + ")";
                if (fee.getApartment().getBuilding() != null) {
                    aptInfo = fee.getApartment().getBuilding().getAddress() + ", " + aptInfo;
                }
                overdueList.add(new TreasuryReportDto.OverdueInfo(
                        aptInfo,
                        fee.getMonth() + "/" + fee.getYear(),
                        fee.getAmount()));
            }
        }

        return TreasuryReportDto.builder()
                .totalUnpaidAmount(globalUnpaid)
                .totalPaidAmount(globalPaid)
                .companyRows(rows)
                .overdueApartments(overdueList)
                .build();
    }

    private static boolean isLate(MonthlyFee fee, LocalDate today, int deadlineDay) {
        boolean isLate = false;

        // ако е от минала година -> ЗАКЪСНЯЛ
        if (fee.getYear() < today.getYear()) {
            isLate = true;
        }
        // ако е от тази година, но минал месец -> ЗАКЪСНЯЛ
        else if (fee.getYear() == today.getYear() && fee.getMonth() < today.getMonthValue()) {
            isLate = true;
        }
        // ако е от текущия месец, но е минало 5-то число -> ЗАКЪСНЯЛ
        else if (fee.getYear() == today.getYear() && fee.getMonth() == today.getMonthValue()
                && today.getDayOfMonth() > deadlineDay) {
            isLate = true;
        }
        return isLate;
    }
}
