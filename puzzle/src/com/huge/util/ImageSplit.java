package com.huge.util;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;

/**
 * 对一幅图片进行切割
 * @author Ben
 *
 */
public class ImageSplit
{
	/**
	 * 切割图片
	 * @param picture 要切割的一幅完整图片
	 * @param piece 切割成piece*piece个切片
	 * @return 返回装有每个切片的List
	 */
	public static List<ImagePiece> splitImage(Bitmap picture, int piece)
	{
		List<ImagePiece> imagePieces = new ArrayList<ImagePiece>();
		// 获取图片的宽和高 以两者小的为基准
		int width = picture.getWidth();
		int height = picture.getHeight();
		// 设置每个切片的宽度和高度
		int pieceWidth = Math.min(width, height) / piece;
		for (int i = 0; i < piece; i++)
		{
			for (int j = 0; j < piece; j++)
			{
				// 从原始图片中截取图片生成切片
				ImagePiece imagePiece = new ImagePiece();
				int index = i * piece + j;
				imagePiece.setIndex(index);
				int x = j * pieceWidth;
				int y = i * pieceWidth;
				Bitmap bitmap = Bitmap.createBitmap(picture, x, y, pieceWidth, pieceWidth);
				imagePiece.setBitmap(bitmap);
				
				imagePieces.add(imagePiece);
			}
		}
		
		return imagePieces;
	}
}
