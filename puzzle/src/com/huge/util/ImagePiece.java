package com.huge.util;

import android.graphics.Bitmap;

/**
 * 图片切片
 * @author Ben
 *
 */
public class ImagePiece
{
	/**
	 *  图片索引位置
	 */
	private int index;
	/**
	 *  图片切片
	 */
	private Bitmap bitmap;
	
	public ImagePiece()
	{
		
	}

	public ImagePiece(int index, Bitmap bitmap)
	{
		this.index = index;
		this.bitmap = bitmap;
	}

	public int getIndex()
	{
		return index;
	}

	public void setIndex(int index)
	{
		this.index = index;
	}

	@Override
	public String toString()
	{
		return "ImagePiece [index=" + index + ", bitmap=" + bitmap + "]";
	}

	public Bitmap getBitmap()
	{
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap)
	{
		this.bitmap = bitmap;
	}
	
	

}
