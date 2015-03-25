package com.huge.view;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.huge.puzzle.R;
import com.huge.util.ImagePiece;
import com.huge.util.ImageSplit;

/**
 * @author Ben
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
	
	
	/**
	 * 动画层
	 */
	private RelativeLayout mAnimationLayout;
	
	/**
	 * 是否正在进行动画效果
	 */
	private boolean isAnimating;
	/**
	 * 游戏是否成功
	 */
	private boolean isGameSuccess;
	private boolean isGameOver;
	
	private boolean isTimeEnabled = false;
	private int mTime;
	
	private boolean isPause;
	
	/**
	 * 设置是否开启时间
	 * 
	 * @param isTimeEnabled
	 */
	public void setTimeEnabled(boolean isTimeEnabled)
	{
		this.isTimeEnabled = isTimeEnabled;
	}
	
	public interface GamePuzzleListener
	{
		void nextLevel(int nextLevel);
		
		void timeChanged(int currentTime);
		
		void gameOver();
	}
	
	public GamePuzzleListener mListener;
	
	/**
	 * 
	 * @param listener
	 */
	public void setOnGamePuzzleListener(GamePuzzleListener listener)
	{
		this.mListener = listener;
	}
	
	// 关卡数
	private int mLevel = 1;
	private final static int NEXT_LEVEL = 0x111;
	private final static int TIME_CHANGED = 0x001;
	
	private Handler mHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg) 
		{
			switch (msg.what)
			{
			// 下一关
			case NEXT_LEVEL:
				mLevel = mLevel + 1;
				if (mListener != null)
				{
					mListener.nextLevel(mLevel);
				}
				else
				{
					nextLevel();
				}
				break;
			// 时间变化
			case TIME_CHANGED:
				if (isGameSuccess || isGameOver)
				{
					return;
				}
				if (mListener != null)
				{
					mListener.timeChanged(mTime);
				}
				if (mTime == 0)
				{
					isGameOver = true;
					mListener.gameOver();
					return;
				}
				mTime--;
				mHandler.sendEmptyMessageDelayed(TIME_CHANGED, 1000);
				break;
			}
		}
	};
	
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
	 * 设置游戏面板的宽度
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
			
			// 判断是否开启时间
			checkTimeEnabled();
			
			once = true;
		}
		// 必须调用以重新设置
		setMeasuredDimension(mWidth, mWidth);
	}

	/**
	 * 是否开启时间
	 */
	private void checkTimeEnabled()
	{
		if (isTimeEnabled)
		{
			// 根据当前等级设置时间
			countTimeBasedLevel();
			mHandler.sendEmptyMessage(TIME_CHANGED);
		}
	}


	/**
	 * 根据当前等级设置游戏时间
	 */
	private void countTimeBasedLevel()
	{
		mTime = (int)Math.pow(2, mLevel) * 60;
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
		}
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
		if (isAnimating)
			return;
		
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
			mSecondView = (ImageView)v;
			// 交换图片
			exchangImage();
		}
		
	}

	
	/**
	 * 交换两张图片 根据ImageView中tag的内容来设置图片
	 * @param first
	 * @param second
	 */
	private void exchangImage()
	{
		mFirstView.setColorFilter(null);
		
		// 创建动画层
		setUpAnimationLayout();
		
		// 在动画层上创建ImageView
		ImageView firstInAnimationLayout = createImageView(mFirstView);
		mAnimationLayout.addView(firstInAnimationLayout);
		
		ImageView secondInAnimationLayout = createImageView(mSecondView);
		mAnimationLayout.addView(secondInAnimationLayout);
		
		// 设置动画
		TranslateAnimation firstAnimation = new TranslateAnimation(0, mSecondView.getLeft()-mFirstView.getLeft(),
				0, mSecondView.getTop()-mFirstView.getTop());
		firstAnimation.setDuration(300);
		firstAnimation.setFillAfter(true);
		firstInAnimationLayout.startAnimation(firstAnimation);
		
		TranslateAnimation secondAnimation = new TranslateAnimation(0, mFirstView.getLeft()-mSecondView.getLeft(),
				0, mFirstView.getTop()-mSecondView.getTop());
		secondAnimation.setDuration(300);
		secondAnimation.setFillAfter(true);
		secondInAnimationLayout.startAnimation(secondAnimation);
		
		// 绑定监听器
		firstAnimation.setAnimationListener(new AnimationListener()
		{

			@Override
			public void onAnimationStart(Animation animation)
			{
				// 动画开始 设置两张图片不可见
				mFirstView.setVisibility(View.INVISIBLE);
				mSecondView.setVisibility(View.INVISIBLE);
				isAnimating = true;
			}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				// 动画结束 
				String firstParams = (String) mFirstView.getTag();
				String firstTag = firstParams.split("_")[0];
				Bitmap firstBitmap = mImagePieces.get(Integer.parseInt(firstTag)).getBitmap();
				
				String secondParams = (String) mSecondView.getTag();
				String secondTag = secondParams.split("_")[0];
				Bitmap secondBitmap = mImagePieces.get(Integer.parseInt(secondTag)).getBitmap();
				
				mFirstView.setImageBitmap(secondBitmap);
				mSecondView.setImageBitmap(firstBitmap);
				
				mFirstView.setTag(secondParams);
				mSecondView.setTag(firstParams);
				
				mFirstView.setVisibility(View.VISIBLE);
				mSecondView.setVisibility(View.VISIBLE);
				
				mFirstView = null;
				mSecondView = null;
				mAnimationLayout.removeAllViews();
				isAnimating = false;
				
				// 判断游戏是否成功
				checkSuccess();
			}

			/**
			 * 判断游戏是否成功
			 */
			private void checkSuccess()
			{
				boolean isSuccess = true;
				
				for(int i = 0; i < mItems.length; i++)
				{
					ImageView imageView = mItems[i];
					// 通过比较imageview中tag存储的index是不是和索引一致
					int index = Integer.parseInt(((String)imageView.getTag()).split("_")[1]);
					if (index != i)
					{
						isSuccess = false;
					}
				}
				// 游戏成功
				if (isSuccess)
				{
					isGameSuccess = true;
					mHandler.removeMessages(TIME_CHANGED);
					// 信息
					Toast.makeText(getContext(), "Success ， level up !!!",
							Toast.LENGTH_LONG).show();
					// 传消息
					mHandler.sendEmptyMessage(NEXT_LEVEL);
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{
				
			}
			
		});
		
	}
	
	/**
	 * 在动画层上创建ImageView
	 * @param view
	 * @return
	 */
	private ImageView createImageView(ImageView view)
	{
		ImageView imageView = new ImageView(getContext());
		// 根据tag内容设置bitmap
		String params = (String) view.getTag();
		String tag = params.split("_")[0];
		Bitmap bitmap = mImagePieces.get(Integer.parseInt(tag)).getBitmap();
		imageView.setImageBitmap(bitmap);
		// 设置布局
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(mItemWidth, mItemWidth);
		Log.d("view", "left:" + view.getLeft()+"");
		Log.d("view", "top:" + view.getTop()+"");
		Log.d("view", "mPadding:" + mPadding);
		lp.leftMargin = view.getLeft() - mPadding;
		lp.topMargin = view.getTop() - mPadding;
		imageView.setLayoutParams(lp);
		return imageView;
	}
	
	
	
	/**
	 * 创建动画层
	 */
	private void setUpAnimationLayout()
	{
		if (mAnimationLayout == null)
		{
			mAnimationLayout = new RelativeLayout(getContext());
			// 添加到GameLayout
			addView(mAnimationLayout);
		}
	}
	
	/**
	 * 下一关
	 */
	public void nextLevel()
	{
		this.removeAllViews();
		mColumn++;
		mAnimationLayout = null;
		isGameSuccess = false;
		checkTimeEnabled();
		initBitmap();
		initItem();
	}

	
	public void restart()
	{
		isGameOver = false;
		mColumn--;
		nextLevel();
	}
	
	public void pause()
	{
		isPause = true;
		mHandler.removeMessages(TIME_CHANGED);
	}
	
	public void resume()
	{
		if (isPause)
		{
			isPause = false;
			mHandler.sendEmptyMessage(TIME_CHANGED);
		}
	}
}
