package com.zjta.wyb.download.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.zjta.wyb.base.GlobalApplication;
import com.zjta.wyb.download.model.DaoMaster;
import com.zjta.wyb.download.model.DaoSession;
import com.zjta.wyb.download.model.DownloadInfo;
import com.zjta.wyb.download.model.DownloadInfoDao;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

/**
 * 断点续传
 * 数据库工具类-geendao运用
 */
public class DbDownUtil {

    private static DbDownUtil db;
    private final static String dbName = "AppDb";
    public DaoMaster.DevOpenHelper openHelper;
    private Context context = GlobalApplication.getContext();


    private DbDownUtil() {
        openHelper = new DaoMaster.DevOpenHelper(context, dbName, null);
    }


    /**
     * 获取单例
     */
    public static DbDownUtil getInstance() {
        if (db == null) {
            synchronized (DbDownUtil.class) {
                if (db == null) {
                    db = new DbDownUtil();
                }
            }
        }
        return db;
    }


    /**
     * 获取可读数据库
     */

    private SQLiteDatabase getReadableDatabase() {
        if (openHelper == null) {
            openHelper = new DaoMaster.DevOpenHelper(context, dbName, null);
        }
        SQLiteDatabase db = openHelper.getReadableDatabase();
        return db;
    }

    /**
     * 获取可写数据库
     */

    private SQLiteDatabase getWritableDatabase() {
        if (openHelper == null) {
            openHelper = new DaoMaster.DevOpenHelper(context, dbName, null);
        }
        SQLiteDatabase db = openHelper.getWritableDatabase();
        return db;
    }


    public void insertOrReplace(DownloadInfo info) {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        DownloadInfoDao downInfoDao = daoSession.getDownloadInfoDao();
        downInfoDao.insertOrReplace(info);
    }

    public void deleteDowninfo(DownloadInfo info) {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        DownloadInfoDao downInfoDao = daoSession.getDownloadInfoDao();
        downInfoDao.delete(info);
    }

    @Nullable
    public DownloadInfo queryDownBy(String url) {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        DownloadInfoDao downInfoDao = daoSession.getDownloadInfoDao();
        QueryBuilder<DownloadInfo> qb = downInfoDao.queryBuilder();
        qb.where(DownloadInfoDao.Properties.Url.eq(url));
        List<DownloadInfo> list = qb.list();
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    public List<DownloadInfo> queryDownAll() {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        DownloadInfoDao downInfoDao = daoSession.getDownloadInfoDao();
        QueryBuilder<DownloadInfo> qb = downInfoDao.queryBuilder();
        return qb.list();
    }
}
