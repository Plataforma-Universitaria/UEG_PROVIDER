package br.ueg.tc.ueg_provider.enums;

import java.nio.file.Path;
import java.nio.file.Paths;

public enum DocEnum {

    FREQUENCY_RECORD(Paths.get("frequency"),"record_"),

    BOND_RECORD(Paths.get("bonded"),"record_"),

    ACADEMIC_RECORD(Paths.get("academic"), "record_");

    private final Path folderPath;
    private final String filePrefix;

    DocEnum(final Path folderPath, final String filePrefix){
        this.folderPath = folderPath;
        this.filePrefix = filePrefix;
    }

    public Path getFolderPath() {
        return folderPath;
    }

    public String getFilePrefix() {
        return filePrefix;
    }
}
