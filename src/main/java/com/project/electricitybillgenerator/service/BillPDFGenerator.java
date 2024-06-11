package com.project.electricitybillgenerator.service;

import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.border.SolidBorder;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.project.electricitybillgenerator.model.BillReading;
import com.project.electricitybillgenerator.model.BillUser;
import com.project.electricitybillgenerator.repository.ReadingRepository;
import com.project.electricitybillgenerator.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

@Service
public class BillPDFGenerator {
    private final  UserRepository userRepository;
    private final  ReadingRepository readingRepository;

    public BillPDFGenerator(UserRepository userRepository, ReadingRepository readingRepository) {
        this.userRepository = userRepository;
        this.readingRepository = readingRepository;
    }

    public String statement(BillReading allReading) throws FileNotFoundException {
        ReadingService constants = new ReadingService(readingRepository);
        int meterId = allReading.getMeterId();
        String date = allReading.getDate();
        Optional<BillUser> user = userRepository.findByMeterId(meterId);
        Optional<BillReading> userData = readingRepository.findByMeterId(meterId, date);
        System.out.println(meterId);

        String name;
        double currentReading;
        double previousReading;
        double unitsConsumed;
        double unitRate;
        double billAmount;
        double gst;
        double billWithoutGST;
        String address;
        String email;

        if (user.isPresent() && userData.isPresent()) {
            currentReading = userData.get().getCurrentMonthReading();
            previousReading = userData.get().getPreviousMonthReading();
            unitsConsumed = userData.get().getUnitConsumed();
            unitRate = constants.unitRate;
            billAmount = userData.get().getBillAmount();
            gst = billAmount - ((currentReading - previousReading) * unitRate);
            billWithoutGST = billAmount - gst;

            name = user.get().getName();
            address = user.get().getAddress();
            email = user.get().getEmail();

            // Rest of your code...
            String path = String.format("C:\\Users\\itsaa\\Downloads\\%d-bill-%s.pdf", meterId, date);
            PdfWriter pdfWriter = new PdfWriter(path);
            PdfDocument pdfdoc = new PdfDocument(pdfWriter);
            pdfdoc.setDefaultPageSize(PageSize.A4);
            Document document = new Document(pdfdoc);

            document.add(new Paragraph("Electricity Bill")
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(35));

            Border bd = new SolidBorder(Color.GRAY, 1);
            Table divider1 = new Table(100);
            divider1.setBorder(bd);
            document.add(divider1);

            String p1 = String.format("""
                        Bill number: %s
                        Date: %s
                        """, date + meterId,
                    LocalDate.now(ZoneId.of("Asia/Kolkata")));

            String p2 = String.format("""
                Name: %s
                Address: %s
                Email: %s
                Meter ID: %d
                """, name, address, email, meterId);

            String p3 = String.format("""
                        Bill for month: %s
                        This month reading: %.2f
                        Last month reading: %.2f
                        Units Consumed: %.2f
                        Unit Price: %.2f
                        Bill: %.2f
                        18%% GST: %.2f
                        """,
                    date, currentReading, previousReading, unitsConsumed, unitRate,
                    billWithoutGST, gst);

            String p4 = String.format("Payable Amount: %.2f",
                    billAmount);

            document.add(new Paragraph(p1));
            document.add(divider1);
            document.add(new Paragraph(p2));
            document.add(divider1);
            document.add(new Paragraph(p3));
            document.add(divider1);
            document.add(new Paragraph(p4));
            document.close();
            return "Saved successfully to " + path;
        } else {
            // Handle the case where the user or userData isn't present...
            return "Sorry";
        }
    }
}