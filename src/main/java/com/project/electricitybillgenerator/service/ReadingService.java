package com.project.electricitybillgenerator.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import static java.util.Calendar.MONTH;
import java.util.Date;

import com.project.electricitybillgenerator.repository.ReadingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReadingService {
    private final ReadingRepository readingRepository;

    public ReadingService(ReadingRepository readingRepository) {
        this.readingRepository = readingRepository;
    }

    // Rates of Unit and GST
    public double unitRate = 7.5;
    public double gstRate = 18;

    // Get previous month i.e., current month - 1
    public Date previousDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(MONTH, -1);
        Date previousDate = cal.getTime();
        return previousDate;
    }

    // Get the previous month reading from previousDate
    public double previousMonthReading(int meterId, Date date) {
        var previousReading = readingRepository.previousReading(meterId, previousDate(date));
        if (!previousReading.isEmpty()) {
            return previousReading.getFirst();
        } else {
            // When the list is empty
            return 0;
        }
    }

    public double calculateBill(double currentMonthReading, double previousMonthReading) {
        var bill = (currentMonthReading - previousMonthReading) * unitRate;
        var gst = (gstRate/100.0) * bill;
        var totalBill = bill + gst;
        return totalBill;
    }

    @Transactional // because error: Executing an update/delete query
    public ResponseEntity<String> deleteReadingWithDate(int meterId, String date) {
        readingRepository.deleteReadingByMeterIdAndDate(meterId, date);
        return ResponseEntity.ok("Deleted reading of " + date + "!");
    }
}
