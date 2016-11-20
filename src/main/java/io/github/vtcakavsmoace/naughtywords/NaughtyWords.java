package io.github.vtcakavsmoace.naughtywords;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
//import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
//import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

/* This program is free software. It comes without any warranty, to
 * the extent permitted by applicable law. You can redistribute it
 * and/or modify it under the terms of the Do What The Fuck You Want
 * To Public License, Version 2, as published by Sam Hocevar. See
 * http://www.wtfpl.net/ for more details. */
public class NaughtyWords extends JavaPlugin implements Listener {

	/**
	 * Naughty words.
	 */
	private List<String> naughtyNaughty;

	/**
	 * Called when enabled. If this fails, validate that you have your naughty
	 * words set.
	 */
	@Override
	public void onEnable() {
		assureDataFolder();
		hardReloadPlayers();
		getNaughtyWords();
		getServer().getPluginManager().registerEvents(this, this);
		getLogger().info("NaughtyWords started.");
	}

	private void assureDataFolder() {
		if (!getDataFolder().exists()) {
			getLogger().warning("Did not find data folder, making...");
			getDataFolder().mkdir();
		}
		getLogger().info("Data folder loaded.");
	}

	private void hardReloadPlayers() {
		NaughtinessLevel.hardWipeCurrent();
		for (Player player : getServer().getOnlinePlayers())
			establishNaughtinessLevel(player);
		getLogger().info("Players hard reloaded.");
	}

	private void getNaughtyWords() {
		if (naughtyNaughty == null)
			naughtyNaughty = new ArrayList<String>();
		File naughtyWords = new File(getDataFolder(), "naughtywords.txt");
		if (naughtyWords.exists()) {
			try (Scanner sc = new Scanner(naughtyWords)) {
				while (sc.hasNextLine()) {
					String nl = sc.nextLine();
					if (nl == null)
						break;
					nl = nl.trim().toLowerCase();
					if (nl != "")
						naughtyNaughty.add(nl);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				naughtyWords.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Called when disabled. If this fails, you really need to check your spigot
	 * configuration.
	 */
	@Override
	public void onDisable() {
		deregisterAllPlayers();
		naughtyNaughty.clear();
		HandlerList.unregisterAll((Listener) this);
		getLogger().info("NaughtyWords killed.");
	}

	private void deregisterAllPlayers() {
		for (Player player : getServer().getOnlinePlayers())
			deregisterPlayer(player, NaughtinessLevel.getNaughtyLevel(player.getUniqueId().toString()).name());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onLogin(PlayerLoginEvent event) {
		String player = event.getPlayer().getUniqueId().toString();
		establishNaughtinessLevel(event.getPlayer());
		getLogger().info(event.getPlayer().getName() + " loaded as " + NaughtinessLevel.getNaughtyLevel(player) + ".");
	}

	private void establishNaughtinessLevel(Player player) {
		File loadPlayer = new File(getDataFolder(), player.getUniqueId().toString());
		if (loadPlayer.exists()) {
			try {
				String level = new String(Files.readAllBytes(loadPlayer.toPath()));
				NaughtinessLevel.assignNaughtiness(player.getUniqueId().toString(), level);
				getLogger().info(player.getName() + " read to be " + level + ".");
			} catch (Exception e) {
				e.printStackTrace();
				appallingCatch(player);
			}
		}
	}

	private void deregisterPlayer(Player player, String level) {
		saveNaughtinessLevel(player, level);
		NaughtinessLevel.unassignNaughtiness(player.getUniqueId().toString());
		getLogger().info(player.getName() + " unloaded.");
	}

	private void saveNaughtinessLevel(Player player, String level) {
		try {
			File savePlayer = new File(getDataFolder(), player.getUniqueId().toString());
			if (savePlayer.exists())
				savePlayer.delete();
			savePlayer.createNewFile();
			FileWriter fw = new FileWriter(savePlayer, true);
			PrintWriter pw = new PrintWriter(fw);
			pw.write(level);
			pw.flush();
			pw.close();
			getLogger()
					.info("Naughtiness level for player " + player.getName() + " was saved as " + level + "!");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void appallingCatch(Player player) {
		getLogger().warning("Setting player " + player.getName() + "'s naughtiness to 'Appalling'!");
		NaughtinessLevel.appalling.naughtyList.add(player.getUniqueId().toString());
	}

	/**
	 * EventHandler for when a player uses Chat. If they swear, their message
	 * will be cancelled, they will be ridiculed, and they will be kicked (if
	 * below ban threshold).
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (event.isCancelled())
			return;

		Player player = event.getPlayer();
		String msg = event.getMessage().toLowerCase();

		for (String naughty : naughtyNaughty) {
			if (msg.contains(naughty)) {

				NaughtinessLevel naughtyLevel = NaughtinessLevel.incrementNaughtinessLevel(player.getUniqueId().toString());

				event.setCancelled(true);
				player.performCommand("me said a naughty word!");

				String playername = player.getName();

				if (naughtyLevel != NaughtinessLevel.banworthy) {
					Bukkit.getScheduler().runTask(this,
							new SyncKick(player, "You said a naughty word, you " + naughtyLevel.name() + " person!", naughtyLevel.name()));
					getLogger().info("The offender (" + playername + ") was kicked!");
				} else {
					Bukkit.getBanList(Type.NAME).addBan(playername,
							"You said enough naughty words, you " + naughtyLevel.name() + " person! You are banned!",
							null, "NaughtyWords");
					Bukkit.getScheduler().runTask(this,
							new SyncKick(player, "You said a naughty word, you " + naughtyLevel.name() + " person!", naughtyLevel.name()));
					getLogger().info("The offender (" + playername + ") was banned!");
				}

				return;
			}
		}

	}

	private class SyncKick implements Runnable {
		final Player playerToKick;
		final String reason;
		final String level;

		public SyncKick(Player playerToKick, String reason, String level) {
			this.playerToKick = playerToKick;
			this.reason = reason;
			this.level = level;
		}

		public void run() {
			playerToKick.kickPlayer(reason);
			deregisterPlayer(playerToKick, level);
		}
	}

}
