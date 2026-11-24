
package com.smartfin.smartfin.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.smartfin.smartfin.dto.TransactionResponse;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class ReceiptService {

    public byte[] generateReceipt(TransactionResponse transaction) {

        try {
            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            Document document = new Document();

            PdfWriter.getInstance(document, outputStream);

            document.open();

            Font titleFont = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    22,
                    new Color(37, 99, 235)
            );

            Font headingFont = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    12
            );

            Font normalFont = FontFactory.getFont(
                    FontFactory.HELVETICA,
                    11
            );

            Paragraph title = new Paragraph("SMARTFIN", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph(
                    "Transaction Receipt",
                    normalFont
            );

            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);

            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.2f, 2f});

            addRow(table, "Reference Number",
                    transaction.getReferenceNumber(),
                    headingFont, normalFont);

            addRow(table, "Transaction Type",
                    transaction.getTransactionType(),
                    headingFont, normalFont);

            addRow(table, "Amount",
                    "INR " + transaction.getAmount(),
                    headingFont, normalFont);

            addRow(table, "Status",
                    transaction.getStatus(),
                    headingFont, normalFont);

            addRow(table, "Description",
                    transaction.getDescription() == null
                            ? "N/A"
                            : transaction.getDescription(),
                    headingFont, normalFont);

            String date = transaction.getCreatedAt() == null
                    ? "N/A"
                    : transaction.getCreatedAt().format(
                    DateTimeFormatter.ofPattern(
                            "dd-MM-yyyy HH:mm:ss XXX"
                    )
            );

            addRow(table, "Date & Time",
                    date,
                    headingFont, normalFont);

            document.add(table);

            document.add(new Paragraph(" "));

            Paragraph footer = new Paragraph(
                    "This is a system-generated receipt.",
                    normalFont
            );

            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

            return outputStream.toByteArray();

        } catch (Exception exception) {
            throw new RuntimeException(
                    "Unable to generate transaction receipt.",
                    exception
            );
        }
    }

    private void addRow(
            PdfPTable table,
            String label,
            String value,
            Font headingFont,
            Font normalFont
    ) {

        PdfPCell labelCell = new PdfPCell(
                new Phrase(label, headingFont)
        );

        PdfPCell valueCell = new PdfPCell(
                new Phrase(value == null ? "N/A" : value, normalFont)
        );

        labelCell.setPadding(10);
        valueCell.setPadding(10);

        labelCell.setBackgroundColor(
                new Color(241, 245, 249)
        );

        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}
