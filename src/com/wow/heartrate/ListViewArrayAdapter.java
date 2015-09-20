package com.wow.heartrate;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * 自定义ListViewAdapter，为了控制字体大小和颜色
 * @author gongjan
 *
 * @param <T>
 */
public class ListViewArrayAdapter<T> extends ArrayAdapter<T> {

	int pos = -1;
	public ListViewArrayAdapter(Context context, int resource,List<T> objects) {
		super(context, resource,  objects);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public View getView(int post, View convertView, ViewGroup parent) {
	// TODO Auto-generated method stub
		TextView mTextView = new TextView(this.getContext());
		mTextView.setText((String)this.getItem(post));
		mTextView.setTextSize(16);//设置字体大小
		mTextView.setTextColor(Color.WHITE);
		mTextView.setHeight(100);
		mTextView.setPadding(5, 10, 2, 2);
		//前两位为透明度，后面为RGB色位
		if(post == pos)
			mTextView.setBackgroundColor(0xFFF24C3C);
		else
			mTextView.setBackgroundColor(0xFF37403F);

		return mTextView;
	}
	
	public void setPosition(int pos){
		this.pos = pos;
	}

}
