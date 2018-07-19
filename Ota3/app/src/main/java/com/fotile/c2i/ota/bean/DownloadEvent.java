package com.fotile.c2i.ota.bean;

/**
 * @author ： panyw .
 * @date ：2018/2/1 18:14
 * @COMPANY ： Fotile智能厨电研究院
 * @description ：
 */

public class DownloadEvent {
    private String download_state;
    private String tips;

    public DownloadEvent(){}

    public DownloadEvent(String state,String tips){
        this.download_state = state;
        this.tips = tips;
    }

    public String getDownload_state() {
        return download_state;
    }

    public void setDownload_state(String download_state) {
        this.download_state = download_state;
    }

    public String getError_tips() {
        return tips;
    }

    public void setError_tips(String tips) {
        this.tips = tips;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DownloadEvent{");
        sb.append("download_state='").append(download_state).append('\'');
        sb.append(", tips='").append(tips).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
