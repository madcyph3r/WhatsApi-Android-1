package nl.giovanniterlingen.whatsapp;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ChatAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final String[] values;
	private final String nEdit;

	public ChatAdapter(Context context, String[] values, String nEdit) {
		super(context, -1, values);
		this.context = context;
		this.values = values;
		this.nEdit = nEdit;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = view = mInflater.inflate(R.layout.chat_item, null);

		Resources res = getContext().getResources();
		Drawable bubblesChat = res.getDrawable(R.drawable.balloon_incoming_normal);
		Drawable bubblesResponse = res.getDrawable(R.drawable.balloon_outgoing_normal);
		TextView left = (TextView) view.findViewById(R.id.lefttext);
		TextView right = (TextView) view.findViewById(R.id.righttext);

		String txt = values[position];
		if (txt.startsWith("me: ")) {
			
			String message = getItem(position).replaceAll("me: ", "");
			right.setText(message);
			right.setBackgroundDrawable(bubblesResponse);
			left.setText("");
			left.setBackgroundDrawable(null);
		} else {
			String message = getItem(position).replaceAll(nEdit + ": ", "");
			left.setText(message);
			left.setBackgroundDrawable(bubblesChat);
			right.setText("");
			right.setBackgroundDrawable(null);
		}
		return view;
	}
}
