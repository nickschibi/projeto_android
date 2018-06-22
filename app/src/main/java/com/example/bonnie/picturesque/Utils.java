package com.example.bonnie.picturesque;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created by aluno on 08/06/18.
 */




    public class Utils {
        public static byte [] toByteArray (Bitmap image){
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 0,byteArrayOutputStream );
            return byteArrayOutputStream.toByteArray();
        }
        public static Bitmap toBitmap (byte [] bytes){
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            return BitmapFactory.decodeStream(byteArrayInputStream);
        }


}
