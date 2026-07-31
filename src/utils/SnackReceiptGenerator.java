package utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import models.SnackSale;
import models.SnackSaleItem;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.math.BigDecimal;

public class SnackReceiptGenerator {

    public static void generateAndOpenReceipt(SnackSale sale, List<SnackSaleItem> items) {
        // Disabled per user request - no saving or generating receipts
    }
}
