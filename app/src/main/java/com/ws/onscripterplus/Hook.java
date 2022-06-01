package com.ws.onscripterplus;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@SuppressLint("NewApi")
public class Hook implements IXposedHookLoadPackage, IXposedHookInitPackageResources {

  @Override
  public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
    if (!lpparam.packageName.equals("com.onscripter.plus")) {
      return;
    }
    Class<?> m44 = XposedHelpers.findClass("m44", lpparam.classLoader);
    Class<?> tAu = XposedHelpers.findClass("m44$TAu", lpparam.classLoader);
    Class<?> w44 = XposedHelpers.findClass("w44", lpparam.classLoader);

    // 加载提示 申请权限
    XposedHelpers.findAndHookMethod("com.onscripter.plus.MainActivity", lpparam.classLoader,
        "onCreate", Bundle.class,
        new XC_MethodHook() {
          @Override
          protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            super.afterHookedMethod(param);
            Activity activity = (Activity) param.thisObject;

            Toast.makeText(activity, "ONScripter Plus+ 加载成功!", Toast.LENGTH_SHORT).show();

            PackageManager packageManager = activity.getPackageManager();

            List<String> strings = Arrays.asList(
                packageManager.getPackageInfo("com.onscripter.plus",
                    PackageManager.GET_PERMISSIONS).requestedPermissions);

            if (!strings.contains(permission.MANAGE_EXTERNAL_STORAGE)) {
              Toast.makeText(activity, "没有权限 " + permission.MANAGE_EXTERNAL_STORAGE,
                  Toast.LENGTH_SHORT).show();
              return;
            }
            // 先判断有没有权限
            if (!Environment.isExternalStorageManager()) {
              Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
              intent.setData(Uri.parse("package:" + activity.getPackageName()));
              activity.startActivityForResult(intent, 200);
            }

          }
        });

    // 加载封面为 icon.png
    XposedHelpers.findAndHookMethod(m44, "gg", tAu, String.class,
        new XC_MethodHook() {
          @Override
          protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            super.afterHookedMethod(param);
            if (param.getResult() != null) {
              return;
            }
            Object arg = param.args[0];
            Object z34 = tAu.getField("gt").get(arg);
            Object str1 = tAu.getField("fHh").get(arg);
            String name = null, image = null;
            if (z34 != null) {
              image = URLDecoder.decode(z34.toString(), "utf-8");
              String[] split = image.split(":");
              Path parent = Paths.get(split[split.length - 1]).getParent();
              name = parent.getFileName().toString();
              String path = parent.toString();
              image = "file://" + (path.startsWith("/") ? path : "/storage/emulated/0/" + path)
                  + "/icon.png";
            }
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            Object o = XposedHelpers.newInstance(w44, image.hashCode(), z34, str1,
                name, name, name, name, name, image,
                true, true, 1, date, "", true);
            param.setResult(o);
          }
        });

    // 去除广告
    XposedHelpers.findAndHookMethod("com.google.android.gms.ads.AdActivity", lpparam.classLoader,
        "onCreate",
        Bundle.class, new XC_MethodHook() {
          @Override
          protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            super.afterHookedMethod(param);
            Activity activity = (Activity) param.thisObject;
            activity.finish();
          }
        });

  }

  /**
   * 去除谷歌广告
   *
   * @param resparam Information about the resources.
   * @throws Throwable
   */
  @Override
  public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
    if (!resparam.packageName.equals("com.onscripter.plus")) {
      return;
    }
    resparam.res.hookLayout("com.onscripter.plus", "layout", "banner_area",
        new XC_LayoutInflated() {
          @Override
          public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
            TextView bannerText = liparam.view.findViewById(
                liparam.res.getIdentifier("banner_text", "id", "com.onscripter.plus"));
            bannerText.setHeight(0);
            bannerText.setWidth(0);
            View banner = liparam.view.findViewById(
                liparam.res.getIdentifier("banner", "id", "com.onscripter.plus"));
            banner.setVisibility(View.GONE);
          }
        });
  }

}
