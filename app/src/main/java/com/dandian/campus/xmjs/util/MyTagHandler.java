package com.dandian.campus.xmjs.util;

import java.io.File;
import java.io.IOException;

import org.xml.sax.XMLReader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.Html.TagHandler;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.view.View;

public class MyTagHandler implements TagHandler {

	private Context context;

	public MyTagHandler(Context context) {
		this.context = context;
	}

	@Override
	public void handleTag(boolean opening, String tag, Editable output,
						  XMLReader xmlReader) {
		// TODO Auto-generated method stub

		if (tag.toLowerCase().equals("img")) {
			int len = output.length();
			ImageSpan[] images = output.getSpans(len-1, len, ImageSpan.class);
			String imgURL = images[0].getSource();

			output.setSpan(new ImageClick(context, imgURL), len-1, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

	}

	private class ImageClick extends ClickableSpan {

		private String url;
		private Context context;

		public ImageClick(Context context, String url) {
			this.context = context;
			this.url = url;
		}

		@Override
		public void onClick(View widget) {
			// TODO Auto-generated method stub

			String imageName = null;
			try {
				imageName = Dm5.dm5(url);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String[] ss = url.split("\\.");
			String ext = ss[ss.length - 1];


			String savePath = FileUtility.getCacheDir() + imageName + "." + ext;

			File file = new File(savePath);
			if (file.exists()) {

				Intent intent = new Intent(Intent.ACTION_VIEW);
				if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.N) {
					Uri contentUri = FileProvider.getUriForFile(context,context.getApplicationContext().getPackageName() + ".fileProvider",file);
					//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
					intent.setDataAndType(contentUri,"image/*");

				}else{
					//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setDataAndType(Uri.fromFile(file),"image/*");
				}
				context.startActivity(intent);
			}
		}

	}

}
