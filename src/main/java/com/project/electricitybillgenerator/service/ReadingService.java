package com.project.electricitybillgenerator.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import static java.util.Calendar.MONTH;
import java.util.Date;

import com.project.electricitybillgenerator.model.BillReading;
import org.springframework.beans.factory.annotation.Autowired;

import com.project.electricitybillgenerator.repository.ReadingRepository;
import org.springframework.stereotype.Service;

@Service
public class ReadingService {
    private final ReadingRepository readingRepository;

    public ReadingService(ReadingRepository readingRepository) {
        this.readingRepository = readingRepository;
    }

    public Date previousDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(MONTH, -1);
        Date previousDate = cal.getTime();
        System.out.println(sdf.format(date));
        System.out.println(sdf.format(previousDate));

        return previousDate;
    }

    // SQL query to get the previous month reading from previousDate
    public double previousMonthReading(int meter_id, Date date) {
        var previousReading = readingRepository.previousReading(meter_id, previousDate(date));
        if (!previousReading.isEmpty()) {
            return previousReading.getFirst();
        } else {
            // Handle the case when the list is empty
            // For example, return a default value or throw a custom exception
            return 0;
        }
    }
}
