package com.datecs.demo.ui.main.tools;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.Arrays;

public class BitmapConvertor{

	private static final String TAG = "BitmapConvertor";
	private ProgressDialog mPd;
	private Context mContext;
    private Bitmap outBitmap;

	public BitmapConvertor(Context context) {
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	public Bitmap convertBitmap(Bitmap inputBitmap){
		ConvertInBackground convert = new ConvertInBackground();
		convert.execute(inputBitmap);
		return outBitmap;
	}

	private Bitmap bmpDithering(Bitmap inBmp) {

		final int imgWIDTH = inBmp.getWidth();
		final int imgHEGHT = inBmp.getHeight();
		Bitmap bmpGrayscale = Bitmap.createBitmap(imgWIDTH, imgHEGHT, Bitmap.Config.ARGB_8888);

		int[] workBuffer = new int[imgWIDTH * imgHEGHT];
		Arrays.fill(workBuffer, 0);
		for (int y = 0; y < imgHEGHT - 1; y++)
			for (int x = 0; x < imgWIDTH - 1; x++) {
				int tmpColor = inBmp.getPixel(x, y);
				int b_b = (Color.blue(tmpColor));  // >> 16);
				int g_b = (Color.green(tmpColor)); // >>  8);
				int r_b = (Color.red(tmpColor));
				workBuffer[y * imgWIDTH + x] = (int) Math.round(0.114 * r_b + 0.587 * g_b + 0.299 * b_b);
			}
		// floyd-steinberg
		for (int y = 0; y < imgHEGHT - 2; y++)
			for (int x = 0; x < imgWIDTH - 2; x++) {
				int d = workBuffer[(y * imgWIDTH) + x];
				if (d > 128) {
					d = 255 - d;
					if (d < 0) d = 0;
					workBuffer[(y * imgWIDTH) + x + 1] = workBuffer[(y * imgWIDTH) + x + 1] - (d >> 1);
					workBuffer[((y + 1) * imgWIDTH) + x] = workBuffer[((y + 1) * imgWIDTH) + x] - (d >> 2);
					workBuffer[((y + 1) * imgWIDTH) + x + 1] = workBuffer[((y + 1) * imgWIDTH) + x + 1] - (d >> 3);
					workBuffer[((y + 1) * imgWIDTH) + x - 1] = workBuffer[((y + 1) * imgWIDTH) + x - 1] - (d >> 3);
					workBuffer[(y * imgWIDTH) + x] = 255;
				} else {
					if (d < 0) d = 0;
					workBuffer[(y * imgWIDTH) + x + 1] = workBuffer[(y * imgWIDTH) + x + 1] + (d >> 1);
					workBuffer[((y + 1) * imgWIDTH) + x] = workBuffer[((y + 1) * imgWIDTH) + x] + (d >> 2);
					workBuffer[((y + 1) * imgWIDTH) + x + 1] = workBuffer[((y + 1) * imgWIDTH) + x + 1] + (d >> 3);
					workBuffer[((y + 1) * imgWIDTH) + x - 1] = workBuffer[((y + 1) * imgWIDTH) + x - 1] + (d >> 3);
					workBuffer[(y * imgWIDTH) + x] = 0;
				}
			}
		for (int y = 0; y < imgHEGHT - 1; y++)
			for (int x = 0; x < imgWIDTH - 1; x++) {
				if (workBuffer[y * imgWIDTH + x] > 128)
					bmpGrayscale.setPixel(x, y, Color.WHITE);
				else bmpGrayscale.setPixel(x, y, Color.BLACK);
			}
		return bmpGrayscale;
	}


    class ConvertInBackground extends AsyncTask<Bitmap, String, Void>{

		@Override
		protected Void doInBackground(Bitmap... params) {
			// TODO Auto-generated method stub
		outBitmap= bmpDithering(params[0]);
			return null;
		}

		
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			mPd.dismiss();
            Toast.makeText(mContext, "Monochrome bitmap created successfully.", Toast.LENGTH_LONG).show();
		}


		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			mPd= ProgressDialog.show(mContext, "Converting Image", "Please Wait", true, false, null);
		}

		
    	
    }
}
