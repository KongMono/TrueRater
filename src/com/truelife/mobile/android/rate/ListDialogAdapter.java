package com.truelife.mobile.android.rate;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class ListDialogAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private SparseArray<String> items = new SparseArray<String>();
	protected Context context;
	ViewHolder holder;

	public class ViewHolder {
		public TextView title;
		public TextView detail;
		public int position;
	}

	public ListDialogAdapter(Context context, SparseArray<String> items) {
		this.mInflater = LayoutInflater.from(context);
		this.items = items;
		this.context = context;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int pos) {
		return items.get(pos);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		String item = items.get(position);
		View view = convertView;
		if (convertView == null) {
			view = mInflater.inflate(R.layout.item_list, parent, false);
			holder = new ViewHolder();
			
			holder.detail = (TextView)view.findViewById(R.id.detail);
			holder.detail.setTextSize(15f);
			
			holder.position = position;
			view.setTag(holder);

		} else {
			holder = (ViewHolder) view.getTag();
		}
		
		holder.detail.setText(item);
		
		return view;
	}
}
