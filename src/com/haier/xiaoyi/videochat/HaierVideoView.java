package com.haier.xiaoyi.videochat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.haier.xiaoyi.MainApplication;
import com.haier.xiaoyi.util.Logger;

public class HaierVideoView extends View {

	Bitmap bitmap;
	public static int height = MainApplication.ScreenHigh ;
	public static int width =  MainApplication.ScreenWidth ;
	Matrix matrix = new Matrix();

	private void init() {
		// matrix.setRotate(-90);
//		matrix.postScale(3f, 2.5f);
	}

	public HaierVideoView(Context context) {
		super(context);
		init();
	}

	public HaierVideoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public HaierVideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (bitmap != null) {
			Logger.d("luo","height :" + height + ",width : " + width);
			Logger.d("luo"," get height :" + getHeight() + ", get width : " + getWidth());
			Logger.d("luo","bitmap.height :" + bitmap.getHeight() + ",bitmap.width : " + bitmap.getWidth());
//			canvas.drawColor(Color.BLUE);
			RectF rectF = new RectF(0, 0,getWidth(), getHeight());   //w和h分别是屏幕的宽和高，也就是你想让图片显示的宽和高
//			canvas.drawBitmap(bitmap, null, rectF, null);
			canvas.drawBitmap(bitmap, null, rectF, null);
//			setBitmap(ThumbnailUtils.extractThumbnail(bitmap, height, width));
		}
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
		invalidate();
	}

}
