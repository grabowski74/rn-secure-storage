package com.taluttasgiran.rnsecurestorage;

import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.IllegalViewOperationException;
import com.securepreferences.SecurePreferences;

import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.ArrayList;
/**
 * 
 * @author 
 *
 */
public class RNSecureStorageModule extends ReactContextBaseJavaModule {
    private SharedPreferences prefs;
    private RNKeyStore rnKeyStore;

    RNSecureStorageModule(ReactApplicationContext reactContext) {
        super(reactContext);
        if (useKeystore()) {
            rnKeyStore = new RNKeyStore();
        } else {
            prefs = new SecurePreferences(getReactApplicationContext(), (String) null, "e4b001df9a082298dd090bb7455c45d92fbd5ddd.xml");
        }
    }

    private boolean useKeystore() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    @ReactMethod
    public void set(String key, String value, @Nullable ReadableMap options, Promise promise) {
        if (useKeystore()) {
            try {
                rnKeyStore.setCipherText(getReactApplicationContext(), key, value);
                promise.resolve("Key stored/updated successfully");
            } catch (Exception e) {
                promise.reject(e);
            }
        } else {
            try {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(key, value);
                editor.apply();
                promise.resolve("Key stored/updated successfully");
            } catch (Exception e) {
                promise.reject(e);
            }
        }
    }

    @ReactMethod
    public void get(String key, Promise promise) {
        if (useKeystore()) {
            try {
                promise.resolve(rnKeyStore.getPlainText(getReactApplicationContext(), key));
            } catch (FileNotFoundException fnfe) {
                promise.reject(fnfe);
            } catch (Exception e) {
                promise.reject(e);
            }
        } else {
            try {
                promise.resolve(prefs.getString(key, null));
            } catch (IllegalViewOperationException e) {
                promise.reject(e);
            }
        }
    }


    @ReactMethod
    public void remove(String key, Promise promise) {
        ArrayList<Boolean> fileDeleted = new ArrayList<Boolean>();
        if (useKeystore()) {
            try {
                for (String filename : new String[]{
                        Constants.SKS_DATA_FILENAME + key,
                        Constants.SKS_KEY_FILENAME + key,
                }) {
                    fileDeleted.add(getReactApplicationContext().deleteFile(filename));
                }
                if (!fileDeleted.get(0) || !fileDeleted.get(1)){
                    promise.reject("404","Could not find the key to delete.");
                }else{
                    promise.resolve("Key removed successfully");

                }
            } catch (Exception e) {
                promise.reject(e);
            }
        } else {
            try {
                if (prefs.getString(key, null) == null){
                    promise.reject("404","Could not find the key to delete.");
                }else {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.remove(key).apply();
                    promise.resolve("Key removed successfully");
                }
            } catch (Exception e) {
                promise.reject(e);
            }
        }
    }

    @Override
    public String getName() {
        return "RNSecureStorage";
    }
}