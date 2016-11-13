package com.zoffcc.applications.zanavi;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.zoffcc.applications.logging.Logging;
import com.zoffcc.applications.logging.Logging.AsyncResponse;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
@SuppressLint("NewApi")
public class ZANaviAboutPage extends AppCompatActivity implements AsyncResponse
{
	ProgressDialog progressDialog2;

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

		AboutPage ap = new AboutPage(this).isRTL(false).setImage(R.drawable.icon).setDescription(Navit.get_text("Welcome to ZANavi offline Navigation")).addItem(new Element().setTitle("Version " + Navit.ZANAVI_VERSION)).addItem(BugsElement).addEmail("feedback@zanavi.cc").addWebsite("http://zanavi.cc/")
		// .addYoutube("UCoghiC-cOCZyq6PGehpm5wA")
				.addPlayStore("com.zoffcc.applications.zanavi").addItem(fdroidElement).addGitHub("zoff99/zanavi");

		Element e001 = new Element();
		e001.setTitle("OpenStreetMap data is available under the Open Database Licence");
		String url001 = "http://www.openstreetmap.org/copyright";
		Intent i001 = new Intent(Intent.ACTION_VIEW);
		i001.setData(Uri.parse(url001));
		e001.setIntent(i001);
		ap.addItem(e001);

		e001 = new Element();
		e001.setTitle(Navit.get_text("send Crash report via Email"));
		e001.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				try
				{
					progressDialog2 = ProgressDialog.show(ZANaviAboutPage.this, "", Navit.get_text("reading crash info ..."));

					progressDialog2.setCanceledOnTouchOutside(false);
					progressDialog2.setOnCancelListener(new DialogInterface.OnCancelListener()
					{
						@Override
						public void onCancel(DialogInterface dialog)
						{
						}
					});

					// get logcat messages ----------------
					Logging x = new Logging();
					Logging.delegate = ZANaviAboutPage.this;
					x.new PopulateLogcatAsyncTask(ZANaviAboutPage.this.getApplicationContext()).execute();
					// get logcat messages ----------------

				}
				catch (Exception e)
				{
				}
			}
		});
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

	@Override
	public void processFinish(String output_part1)
	{
		String output = output_part1 + System.getProperty("line.separator") + System.getProperty("line.separator") + "LastStackTrace:" + System.getProperty("line.separator") + ZANaviMainApplication.last_stack_trace_as_string;
		ZANaviMainApplication.last_stack_trace_as_string = ""; // reset last stacktrace

		System.out.println("ZANaviAboutPage:" + "processFinish");

		String date = new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.GERMAN).format(new Date());
		String full_file_name = Navit.NAVIT_DATA_DEBUG_DIR + "/crashlog_" + date + ".txt";
		String full_file_name_suppl = Navit.NAVIT_DATA_DEBUG_DIR + "/crashlog_single.txt";
		String feedback_text = Navit.get_text("Crashlog") + "\n" + Navit.get_text("You can use our PGP-Key") + ": " + Navit.PGP_KEY_ID;

		System.out.println("crashlogfile=" + full_file_name);

		ZANaviLogMessages.am(ZANaviLogMessages.STATUS_INFO, "ZANaviAboutPage:" + "crashlogfile=" + full_file_name);

		Logging.writeToFile(output, ZANaviAboutPage.this, full_file_name);

		String subject_d_version = "";
		if (Navit.Navit_DonateVersion_Installed)
		{
			subject_d_version = subject_d_version + "D,";
		}

		if (Navit.Navit_Largemap_DonateVersion_Installed)
		{
			subject_d_version = subject_d_version + "L,";
		}

		try
		{
			int rl = Navit.get_reglevel();

			if (rl > 0)
			{
				subject_d_version = "U" + rl + ",";
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		String FD_addon = "";
		if (Navit.FDBL)
		{
			FD_addon = ",FD";
		}

		try
		{
			new Handler().post(new Runnable()
			{
				@Override
				public void run()
				{
					progressDialog2.dismiss();
					System.out.println("ZANaviAboutPage:" + "progressDialog2.dismiss()");
				}
			});
		}
		catch (Exception ee)
		{
		}

		Navit.Global_Navit_Object.sendEmailWithAttachment(this, "feedback@zanavi.cc", "ZANavi Crashlog (v:" + subject_d_version + FD_addon + Navit.NavitAppVersion + " a:" + android.os.Build.VERSION.SDK + ")", feedback_text, full_file_name, full_file_name_suppl);

		// reset message
		ZANaviMainApplication.last_stack_trace_as_string = "";
		PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).edit().putString("last_crash_text", ZANaviMainApplication.last_stack_trace_as_string).commit();

		// reset flag
		Navit.intro_flag_crash = false;
	}
}
