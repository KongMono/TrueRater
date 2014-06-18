package com.truelife.mobile.android.rate;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AppRater {
    // Preference Constants
    private final static String PREF_NAME = "apprater";
    private final static String PREF_LAUNCH_COUNT = "launch_count";
    private final static String PREF_FIRST_LAUNCHED = "date_firstlaunch";
    private final static String PREF_DONT_SHOW_AGAIN = "dontshowagain";
    private final static String PREF_REMIND_LATER = "remindmelater";
    private final static String PREF_APP_VERSION_NAME = "app_version_name";
    private final static String PREF_APP_VERSION_CODE = "app_version_code";

    private final static int DAYS_UNTIL_PROMPT = 3;
    private final static int LAUNCHES_UNTIL_PROMPT = 7;
    private static int DAYS_UNTIL_PROMPT_FOR_REMIND_LATER = 3;
    private static int LAUNCHES_UNTIL_PROMPT_FOR_REMIND_LATER = 7;
    private static boolean isDark;
    private static boolean themeSet;
    private static boolean hideNoButton;
    private static boolean isVersionNameCheckEnabled;
    private static boolean isVersionCodeCheckEnabled;
    
    
    private static String Message;
    private static String textLater;
    private static String textRate;
    private static String textNothank;
    private static AlertDialog helpDialog;

    private static Market market = new GoogleMarket();
    
    
    
    /**
     * Set message
     *
     * @param message
     */
    public static void setMessage(String message) {
    	Message = message;
    }
    
   /**
    * Set message textrater
    *
    * @param textlater
    */
    public static void setTextLater(String textlater) {
    	textLater = textlater;
    }
    
    /**
     * Set message textrate
     *
     * @param textrate
     */
     public static void setTextRate(String textrate) {
    	 textRate = textrate;
     }

     
     /**
      * Set message textnothank
      *
      * @param textnothank
      */
      public static void setTextNoThank(String textnothank) {
    	  textNothank = textnothank;
      }


    /**
     * Decides if the version name check is active or not
     *
     * @param versionNameCheck
     */
    public static void setVersionNameCheckEnabled(boolean versionNameCheck) {
        isVersionNameCheckEnabled = versionNameCheck;
    }

    /**
     * Decides if the version code check is active or not
     *
     * @param versionCodeCheck
     */
    public static void setVersionCodeCheckEnabled(boolean versionCodeCheck) {
        isVersionCodeCheckEnabled = versionCodeCheck;
    }

    /**
     * sets number of day until rating dialog pops up for next time when remind
     * me later option is chosen
     *
     * @param daysUntilPromt
     */
    public static void setNumDaysForRemindLater(int daysUntilPromt) {
        DAYS_UNTIL_PROMPT_FOR_REMIND_LATER = daysUntilPromt;
    }

    /**
     * sets the number of launches until the rating dialog pops up for next time
     * when remind me later option is chosen
     *
     * @param launchesUntilPrompt
     */
    public static void setNumLaunchesForRemindLater(int launchesUntilPrompt) {

        LAUNCHES_UNTIL_PROMPT_FOR_REMIND_LATER = launchesUntilPrompt;
    }

    /**
     * decides if No thanks button appear in dialog or not
     *
     * @param isNoButtonVisible
     */
    public static void setDontRemindButtonVisible(boolean isNoButtonVisible) {
        AppRater.hideNoButton = isNoButtonVisible;
    }

    /**
     * Call this method at the end of your OnCreate method to determine whether
     * to show the rate prompt using the specified or default day, launch count
     * values and checking if the version is changed or not
     *
     * @param context
     */
    public static void app_launched(Context context,Boolean TrueOnly) {
        app_launched(context, DAYS_UNTIL_PROMPT, LAUNCHES_UNTIL_PROMPT,TrueOnly);
    }

    /**
     * Call this method at the end of your OnCreate method to determine whether
     * to show the rate prompt using the specified or default day, launch count
     * values with additional day and launch parameter for remind me later option
     * and checking if the version is changed or not
     *
     * @param context
     * @param daysUntilPrompt
     * @param launchesUntilPrompt
     * @param daysForRemind
     * @param launchesForRemind
     */
    public static void app_launched(Context context, int daysUntilPrompt, int launchesUntilPrompt, int daysForRemind, int launchesForRemind,Boolean TrueOnly) {
        setNumDaysForRemindLater(daysForRemind);
        setNumLaunchesForRemindLater(launchesForRemind);
        app_launched(context, daysUntilPrompt, launchesUntilPrompt,TrueOnly);
    }

    /**
     * Call this method at the end of your OnCreate method to determine whether
     * to show the rate prompt
     *
     * @param context
     * @param daysUntilPrompt
     * @param launchesUntilPrompt
     */
    public static void app_launched(Context context, int daysUntilPrompt, int launchesUntilPrompt,Boolean TrueOnly) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        ApplicationRatingInfo ratingInfo = ApplicationRatingInfo.createApplicationInfo(context);
        int days;
        int launches;
        if (isVersionNameCheckEnabled) {
            if (!ratingInfo.getApplicationVersionName().equals(prefs.getString(PREF_APP_VERSION_NAME, "none"))) {
                editor.putString(PREF_APP_VERSION_NAME, ratingInfo.getApplicationVersionName());
                resetData(context);
                commitOrApply(editor);
            }
        }
        if (isVersionCodeCheckEnabled) {
            if (ratingInfo.getApplicationVersionCode() != (prefs.getInt(PREF_APP_VERSION_CODE, -1))) {
                editor.putInt(PREF_APP_VERSION_CODE, ratingInfo.getApplicationVersionCode());
                resetData(context);
                commitOrApply(editor);
            }
        }
        if (prefs.getBoolean(PREF_DONT_SHOW_AGAIN, false)) {
            return;
        } else if (prefs.getBoolean(PREF_REMIND_LATER, false)) {
            days = DAYS_UNTIL_PROMPT_FOR_REMIND_LATER;
            launches = LAUNCHES_UNTIL_PROMPT_FOR_REMIND_LATER;
        } else {
            days = daysUntilPrompt;
            launches = launchesUntilPrompt;
        }

        // Increment launch counter
        long launch_count = prefs.getLong(PREF_LAUNCH_COUNT, 0) + 1;
        editor.putLong(PREF_LAUNCH_COUNT, launch_count);
        // Get date of first launch
        Long date_firstLaunch = prefs.getLong(PREF_FIRST_LAUNCHED, 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong(PREF_FIRST_LAUNCHED, date_firstLaunch);
        }
        // Wait for at least the number of launches or the number of days used
        // until prompt
        if (launch_count >= launches || (System.currentTimeMillis() >= date_firstLaunch + (days * 24 * 60 * 60 * 1000))) {
            showRateAlertDialog(context, editor,TrueOnly);
        }
        commitOrApply(editor);
    }

    /**
     * Call this method directly if you want to force a rate prompt, useful for
     * testing purposes
     *
     * @param context
     */
    public static void showRateDialog(final Context context,Boolean TrueOnly) {
        showRateAlertDialog(context, null,TrueOnly);
    }

    /**
     * Call this method directly to go straight to play store listing for rating
     *
     * @param context
     */
    public static void rateNow(final Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, market.getMarketURI(context)));
        } catch (ActivityNotFoundException activityNotFoundException1) {
            Log.e(AppRater.class.getSimpleName(), "Market Intent not found");
        }
    }

    /**
     * Set an alternate Market, defaults to Google Play
     *
     * @param market
     */
    public static void setMarket(Market market) {
        AppRater.market = market;
    }

    /**
     * Get the currently set Market
     *
     * @return market
     */
    public static Market getMarket() {
        return market;
    }

    /**
     * Sets dialog theme to dark
     */
    @TargetApi(11)
    public static void setDarkTheme() {
        isDark = true;
        themeSet = true;
    }

    /**
     * Sets dialog theme to light
     */
    @TargetApi(11)
    public static void setLightTheme() {
        isDark = false;
        themeSet = true;
    }

    /**
     * The meat of the library, actually shows the rate prompt dialog
     */
    @SuppressLint("NewApi")
    private static void showRateAlertDialog(final Context context, final SharedPreferences.Editor editor,boolean TrueOnly) {
    	
    	Builder builder = new Builder(context);
    	
    	try {
    		if (TrueOnly) {
        		LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE);
        		ApplicationRatingInfo ratingInfo = ApplicationRatingInfo.createApplicationInfo(context);
        		final View view = inflater.inflate(R.layout.layout_dialog,null);
        		view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
        		
        		TextView headerAppName = (TextView) view.findViewById(R.id.textAppname);
        		
    			headerAppName.setText(context.getResources().getIdentifier("app_displayname", "string", context.getPackageName()));
        		
        		
        		TextView header = (TextView) view.findViewById(R.id.textheader);
        		
        		if (Util.isBlank(Message)) {
        			header.setText(String.format(context.getString(R.string.rate_message), ratingInfo.getApplicationName()));
        		}else{
        			header.setText(Message);
        		}
        		ListView listView = (ListView) view.findViewById(R.id.list);
        		
        		int i = 0;
        		final SparseArray<String> array = new SparseArray<String>();
        		
        		if (Util.isBlank(textRate)) {
        			array.put(i++, String.format(context.getString(R.string.rate), ratingInfo.getApplicationName()));
				}else{
					array.put(i++, textRate);
				}
        		
        		if (Util.isBlank(textLater)) {
        			array.put(i++, context.getString(R.string.later));
				}else{
					array.put(i++, textLater);
				}
        		
        		if (Util.isBlank(textNothank)) {
        			array.put(i++, context.getString(R.string.no_thanks));
				}else{
					array.put(i++, textNothank);
				}
        		
        		ListDialogAdapter adapter = new ListDialogAdapter(context,array);
        		listView.setAdapter(adapter);
        		
        		listView.setOnItemClickListener(new OnItemClickListener() {

        			@Override
        			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
        				
        				switch (array.keyAt(position)) {
    					case 0:
    						 rateNow(context);
    						 	if (editor != null) {
    	                            editor.putBoolean(PREF_DONT_SHOW_AGAIN, true);
    	                            commitOrApply(editor);
    	                        }
    	                        helpDialog.dismiss();
    						break;
    					case 1:
    						 if (editor != null) {
    	                            Long date_firstLaunch = System.currentTimeMillis();
    	                            editor.putLong(PREF_FIRST_LAUNCHED, date_firstLaunch);
    	                            editor.putLong(PREF_LAUNCH_COUNT, 0);
    	                            editor.putBoolean(PREF_REMIND_LATER, true);
    	                            editor.putBoolean(PREF_DONT_SHOW_AGAIN, false);
    	                            commitOrApply(editor);
    	                     }
    					 	 helpDialog.dismiss();
    						break;
    					case 2:
    						if (editor != null) {
                                editor.putBoolean(PREF_DONT_SHOW_AGAIN, true);
                                editor.putBoolean(PREF_REMIND_LATER, false);
                                long date_firstLaunch = System.currentTimeMillis();
                                editor.putLong(PREF_FIRST_LAUNCHED, date_firstLaunch);
                                editor.putLong(PREF_LAUNCH_COUNT, 0);
                                commitOrApply(editor);
                            }
    						helpDialog.dismiss();
    						break;

    					default:
    						helpDialog.dismiss();
    						break;
    					}
        			}
        		});
        		
        		builder.setView(view);
        		
        		helpDialog = builder.create();
        		helpDialog.show();
        		
    		}else{
    	        if (Build.VERSION.SDK_INT >= 11 && themeSet) {
    	            builder = new AlertDialog.Builder(context, (isDark ? AlertDialog.THEME_HOLO_DARK : AlertDialog.THEME_HOLO_LIGHT));
    	        } else {
    	            builder = new AlertDialog.Builder(context);
    	        }
    	        ApplicationRatingInfo ratingInfo = ApplicationRatingInfo.createApplicationInfo(context);
    	        builder.setTitle(String.format(context.getString(R.string.dialog_title), ratingInfo.getApplicationName()));

    	        builder.setMessage(String.format(context.getString(R.string.rate_message), ratingInfo.getApplicationName()));

    	        builder.setPositiveButton(String.format(context.getString(R.string.rate), ratingInfo.getApplicationName()),
    	                new DialogInterface.OnClickListener() {
    	                    public void onClick(DialogInterface dialog, int id) {
    	                        rateNow(context);
    	                        if (editor != null) {
    	                            editor.putBoolean(PREF_DONT_SHOW_AGAIN, true);
    	                            commitOrApply(editor);
    	                        }
    	                        dialog.dismiss();
    	                    }
    	                });

    	        builder.setNeutralButton(context.getString(R.string.later),
    	                new DialogInterface.OnClickListener() {
    	                    public void onClick(DialogInterface dialog, int id) {
    	                        if (editor != null) {
    	                            Long date_firstLaunch = System.currentTimeMillis();
    	                            editor.putLong(PREF_FIRST_LAUNCHED, date_firstLaunch);
    	                            editor.putLong(PREF_LAUNCH_COUNT, 0);
    	                            editor.putBoolean(PREF_REMIND_LATER, true);
    	                            editor.putBoolean(PREF_DONT_SHOW_AGAIN, false);
    	                            commitOrApply(editor);
    	                        }
    	                        dialog.dismiss();
    	                    }
    	                });
    	        if (!hideNoButton) {
    	            builder.setNegativeButton(context.getString(R.string.no_thanks),
    	                    new DialogInterface.OnClickListener() {
    	                        public void onClick(DialogInterface dialog, int id) {
    	                            if (editor != null) {
    	                                editor.putBoolean(PREF_DONT_SHOW_AGAIN, true);
    	                                editor.putBoolean(PREF_REMIND_LATER, false);
    	                                long date_firstLaunch = System.currentTimeMillis();
    	                                editor.putLong(PREF_FIRST_LAUNCHED, date_firstLaunch);
    	                                editor.putLong(PREF_LAUNCH_COUNT, 0);
    	                                commitOrApply(editor);
    	                            }
    	                            dialog.dismiss();
    	                        }
    	                    });
    	        }
    	        builder.show();
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}
        
    }
    
    /**
     * The meat of the library, actually shows the rate prompt dialog
     */
    @SuppressLint("NewApi")
    private static void showRateAlertDialog(final Context context, final SharedPreferences.Editor editor) {
        Builder builder;
        if (Build.VERSION.SDK_INT >= 11 && themeSet) {
            builder = new AlertDialog.Builder(context, (isDark ? AlertDialog.THEME_HOLO_DARK : AlertDialog.THEME_HOLO_LIGHT));
        } else {
            builder = new AlertDialog.Builder(context);
        }
        ApplicationRatingInfo ratingInfo = ApplicationRatingInfo.createApplicationInfo(context);
        builder.setTitle(String.format(context.getString(R.string.dialog_title), ratingInfo.getApplicationName()));

        builder.setMessage(String.format(context.getString(R.string.rate_message), ratingInfo.getApplicationName()));

        builder.setPositiveButton(String.format(context.getString(R.string.rate), ratingInfo.getApplicationName()),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        rateNow(context);
                        if (editor != null) {
                            editor.putBoolean(PREF_DONT_SHOW_AGAIN, true);
                            commitOrApply(editor);
                        }
                        dialog.dismiss();
                    }
                });

        builder.setNeutralButton(context.getString(R.string.later),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (editor != null) {
                            Long date_firstLaunch = System.currentTimeMillis();
                            editor.putLong(PREF_FIRST_LAUNCHED, date_firstLaunch);
                            editor.putLong(PREF_LAUNCH_COUNT, 0);
                            editor.putBoolean(PREF_REMIND_LATER, true);
                            editor.putBoolean(PREF_DONT_SHOW_AGAIN, false);
                            commitOrApply(editor);
                        }
                        dialog.dismiss();
                    }
                });
        if (!hideNoButton) {
            builder.setNegativeButton(context.getString(R.string.no_thanks),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (editor != null) {
                                editor.putBoolean(PREF_DONT_SHOW_AGAIN, true);
                                editor.putBoolean(PREF_REMIND_LATER, false);
                                long date_firstLaunch = System.currentTimeMillis();
                                editor.putLong(PREF_FIRST_LAUNCHED, date_firstLaunch);
                                editor.putLong(PREF_LAUNCH_COUNT, 0);
                                commitOrApply(editor);
                            }
                            dialog.dismiss();
                        }
                    });
        }
        builder.show();
    }

    @SuppressLint("NewApi")
    private static void commitOrApply(SharedPreferences.Editor editor) {
        if (Build.VERSION.SDK_INT > 8) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    public static void resetData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREF_DONT_SHOW_AGAIN, false);
        editor.putBoolean(PREF_REMIND_LATER, false);
        editor.putLong(PREF_LAUNCH_COUNT, 0);
        long date_firstLaunch = System.currentTimeMillis();
        editor.putLong(PREF_FIRST_LAUNCHED, date_firstLaunch);
        commitOrApply(editor);
    }
}