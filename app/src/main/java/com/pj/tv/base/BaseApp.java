package com.pj.tv.base;

import android.app.Application;

import me.jessyan.autosize.AutoSizeConfig;
import me.jessyan.autosize.unit.Subunits;


/**
 * @author acer
 * @date 2018/12/18
 */

public class BaseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AutoSizeConfig.getInstance().getUnitsManager().setSupportDP(false).setSupportSubunits(Subunits.PT);
    }
}
