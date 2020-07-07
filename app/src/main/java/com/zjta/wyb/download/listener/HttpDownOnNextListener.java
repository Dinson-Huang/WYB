package com.zjta.wyb.download.listener;

/**
 * 下载过程中的回调处理
 */
public class HttpDownOnNextListener<T> {
    /**
     * 成功后回调方法
     *
     * @param t
     */
    public void onNext(T t) {
    }

    /**
     * 开始下载
     */
    public void onStart() {
    }

    /**
     * 完成下载
     */
    public void onComplete() {
    }


    /**
     * 下载进度
     *
     * @param readLength
     * @param countLength
     */
    public void updateProgress(long readLength, long countLength) {
    }

    /**
     * 失败或者错误方法
     * 主动调用，更加灵活
     *
     * @param e
     */
    public void onError(Throwable e) {
    }

    /**
     * 暂停下载
     */
    public void onPause() {
    }

    /**
     * 停止下载销毁
     */
    public void onStop() {
    }
}
