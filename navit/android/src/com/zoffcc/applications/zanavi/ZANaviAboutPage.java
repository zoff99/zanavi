package com.zoffcc.applications.zanavi;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
@SuppressLint("NewApi")
public class ZANaviAboutPage extends AppCompatActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Element BugsElement = new Element();
		BugsElement.setTitle(Navit.get_text("Report Bugs"));
		String BugsUrls = "https://github.com/zoff99/zanavi/issues";
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(BugsUrls));
		BugsElement.setIntent(i);

		Element fdroidElement = new Element();
		fdroidElement.setTitle("F-Droid");
		String froidUrls = "https://f-droid.org/repository/browse/?fdid=com.zoffcc.applications.zanavi";
		Intent i2 = new Intent(Intent.ACTION_VIEW);
		i2.setData(Uri.parse(froidUrls));
		fdroidElement.setIntent(i2);

		AboutPage ap = new AboutPage(this).isRTL(false).setImage(R.drawable.icon).setDescription(Navit.get_text("Welcome to ZANavi offline Navigation")).addItem(new Element().setTitle("Version " + Navit.ZANAVI_VERSION)).addItem(BugsElement).addEmail("zoff@zanavi.cc").addWebsite("http://zanavi.cc/")
		// .addYoutube("UCoghiC-cOCZyq6PGehpm5wA")
				.addPlayStore("com.zoffcc.applications.zanavi").addItem(fdroidElement).addGitHub("zoff99/zanavi");

		Element e001 = new Element();
		e001.setTitle("OpenStreetMap data is available under the Open Database Licence");
		String url001 = "http://www.openstreetmap.org/copyright";
		Intent i001 = new Intent(Intent.ACTION_VIEW);
		i001.setData(Uri.parse(url001));
		e001.setIntent(i001);
		ap.addItem(e001);

		ap.addItem(getCopyRightsElement());

		View aboutPage = ap.create();
		setContentView(aboutPage);
	}

	@SuppressLint("DefaultLocale")
	Element getCopyRightsElement()
	{
		Element copyRightsElement = new Element();
		// final String copyrights = String.format("Copyright © %1$d", Calendar.getInstance().get(Calendar.YEAR));
		final String copyrights = String.format("Copyright © 2016");
		copyRightsElement.setTitle(copyrights);
		copyRightsElement.setColor(ContextCompat.getColor(this, mehdi.sakout.aboutpage.R.color.about_item_icon_color));
		copyRightsElement.setGravity(Gravity.CENTER);
		copyRightsElement.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Toast.makeText(ZANaviAboutPage.this, copyrights, Toast.LENGTH_SHORT).show();
			}
		});
		return copyRightsElement;
	}
}
