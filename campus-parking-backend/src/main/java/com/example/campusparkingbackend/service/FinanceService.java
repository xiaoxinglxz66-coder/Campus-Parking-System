package com.example.campusparkingbackend.service;

import com.example.campusparkingbackend.entity.ParkingRecord;
import com.example.campusparkingbackend.repository.ParkingRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FinanceService {

    @Autowired
    private ParkingRecordRepository parkingRecordRepository;

    /**
     * 获取财务概览
     */
    public FinanceOverview getFinanceOverview() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.atTime(LocalTime.MAX);

        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDateTime startOfMonth = firstDayOfMonth.atStartOfDay();

        LocalDate firstDayOfYear = today.withDayOfYear(1);
        LocalDateTime startOfYear = firstDayOfYear.atStartOfDay();

        // 今日收入（只计算校外用户）
        BigDecimal todayRevenue = parkingRecordRepository.calculateExternalUserRevenueByDateRange(
                startOfToday, endOfToday);
        if (todayRevenue == null) todayRevenue = BigDecimal.ZERO;

        // 本月收入
        BigDecimal monthRevenue = parkingRecordRepository.calculateExternalUserRevenueByDateRange(
                startOfMonth, endOfToday);
        if (monthRevenue == null) monthRevenue = BigDecimal.ZERO;

        // 年度收入
        BigDecimal yearRevenue = parkingRecordRepository.calculateExternalUserRevenueByDateRange(
                startOfYear, endOfToday);
        if (yearRevenue == null) yearRevenue = BigDecimal.ZERO;

        // 今日停车次数（校外用户）
        long todayParkingCount = parkingRecordRepository.countExternalUserRecordsByDateRange(
                startOfToday, endOfToday);

        // 当前正在停车的校外用户数量
        long currentParkingCount = parkingRecordRepository.countCurrentExternalUserParking();

        return new FinanceOverview(todayRevenue, monthRevenue, yearRevenue,
                todayParkingCount, currentParkingCount);
    }

    /**
     * 获取收入趋势
     */
    public List<DailyRevenue> getRevenueTrend(LocalDate startDate, LocalDate endDate) {
        List<DailyRevenue> trend = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            LocalDateTime startOfDay = currentDate.atStartOfDay();
            LocalDateTime endOfDay = currentDate.atTime(LocalTime.MAX);

            BigDecimal dailyRevenue = parkingRecordRepository.calculateExternalUserRevenueByDateRange(
                    startOfDay, endOfDay);
            if (dailyRevenue == null) dailyRevenue = BigDecimal.ZERO;

            long dailyCount = parkingRecordRepository.countExternalUserRecordsByDateRange(
                    startOfDay, endOfDay);

            trend.add(new DailyRevenue(currentDate, dailyRevenue, dailyCount));
            currentDate = currentDate.plusDays(1);
        }

        return trend;
    }

    /**
     * 获取校外用户收费记录（分页+筛选）
     */
    public FinanceRecordPage getExternalUserRecords(int page, int size, String plateNumber,
                                                    LocalDate startDate, LocalDate endDate) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startTime"));

        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        if (startDate != null) {
            startDateTime = startDate.atStartOfDay();
        }
        if (endDate != null) {
            endDateTime = endDate.atTime(LocalTime.MAX);
        }

        Page<ParkingRecord> recordsPage = parkingRecordRepository.findExternalUserRecordsWithFilters(
                plateNumber, startDateTime, endDateTime, pageable);

        // 转换为DTO
        List<FinanceRecordDTO> recordDTOs = new ArrayList<>();
        for (ParkingRecord record : recordsPage.getContent()) {
            recordDTOs.add(new FinanceRecordDTO(record));
        }

        return new FinanceRecordPage(recordDTOs, recordsPage.getTotalElements(),
                recordsPage.getTotalPages(), recordsPage.getNumber());
    }

    /**
     * 获取月度统计
     */
    public MonthlyStats getMonthlyStats(int year, int month) {
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());

        LocalDateTime startOfMonth = firstDayOfMonth.atStartOfDay();
        LocalDateTime endOfMonth = lastDayOfMonth.atTime(LocalTime.MAX);

        // 月度总收入
        BigDecimal monthlyRevenue = parkingRecordRepository.calculateExternalUserRevenueByDateRange(
                startOfMonth, endOfMonth);
        if (monthlyRevenue == null) monthlyRevenue = BigDecimal.ZERO;

        // 月度总停车次数
        long monthlyParkingCount = parkingRecordRepository.countExternalUserRecordsByDateRange(
                startOfMonth, endOfMonth);

        // 平均每次停车费用
        BigDecimal averageFee = monthlyParkingCount > 0 ?
                monthlyRevenue.divide(BigDecimal.valueOf(monthlyParkingCount), 2, BigDecimal.ROUND_HALF_UP) :
                BigDecimal.ZERO;

        // 最活跃的日期
        LocalDate busiestDate = null;
        List<Object[]> busiestDates = parkingRecordRepository.findBusiestDateInMonth(year, month);
        if (!busiestDates.isEmpty()) {
            busiestDate = (LocalDate) busiestDates.get(0)[0];
        }

        return new MonthlyStats(monthlyRevenue, monthlyParkingCount, averageFee, busiestDate);
    }

    // ========== DTO 类定义 ==========

    public static class FinanceOverview {
        private final BigDecimal todayRevenue;
        private final BigDecimal monthRevenue;
        private final BigDecimal yearRevenue;
        private final long todayParkingCount;
        private final long currentParkingCount;

        public FinanceOverview(BigDecimal todayRevenue, BigDecimal monthRevenue, BigDecimal yearRevenue,
                               long todayParkingCount, long currentParkingCount) {
            this.todayRevenue = todayRevenue;
            this.monthRevenue = monthRevenue;
            this.yearRevenue = yearRevenue;
            this.todayParkingCount = todayParkingCount;
            this.currentParkingCount = currentParkingCount;
        }

        // Getters
        public BigDecimal getTodayRevenue() { return todayRevenue; }
        public BigDecimal getMonthRevenue() { return monthRevenue; }
        public BigDecimal getYearRevenue() { return yearRevenue; }
        public long getTodayParkingCount() { return todayParkingCount; }
        public long getCurrentParkingCount() { return currentParkingCount; }
    }

    public static class DailyRevenue {
        private final LocalDate date;
        private final BigDecimal revenue;
        private final long parkingCount;

        public DailyRevenue(LocalDate date, BigDecimal revenue, long parkingCount) {
            this.date = date;
            this.revenue = revenue;
            this.parkingCount = parkingCount;
        }

        // Getters
        public LocalDate getDate() { return date; }
        public BigDecimal getRevenue() { return revenue; }
        public long getParkingCount() { return parkingCount; }
    }

    public static class FinanceRecordDTO {
        private Long id;
        private String plateNumber;
        private String userName;
        private String userType;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private BigDecimal fee;
        private String status;
        private String parkingLotName;

        public FinanceRecordDTO(ParkingRecord record) {
            this.id = record.getId();
            this.plateNumber = record.getPlateNumber();
            this.userName = record.getUserName();
            this.userType = record.getUserType();
            this.startTime = record.getStartTime();
            this.endTime = record.getEndTime();
            this.fee = record.getFee();
            this.status = record.getStatus().name();
            this.parkingLotName = record.getParkingLotName();
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getPlateNumber() { return plateNumber; }
        public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public String getUserType() { return userType; }
        public void setUserType(String userType) { this.userType = userType; }
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        public BigDecimal getFee() { return fee; }
        public void setFee(BigDecimal fee) { this.fee = fee; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getParkingLotName() { return parkingLotName; }
        public void setParkingLotName(String parkingLotName) { this.parkingLotName = parkingLotName; }
    }

    public static class FinanceRecordPage {
        private final List<FinanceRecordDTO> records;
        private final long totalElements;
        private final int totalPages;
        private final int currentPage;

        public FinanceRecordPage(List<FinanceRecordDTO> records, long totalElements, int totalPages, int currentPage) {
            this.records = records;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.currentPage = currentPage;
        }

        // Getters
        public List<FinanceRecordDTO> getRecords() { return records; }
        public long getTotalElements() { return totalElements; }
        public int getTotalPages() { return totalPages; }
        public int getCurrentPage() { return currentPage; }
    }

    public static class MonthlyStats {
        private final BigDecimal totalRevenue;
        private final long totalParkingCount;
        private final BigDecimal averageFee;
        private final LocalDate busiestDate;

        public MonthlyStats(BigDecimal totalRevenue, long totalParkingCount,
                            BigDecimal averageFee, LocalDate busiestDate) {
            this.totalRevenue = totalRevenue;
            this.totalParkingCount = totalParkingCount;
            this.averageFee = averageFee;
            this.busiestDate = busiestDate;
        }

        // Getters
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public long getTotalParkingCount() { return totalParkingCount; }
        public BigDecimal getAverageFee() { return averageFee; }
        public LocalDate getBusiestDate() { return busiestDate; }
    }
}
