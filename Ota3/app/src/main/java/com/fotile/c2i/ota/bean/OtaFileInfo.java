package com.fotile.c2i.ota.bean;

import com.dl7.downloaderlib.entity.FileInfo;

/**
 * Created by yaohx on 2017/12/25.
 */

public class OtaFileInfo {
    public FileInfo fileInfo;
    public String errorMsg;

    public OtaFileInfo(FileInfo fileInfo, String errorMsg) {
        this.fileInfo = fileInfo;
        this.errorMsg = errorMsg;
    }
}
