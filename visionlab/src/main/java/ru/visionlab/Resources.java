package ru.visionlab;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class Resources {

    public static final String VL_DATA_PACK = "data";

    public static final String PATH_TO_EXTRACTED_VL_DATA = "/vl/data";

    private static final String PATH_TO_VL_DATA = "/vl";

    public static boolean createVLDataFolder(Context filesContext) {
        // create folder
        final File fmd = new File(filesContext.getFilesDir() + PATH_TO_VL_DATA);
        System.out
                .println("CREATE VL DATA FOLDER : " + filesContext.getFilesDir() + PATH_TO_VL_DATA);

        if (!(fmd.mkdirs() || fmd.isDirectory())) {
            System.out.println("COULDN'T CREATE VL DATA FOLDER : " + filesContext.getFilesDir()
                    + PATH_TO_VL_DATA);
            return false;
        }
        return true;
    }

    public static boolean createFilesFromAssetFolder(Context context, String folder) {

        final File fmd = new File(context.getFilesDir() + PATH_TO_VL_DATA + "/" + folder);

        if (!(fmd.mkdirs() || fmd.isDirectory())) {
            System.out.println("Failed to create folder " + folder);
            return false;
        }

        try {
            AssetManager assetManager = context.getAssets();

            ArrayList<String> files = new ArrayList<>(Arrays.asList(assetManager.list(folder)));

            /*for(Iterator<String> str_iterator=files.iterator();str_iterator.hasNext();){//remove any cpu_plan in data folder(x86 arch is deprecated)
            String currentPlan=str_iterator.next();
                if(currentPlan.contains("cpu")){str_iterator.remove();}
            }*/

            if (files == null || files.size() == 0) {
                System.out.println("ERROR: ASSET FILE IS EMPTY!");
                return false;
            }

            for (String filename : files) {
                File file = new File(
                        context.getFilesDir() + PATH_TO_VL_DATA + "/" + folder + "/" + filename);
                InputStream iStream = assetManager.open(folder + "/" + filename);

                if (writeBytesToFile(iStream, file)) {
                    System.out.println(
                            "CREATED FILE FROM ASSETS: " + folder + "/" + filename + "  TO   "
                                    + file.getAbsolutePath());
                } else {
                    System.out.println("ERROR CREATING FILE FROM ASSETS: " + folder + "/" + filename
                            + "  TO   " + file.getAbsolutePath());
                }

            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }


    public static boolean writeBytesToFile(InputStream is, File file) throws IOException {
        boolean result = false;
        FileOutputStream fos = null;
        try {
            byte[] data = new byte[2048];
            int nbread = 0;
            fos = new FileOutputStream(file);
            while ((nbread = is.read(data)) > -1) {
                fos.write(data, 0, nbread);
            }
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        } finally {
            if (fos != null) {
                fos.close();
            }
        }

        return result;
    }

    public static void hideKeyboard(Context context, View view) {
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) context
                    .getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void hideKeyboardRoutine(Context context, View view) {
        if (view != null && view.findFocus() != null) {
            hideKeyboard(context, view.findFocus());
            view.findFocus().clearFocus();
        }
    }


    @SuppressWarnings("SimplifiableIfStatement")
    public static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public static String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat,
            int quality) {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }


    @Nullable
    public static Bitmap decodeBase64(String input) {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

}