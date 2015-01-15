package com.google.vrtoolkit.cardboard;

import android.content.pm.*;
import java.util.*;
import android.net.*;
import android.widget.*;
import android.content.*;
import android.os.*;
import android.app.*;

class UiUtils
{
    private static final String CARDBOARD_WEBSITE = "http://google.com/cardboard/cfg?vrtoolkit_version=0.5.1";
    private static final String CARDBOARD_CONFIGURE_ACTION = "com.google.vrtoolkit.cardboard.CONFIGURE";
    private static final String INTENT_EXTRAS_VERSION_KEY = "VERSION";
    private static final String NO_BROWSER_TEXT = "No browser to open website.";
    
    static void launchOrInstallCardboard(final Context context) {
        final PackageManager pm = context.getPackageManager();
        final Intent settingsIntent = new Intent();
        settingsIntent.setAction("com.google.vrtoolkit.cardboard.CONFIGURE");
        settingsIntent.putExtra("VERSION", "0.5.1");
        final List<ResolveInfo> resolveInfos = (List<ResolveInfo>)pm.queryIntentActivities(settingsIntent, 0);
        final List<Intent> intentsToGoogleCardboard = new ArrayList<Intent>();
        for (final ResolveInfo info : resolveInfos) {
            final String pkgName = info.activityInfo.packageName;
            if (pkgName.startsWith("com.google.")) {
                final Intent intent = new Intent(settingsIntent);
                intent.setClassName(pkgName, info.activityInfo.name);
                intentsToGoogleCardboard.add(intent);
            }
        }
        if (intentsToGoogleCardboard.isEmpty()) {
            showInstallDialog(context);
        }
        else if (intentsToGoogleCardboard.size() == 1) {
            showConfigureDialog(context, intentsToGoogleCardboard.get(0));
        }
        else {
            showConfigureDialog(context, settingsIntent);
        }
    }
    
    private static void showInstallDialog(final Context context) {
        final DialogInterface.OnClickListener listener = (DialogInterface.OnClickListener)new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                try {
                    context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("http://google.com/cardboard/cfg?vrtoolkit_version=0.5.1")));
                }
                catch (ActivityNotFoundException e) {
                    Toast.makeText(context.getApplicationContext(), (CharSequence)"No browser to open website.", 1).show();
                }
            }
        };
        final FragmentManager fragmentManager = ((Activity)context).getFragmentManager();
        final DialogFragment dialog = new SettingsDialogFragment((DialogStrings)new InstallDialogStrings(), listener);
        dialog.show(fragmentManager, "InstallCardboardDialog");
    }
    
    private static void showConfigureDialog(final Context context, final Intent intent) {
        final DialogInterface.OnClickListener listener = (DialogInterface.OnClickListener)new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                try {
                    context.startActivity(intent);
                }
                catch (ActivityNotFoundException e) {
                    showInstallDialog(context);
                }
            }
        };
        final FragmentManager fragmentManager = ((Activity)context).getFragmentManager();
        final DialogFragment dialog = new SettingsDialogFragment((DialogStrings)new ConfigureDialogStrings(), listener);
        dialog.show(fragmentManager, "ConfigureCardboardDialog");
    }
    
    private static class DialogStrings
    {
        String mTitle;
        String mMessage;
        String mPositiveButtonText;
        String mNegativeButtonText;
    }
    
    private static class InstallDialogStrings extends DialogStrings
    {
        InstallDialogStrings() {
            super();
            this.mTitle = "Configure";
            this.mMessage = "Get the Cardboard app in order to configure your viewer.";
            this.mPositiveButtonText = "Go to Play Store";
            this.mNegativeButtonText = "Cancel";
        }
    }
    
    private static class ConfigureDialogStrings extends DialogStrings
    {
        ConfigureDialogStrings() {
            super();
            this.mTitle = "Configure";
            this.mMessage = "Set up your viewer for the best experience.";
            this.mPositiveButtonText = "Setup";
            this.mNegativeButtonText = "Cancel";
        }
    }
    
    private static class SettingsDialogFragment extends DialogFragment
    {
        private DialogStrings mDialogStrings;
        private DialogInterface.OnClickListener mPositiveButtonListener;
        
        private SettingsDialogFragment(final DialogStrings dialogStrings, final DialogInterface.OnClickListener positiveButtonListener) {
            super();
            this.mDialogStrings = dialogStrings;
            this.mPositiveButtonListener = positiveButtonListener;
        }
        
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            final AlertDialog.Builder builder = new AlertDialog.Builder((Context)this.getActivity());
            builder.setTitle((CharSequence)this.mDialogStrings.mTitle).setMessage((CharSequence)this.mDialogStrings.mMessage).setPositiveButton((CharSequence)this.mDialogStrings.mPositiveButtonText, this.mPositiveButtonListener).setNegativeButton((CharSequence)this.mDialogStrings.mNegativeButtonText, (DialogInterface.OnClickListener)null);
            return (Dialog)builder.create();
        }
    }
}
