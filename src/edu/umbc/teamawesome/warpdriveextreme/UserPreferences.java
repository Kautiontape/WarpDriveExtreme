package edu.umbc.teamawesome.warpdriveextreme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;


public class UserPreferences 
{
	private final static String PREFS_NAME = "app prefs";	
	private final static String kShowTutorialKey = "show tut key";
	private final static String kMusicEnabledKey = "is music enabled key";
	private final static String kSoundEnabledKey = "is sound enabled key";
	private final static String kHighScoreKey = "high score key";

	public static void setShowTutorial(Context ctx, boolean shouldShowTutorial)
	{
		SharedPreferences prefs = ctx.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

		prefs.edit().putBoolean(kShowTutorialKey, shouldShowTutorial).commit();
	}

	public static boolean getShowTutorial(Context ctx)
	{
		SharedPreferences prefs = ctx.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getBoolean(kShowTutorialKey, true);
	}
		
	public static void setMusicEnabled(Context ctx, boolean shouldShowTutorial)
	{
		SharedPreferences prefs = ctx.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

		prefs.edit().putBoolean(kMusicEnabledKey, shouldShowTutorial).commit();
	}

	public static boolean getMusicEnabled(Context ctx)
	{
		SharedPreferences prefs = ctx.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getBoolean(kMusicEnabledKey, true);
	}

	public static void setSoundEnabled(Context ctx, boolean shouldShowTutorial)
	{
		SharedPreferences prefs = ctx.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

		prefs.edit().putBoolean(kSoundEnabledKey, shouldShowTutorial).commit();
	}

	public static boolean getSoundEnabled(Context ctx)
	{
		SharedPreferences prefs = ctx.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getBoolean(kSoundEnabledKey, true);
	}

	/*
	 * By storing the high scores in the form of "<score>:<user>", we're able to utilize SharedPrefs using it's StringSet,
	 * since that form helps avoid duplicates by using the name to differentiate scores, although the final list won't allow
	 * duplicate scores from duplicate users, and will also order users with the same score in reverse alphabetical.
	 */
	public static void addHighScore(Context ctx, String newScore, String user)
	{
		ArrayList<String> highScores = new ArrayList<String>(getHighScores(ctx));
		highScores.add(newScore + ":" + user);
				
		setHighScores(ctx, new HashSet<String>(getSortedHighScores(highScores)));
	}
	
	public static boolean checkHighScore(Context ctx, String score)
	{
		ArrayList<String> highScores = new ArrayList<String>(getHighScores(ctx));
		
		if(highScores.size() < 10)
		{
			return true;
		}
		
		String lowestScore = highScores.get(highScores.size() - 1).split(":")[0];
		
		int comp = score.compareTo(lowestScore);

		
		if(score.compareTo(lowestScore) == 1)
		{
			return true;
		}
		
		return false;
	}
	
	private static ArrayList<String> getSortedHighScores(ArrayList<String> highScores)
	{
		HashMap<String, String> map = new HashMap<String, String>();

		for(String score:highScores)
		{
			map.put(score.split(":")[0], score.split(":")[1]);
		}
		
		ArrayList<String> scoreKeys = new ArrayList<String>(map.keySet());
		
		Collections.sort(scoreKeys, new Comparator<String>() {

			@Override
			public int compare(String lhs, String rhs) {
				if(Integer.valueOf(lhs) < Integer.valueOf(rhs))
					return -1;
				else if(Integer.valueOf(lhs) > Integer.valueOf(rhs))
					return 1;
				return 0;
			}
		});
		Collections.reverse(scoreKeys);
		
		if(scoreKeys.size() > 10)
		{
			scoreKeys.remove(scoreKeys.size() - 1);
		}
		
		highScores = new ArrayList<String>();
		
		for(String score:scoreKeys)
		{
			highScores.add(score + ":" + map.get(score));
		}

		return highScores;
	}
	
	private static void setHighScores(Context ctx, Set<String> highScores)
	{
		SharedPreferences prefs = ctx.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

		prefs.edit().putStringSet(kHighScoreKey, highScores).commit();
	}

	public static ArrayList<String> getHighScores(Context ctx)
	{
		SharedPreferences prefs = ctx.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		ArrayList<String> highScores = new ArrayList<String>(prefs.getStringSet(kHighScoreKey, new HashSet<String>()));

		return getSortedHighScores(highScores);
	}
	
}
