package com.fotile.c2i.ota.service;

import com.fotile.c2i.ota.bean.OtaFileInfo;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by yaohx on 2017/12/25.
 */

public class DownloadAction {


    private static DownloadAction downloadAction;

    private DownloadAction() {

    }

    public synchronized static DownloadAction getInstance() {
        if (null == downloadAction) {
            downloadAction = new DownloadAction();
        }
        return downloadAction;
    }

    public void addAction(ActionListener actionListener) {
        if (null != actionListener) {
            this.actionListener = actionListener;
        }
    }


    public void removeAction() {
        this.actionListener = null;
    }

    public void reciverData(final OtaFileInfo otaFileInfo) {
        if (null != actionListener) {
            actionListener.onAction(otaFileInfo);
        }
    }


    public interface ActionListener {
        abstract void onAction(OtaFileInfo otaFileInfo);
    }

    ActionListener actionListener;

}
