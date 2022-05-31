package com.ws.onscripterplus;

import android.os.Build;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Hook implements IXposedHookLoadPackage, IXposedHookInitPackageResources {

  @Override
  public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
    if (!lpparam.packageName.equals("com.onscripter.plus")) {
      return;
    }
    Class<?> m44 = XposedHelpers.findClass("m44", lpparam.classLoader);
    Class<?> tAu = XposedHelpers.findClass("m44$TAu", m44.getClassLoader());
    Class<?> w44 = XposedHelpers.findClass("w44", lpparam.classLoader);

    XposedHelpers.findAndHookMethod(m44, "gg", tAu, String.class, new XC_MethodHook() {
      private long index = 0;

      @Override
      protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        super.beforeHookedMethod(param);
        for (Field declaredField : tAu.getDeclaredFields()) {
          declaredField.setAccessible(false);
          XposedBridge.log(declaredField.getName() + " - " + declaredField.get(param.args[0]));
        }
      }

      @RequiresApi(api = Build.VERSION_CODES.O)
      @Override
      protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        super.afterHookedMethod(param);
        XposedBridge.log("嘟嘟噜afterHookedMethod");
        if (param.getResult() != null) {
          return;
        }
        Object arg = param.args[0];
        Object z34 = tAu.getField("gt").get(arg);
        Object str1 = tAu.getField("fHh").get(arg);
        String name = null, image = null;
        if (z34 != null) {
          image = URLDecoder.decode(z34.toString(), "utf-8");
          Path parent = Paths.get(image).getParent();
          name = parent.getFileName().toString();
          image = parent.getParent().toString() + "/icon/" + name + ".png";
          String[] split = image.split(":");
          image = "file:///storage/emulated/0/" + split[split.length - 1];
        }
        XposedBridge.log(image);
        Object o = XposedHelpers.newInstance(w44, System.currentTimeMillis() + (index++), z34, str1,
            name, name, name, name, name, image,
            true, true, 1, "2022-01-01", "", true);
        param.setResult(o);
      }
    });
  }

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
