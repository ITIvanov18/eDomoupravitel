package nbu.edomoupravitel.service.impl;

import lombok.RequiredArgsConstructor;
import nbu.edomoupravitel.dto.CompanyDto;
import nbu.edomoupravitel.dto.TreasuryReportDto;
import nbu.edomoupravitel.entity.Apartment;
import nbu.edomoupravitel.entity.MonthlyFee;
import nbu.edomoupravitel.repository.ApartmentRepository;
import nbu.edomoupravitel.repository.CompanyRepository;
import nbu.edomoupravitel.repository.MonthlyFeeRepository;
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
    private final CompanyRepository companyRepository;

    @Override
    @Transactional
    public void generateMonthlyFees(int month, int year) {
        if (monthlyFeeRepository.existsByMonthAndYear(month, year)) {
            throw new RuntimeException("Fees for " + month + "/" + year + " are already generated!");
        }

        List<Apartment> apartments = apartmentRepository.findAll();
        for (Apartment apartment : apartments) {
            double feeAmount = apartmentService.calculateMonthlyFee(apartment.getId());
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

        // 1. Итерираме през компаниите, за да сметнем техните баланси
        List<CompanyDto> companies = companyService.getAllCompanies();

        for (CompanyDto company : companies) {
            // Unpaid sum via repository query
            BigDecimal unpaid = monthlyFeeRepository.sumUnpaidByCompany(company.getId());
            if (unpaid == null)
                unpaid = BigDecimal.ZERO;

            // Paid sum via repository query (трябва да добавим този метод в
            // MonthlyFeeRepository или да го сметнем тук)
            // Тъй като нямаме директен paid метод в MonthlyFeeRepository все още, нека
            // добавим проста логика:
            // Всъщност, по-добре да го извлечем. Засега ще ползваме stream филтър ако няма
            // Query.
            // Но за по-лесно: нека допуснем, че имаме платените = (Total - Unpaid)? Не
            // съвсем.
            // Нека ползваме native query или stream подход върху всички такси на
            // компанията.
            // За оптимизация, ще добавим sumPaidByCompany в репото по-късно.
            // Сега ще ползваме stream approach (по-бавен, но работи):

            // Временна логика (може да се оптимизира):
            // Взимаме всички такси за компанията? Това е тежко.
            // Нека просто оставим 0.00 или направим нова заявка в репото...
            // За да не пипаме репото сега, ще оставим 0 или ще ползваме съществуващото.
            // В User Request-а имаше заявка само за Unpaid. Нека добавим simple method
            BigDecimal paid = monthlyFeeRepository.findAll().stream()
                    .filter(mf -> mf.getApartment().getBuilding().getEmployee().getCompany().getId()
                            .equals(company.getId()))
                    .filter(MonthlyFee::isPaid)
                    .map(MonthlyFee::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            globalUnpaid = globalUnpaid.add(unpaid);
            globalPaid = globalPaid.add(paid);

            rows.add(new TreasuryReportDto.CompanyReportRow(company.getName(), unpaid, paid));
        }

        // --- НОВА ЛОГИКА ЗА ЗАКЪСНЕНИЯ ---
        List<TreasuryReportDto.OverdueInfo> overdueList = new ArrayList<>();
        LocalDate today = LocalDate.now();
        int deadlineDay = 5; // Срок за плащане

        // Взимаме всички неплатени такси от базата
        List<MonthlyFee> allUnpaid = monthlyFeeRepository.findAll().stream()
                .filter(fee -> !fee.isPaid())
                .toList();

        for (MonthlyFee fee : allUnpaid) {
            boolean isLate = false;

            // Ако е от минала година -> ЗАКЪСНЯЛ
            if (fee.getYear() < today.getYear()) {
                isLate = true;
            }
            // Ако е от тази година, но минал месец -> ЗАКЪСНЯЛ
            else if (fee.getYear() == today.getYear() && fee.getMonth() < today.getMonthValue()) {
                isLate = true;
            }
            // Ако е от текущия месец, но е минало 5-то число -> ЗАКЪСНЯЛ
            else if (fee.getYear() == today.getYear() && fee.getMonth() == today.getMonthValue()
                    && today.getDayOfMonth() > deadlineDay) {
                isLate = true;
            }

            if (isLate) {
                String aptInfo = "Ap. " + fee.getApartment().getApartmentNumber() +
                        " (Floor " + fee.getApartment().getFloor() + ")";
                // Добавяме и адреса на сградата за яснота
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
                .overdueApartments(overdueList) // <--- ПЪХАМЕ ГИ ТУК
                .build();
    }
}
