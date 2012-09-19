/**
 * 
 */
package de.incoherent.suseconferenceclient.app;

import de.suse.conferenceclient.R;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.widget.TextView;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class AboutDialog extends Dialog {
    private Context context;
    public AboutDialog(Context context) {
            super(context, R.style.PlainDialog);
            this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.about_dialog);
            TextView version = (TextView) findViewById(R.id.versionTextView);
            String versionName = "Unknown Version";
            try {
                    versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            } catch (NameNotFoundException e) {
                    e.printStackTrace();
            }
            version.setText(versionName);
            TextView homePage = (TextView) findViewById(R.id.homePageLink);
            homePage.setLinksClickable(true);
            homePage.setText("https://github.com/mbarringer/suseconferenceclient");
            Linkify.addLinks(homePage, Linkify.WEB_URLS);
    }

}
