package br.ueg.tc.ueg_provider.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class UtilsService {

    public static String getCurrentFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate currentDate = LocalDate.now();
        return currentDate.format(formatter);
    }

}
