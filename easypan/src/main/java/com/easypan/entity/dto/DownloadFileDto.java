package com.easypan.entity.dto;

//import lombok.Data;

//@Data
public class DownloadFileDto {
    private String downloadCode;
    private String fileName;
//    private String fileID;

    public String getDownloadCode() {
        return downloadCode;
    }

    public void setDownloadCode(String downloadCode) {
        this.downloadCode = downloadCode;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

//    public String getFileID() {
//        return fileID;
//    }
//
//    public void setFileID(String fileID) {
//        this.fileID = fileID;
//    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    private String filePath;
}
