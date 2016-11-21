package io.github.vtcakavsmoace.naughtywords;

import java.util.ArrayList;
import java.util.List;

/* This program is free software. It comes without any warranty, to
 * the extent permitted by applicable law. You can redistribute it
 * and/or modify it under the terms of the Do What The Fuck You Want
 * To Public License, Version 2, as published by Sam Hocevar. See
 * http://www.wtfpl.net/ for more details. */
public enum NaughtinessLevel {

	divine, cheeky, naughty, ghastly, appalling, banworthy;

	List<String> naughtyList;

	public static NaughtinessLevel incrementNaughtinessLevel(String player) {
		switch (getNaughtyLevel(player)) {
		case appalling:
			if (banworthy.naughtyList == null)
				banworthy.naughtyList = new ArrayList<String>();
			appalling.naughtyList.remove(player);
			banworthy.naughtyList.add(player);
			return banworthy;
		case banworthy:
			return banworthy;
		case cheeky:
			if (naughty.naughtyList == null)
				naughty.naughtyList = new ArrayList<String>();
			cheeky.naughtyList.remove(player);
			naughty.naughtyList.add(player);
			return naughty;
		case ghastly:
			if (appalling.naughtyList == null)
				appalling.naughtyList = new ArrayList<String>();
			ghastly.naughtyList.remove(player);
			appalling.naughtyList.add(player);
			return appalling;
		case naughty:
			if (ghastly.naughtyList == null)
				ghastly.naughtyList = new ArrayList<String>();
			naughty.naughtyList.remove(player);
			ghastly.naughtyList.add(player);
			return ghastly;
		default:
			if (cheeky.naughtyList == null)
				cheeky.naughtyList = new ArrayList<String>();
			cheeky.naughtyList.add(player);
			return cheeky;
		}
	}

	public static NaughtinessLevel getNaughtyLevel(String player) {
		for (NaughtinessLevel level : NaughtinessLevel.values()) {
			if (level.naughtyList != null && level.naughtyList.contains(player)) {
				return level;
			}
		}
		return divine;
	}

	public static void assignNaughtiness(String string, String naughtyLevel) throws Exception {
		NaughtinessLevel naughtiness;
		try {
			naughtiness = valueOf(naughtyLevel);
		} catch (Exception e) {
			naughtiness = divine;
		}

		unassignNaughtiness(string);

		if (naughtiness.naughtyList == null)
			naughtiness.naughtyList = new ArrayList<String>();
		
		naughtiness.naughtyList.add(string);
	}

	public static boolean unassignNaughtiness(String player) {
		NaughtinessLevel level = getNaughtyLevel(player);
		if (level.naughtyList != null)
			return level.naughtyList.remove(player);
		return false;
	}

	public static void hardWipeCurrent() {
		for (NaughtinessLevel level : values())
			if (level.naughtyList != null)
				level.naughtyList.clear();
	}

}