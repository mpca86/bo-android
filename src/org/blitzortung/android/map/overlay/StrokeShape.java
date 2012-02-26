package org.blitzortung.android.map.overlay;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.shapes.Shape;

public class StrokeShape extends Shape {

	private float width;
	private float height;
	private float lineWidth;
	private int color;
	
	public StrokeShape(float size, int color) {
		width = size;
		height = size;
		lineWidth = size/4;
		this.color = color;
	}
	
	@Override
	public void draw(Canvas canvas, Paint paint) {
		paint.setColor(color);
		paint.setStrokeWidth(lineWidth);
		canvas.drawLine(-width / 2, 0.0f, width / 2, 0.0f, paint);
		canvas.drawLine(0.0f, -height / 2, 0.0f, height / 2, paint);

	}
}
