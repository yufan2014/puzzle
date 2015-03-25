package com.huge.view;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.huge.puzzle.R;
import com.huge.util.ImagePiece;
import com.huge.util.ImageSplit;

/**
 * @author Ben
 * 写自己的布局，继承自相对布局
 *
 */
public class GameLayout extends RelativeLayout implements OnClickListener
{

	/**
	 * 切片行列数 默认为3行3列
	 */
	private int mColumn = 3;
	
	/**
	 * 内边距
	 */
	private int mPadding;
	
	/**
	 * 每两个切图间距 默认3dp
	 */
	private int mMargin = 3;
	
	/**
	 * 每个布局中的切图
	 */
	private ImageView[] mItems;
	
	/**
	 * 每个布局中的切图的宽度
	 */
	private int mItemWidth;
	
	/**
	 * 游戏原始图片
	 */
	private Bitmap mBitmap = null;
	
	/**
	 * 所有切图
	 */
	private List<ImagePiece> mImagePieces;
	
	/**
	 * 游戏面板的宽度
	 */
	private int mWidth;
	
	/**
	 * 控制执行
	 */
	private boolean once = false;
	
	
	/**
	 * 交换切图 第一个
	 */
	private ImageView mFirstView;
	/**
	 * 交换切图 第二个
	 */
	private ImageView mSecondView;
	
	
	public GameLayout(Context context)
	{
		this(context, null);
	}

	public GameLayout(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}
	
	public GameLayout(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		init();
	}

	/**
	 * 初始化mMargin
	 */
	private void init()
	{
		// 将单位转为20dp
		mMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 
				3, getResources().getDisplayMetrics());
		// 设置padding
		mPadding = min(getPaddingLeft(), getPaddingRight(), getPaddingTop(), getPaddingBottom());
	}
	
	/**
	 * 设置游戏面板的宽和高 
	 * 加载原始图片，进行切图
	 * 设置每个ImageView
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		if (!once)
		{
			mWidth = Math.min(getMeasuredHeight(), getMeasuredWidth());
			
			// 加载原始图片，进行切图
			initBitmap();
			
			// 设置每个ImageView
			initItem();
			
			once = true;
		}
		// 必须调用以重新设置
		setMeasuredDimension(mWidth, mWidth);
	}

	/**
	 * 加载原始图片，进行切图
	 */
	private void initBitmap()
	{
		if (mBitmap == null)
		{
			// 加载原始图片
			mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image);
			// 切图
			mImagePieces = ImageSplit.splitImage(mBitmap, mColumn);
			// 对切图进行乱序
			Collections.sort(mImagePieces, new Comparator<ImagePiece>()
			{
				@Override
				public int compare(ImagePiece lhs, ImagePiece rhs)
				{
					return Math.random() > 0.5 ? 1 : -1;
				}
				
			});
		}
		
		
	}
	
	/**
	 * 设置每个ImageView
	 */
	private void initItem()
	{
		// 每个ImageView的宽度
		mItemWidth = (mWidth - mPadding * 2 - mMargin * (mColumn-1)) / mColumn;
		mItems = new ImageView[mColumn * mColumn];
		
		for (int i = 0; i < mItems.length; i++)
		{
			ImageView item = new ImageView(getContext());
			// 点击事件
			item.setOnClickListener(this);
			// 设置ImageView的图片
			item.setImageBitmap(mImagePieces.get(i).getBitmap());
			mItems[i] = item;
			item.setId(i+1);
			
			// 在item的tag中存储index
			item.setTag(i + "_" + mImagePieces.get(i).getIndex());
			
			// 设置布局
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(mItemWidth, mItemWidth);
			
			// 如果不是第一行 设置topMargin和rule
			if ((i+1) > mColumn)
			{
				lp.topMargin = mMargin;
				lp.addRule(RelativeLayout.BELOW, mItems[i-mColumn].getId());
			}
			
			// 如果不是第一列
			if (i % mColumn != 0)
			{
				lp.addRule(RelativeLayout.RIGHT_OF, mItems[i-1].getId());
			}
			
			// 如果不是最后一列 设置rightMargin
			if ((i+1) % mColumn != 0)
			{
				lp.rightMargin = mMargin;
			}
			
			addView(item, lp);
		}
	}
	
	/**
	 * 求几个数的最小值
	 * @param params
	 * @return
	 */
	private int min(int... params)
	{
		int value = params[0];
		for (int i : params)
		{
			if (i < value)
			{
				value = i;
			}
		}
		return value;
	}

	/* 
	 * 点击两张图片 进行交换
	 */
	@Override
	public void onClick(View v)
	{
		// 如果两次点击都是同一张图片，则取消第一张的高亮
		if (mFirstView == v)
		{
			mFirstView.setColorFilter(null);
			mFirstView = null;
			return;
		}
		
		// 点击第一张 设置高亮
		if (mFirstView == null)
		{
			mFirstView = (ImageView)v;
			mFirstView.setColorFilter(Color.parseColor("#55ff0000"));
		}
		// 点击第二张 进行交换
		else
		{
			mFirstView.setColorFilter(null);
			
			mSecondView = (ImageView)v;
			// 交换图片
			exchangImage(mFirstView, mSecondView);
			
			mFirstView = null;
			mSecondView = null;
		}
		
	}

	
	/**
	 * 交换两张图片 根据ImageView中tag的内容来设置图片
	 * @param first
	 * @param second
	 */
	private void exchangImage(ImageView first, ImageView second)
	{
		// 获得ImageView中存储的tag
		String firstParams = (String)first.getTag();
		String firstTag = firstParams.split("_")[0];
		
		String secondParams = (String)second.getTag();
		String secondTag = secondParams.split("_")[0];
		
		mFirstView.setImageBitmap(mImagePieces.get(Integer.parseInt(secondTag)).getBitmap());
		mFirstView.setTag(secondParams);
		mSecondView.setImageBitmap(mImagePieces.get(Integer.parseInt(firstTag)).getBitmap());
		mSecondView.setTag(firstParams);
		
	}
}
