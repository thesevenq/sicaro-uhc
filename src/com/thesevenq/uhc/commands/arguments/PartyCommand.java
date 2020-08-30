package com.thesevenq.uhc.commands.arguments;

import com.thesevenq.uhc.utilties.Color;
import com.thesevenq.uhc.utilties.Msg;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.thesevenq.uhc.UHC;
import com.thesevenq.uhc.commands.BaseCommand;
import com.thesevenq.uhc.listeners.ChatListener;
import com.thesevenq.uhc.managers.BorderManager;
import com.thesevenq.uhc.managers.GameManager;
import com.thesevenq.uhc.managers.PartyManager;
import com.thesevenq.uhc.managers.RequestManager;
import com.thesevenq.uhc.player.party.Party;
import com.thesevenq.uhc.player.party.PartyRequest;
import com.thesevenq.uhc.player.UHCData;
import com.thesevenq.uhc.player.state.GameState;
import com.thesevenq.uhc.utilties.UHCUtils;

import java.util.ArrayList;
import java.util.List;

public class PartyCommand extends BaseCommand {

	private UHC plugin = UHC.getInstance();

	public PartyCommand(UHC plugin) {
		super(plugin);
		
		this.command = "party";
		this.forPlayerUseOnly = true;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;

		if(args.length == 0) {
			if(!PartyManager.isEnabled()) {
				player.sendMessage(Color.translate("&cParties are currently disabled."));
			} else {
				this.sendUsage(sender);
			}
		} else if(args[0].equalsIgnoreCase("create")) {
			if(!PartyManager.isEnabled()) {
				player.sendMessage(Color.translate("&cParties are currently disabled."));
				return;
			}
			
			if(GameManager.getGameState().equals(GameState.PLAYING)) {
				player.sendMessage(Color.translate("&cThe game is already running."));
				return;
			}

			if(PartyManager.getByPlayer(player) != null) {
				player.sendMessage(Color.translate("&7You are already in a party."));
				return;
			}

			plugin.getPartyManager().handleCreateParty(player);
		} else if(args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("inv")) {
			if (!PartyManager.isEnabled()) {
				player.sendMessage(Color.translate("&cParties are currently disabled."));
				return;
			}
			
			if(GameManager.getGameState().equals(GameState.PLAYING)) {
				player.sendMessage(Color.translate("&cThe game is already running."));
				return;
			}
			
			if(args.length == 1) {
				player.sendMessage(Color.translate("&cUsage: /party invite <player>"));
				return;
			}

			Player target = Bukkit.getPlayer(args[1]);

			if(Msg.checkOffline(player, args[1])) return;

			if(target == player) {
				player.sendMessage(Color.translate("&cYou can't invite yourself to your party."));
				return;
			}

			Party party = PartyManager.getByPlayer(player);

			if(party == null) {
				plugin.getPartyManager().handleCreateParty(player);
				RequestManager.handleSendRequest(player, target, PartyManager.getByPlayer(player));
				return;
			}

			if(party.getOwner() != player) {
				player.sendMessage(Color.translate("&cYou must be party leader to invite player to your party."));
				return;
			}

			RequestManager.handleSendRequest(player, target, PartyManager.getByPlayer(player));
		} else if(args[0].equalsIgnoreCase("kick")) {
			if(!PartyManager.isEnabled()) {
				player.sendMessage(Color.translate("&cParties are currently disabled."));
				return;
			}
			
			if(GameManager.getGameState().equals(GameState.PLAYING)) {
				player.sendMessage(Color.translate("&cThe game is already running."));
				return;
			}

			if(args.length == 1) {
				player.sendMessage(Color.translate("&cUsage: /party kick <player>"));
				return;
			}

			Party party = PartyManager.getByPlayer(player);

			if(party == null) {
				player.sendMessage(Color.translate("&7You are not in a party."));
				return;
			}

			if(party.getOwner() != player) {
				player.sendMessage(Color.translate("&cYou must be party leader to kick players from your party."));
				return;
			}

			Player target = Bukkit.getPlayer(args[1]);

			if(Msg.checkOffline(player, args[1])) return;

			if(target == player) {
				player.sendMessage(Color.translate("&cYou can't kick your self."));
				return;
			}

			if(!party.getPlayers().contains(target.getUniqueId())) {
				player.sendMessage(Color.translate("&c&l" + target.getName() + " &cisn't in your party."));
				return;
			}

			plugin.getPartyManager().handleUnregisterParty(target.getName());

			if(target.isOnline()) {
				target.getPlayer().sendMessage(Color.translate("&cYou have been kicked from the party by &l" + player.getName() + "&c."));
			}

			party.broadcast("&c&l" + target.getName() + " &chas been kicked from the party by &l" + player.getName() + "&c.");

		} else if(args[0].equalsIgnoreCase("show") || args[0].equalsIgnoreCase("who") || args[0].equalsIgnoreCase("info")) {
			if(!PartyManager.isEnabled()) {
				player.sendMessage(Color.translate("&cParties are currently disabled."));
				return;
			}

			if(args.length == 1) {
				Party party = PartyManager.getByPlayer(player);
				
				if(party == null) {
					player.sendMessage(Color.translate("&7You are not in a party."));
					return;
				}

				UHCData leaderData = UHCData.getByName(party.getOwner().getName());
				String leaderName = (leaderData.isAlive() ? "&a" : "&c") + party.getOwner().getName();

				List<String> users = new ArrayList<>(party.getPlayers());
				users.remove(party.getOwner().getName());

				StringBuilder builder = new StringBuilder();

				users.forEach(user -> {
					if(builder.length() > 0) {
						builder.append("&f, ");
					}

					OfflinePlayer member = Bukkit.getOfflinePlayer(user);
					UHCData memberData = UHCData.getByName(member.getName());

					builder.append((memberData.isAlive() ? "&a" : "&c")).append(member.getName());
				});

				player.sendMessage(Msg.BIG_LINE);
				player.sendMessage(Color.translate("&3Party Information:"));
				player.sendMessage(Color.translate("&bLeader: " + leaderName));
				player.sendMessage(Color.translate("&bMembers (&3" + party.getPlayers().size() + "&b): " + builder.toString()));
				player.sendMessage(Msg.BIG_LINE);
				return;
			}

            Player target = Bukkit.getPlayer(args[1]);

            if(Msg.checkOffline(player, args[1])) return;

			Party party = PartyManager.getByPlayer(target);
			
			if(party == null) {
				player.sendMessage(Color.translate("&c&l" + target.getName() + " &cisn't in a party."));
				return;
			}

			UHCData leaderData = UHCData.getByName(party.getOwner().getName());
			String leaderName = (leaderData.isAlive() ? "&a" : "&c") + party.getOwner().getName();

			List<String> users = new ArrayList<>(party.getPlayers());
			users.remove(party.getOwner().getName());

			StringBuilder builder = new StringBuilder();

			users.forEach(user -> {
				if(builder.length() > 0) {
					builder.append("&f, ");
				}

				OfflinePlayer member = Bukkit.getOfflinePlayer(user);
				UHCData memberData = UHCData.getByName(member.getName());

				builder.append((memberData.isAlive() ? "&a" : "&c")).append(member.getName());
			});

			player.sendMessage(Msg.BIG_LINE);
			player.sendMessage(Color.translate("&3Party Information:"));
			player.sendMessage(Color.translate("&bLeader: " + leaderName));
			player.sendMessage(Color.translate("&bMembers (&3" + party.getPlayers().size() + "&b): " + builder.toString()));
			player.sendMessage(Msg.BIG_LINE);
		} else if(args[0].equalsIgnoreCase("leave")) {
			if(!PartyManager.isEnabled()) {
				player.sendMessage(Color.translate("&cParties are currently disabled."));
				return;
			}
			
			if(GameManager.getGameState().equals(GameState.PLAYING)) {
				player.sendMessage(Color.translate("&cThe game is already running."));
				return;
			}

			Party party = PartyManager.getByPlayer(player);

			if(party == null) {
				player.sendMessage(Color.translate("&7You are not in a party."));
				return;
			}

			plugin.getPartyManager().handleUnregisterParty(player.getName());

			player.getInventory().clear();
			UHCUtils.loadLobbyInventory(player);
		} else if(args[0].equalsIgnoreCase("accept")) {
			if(!PartyManager.isEnabled()) {
				player.sendMessage(Color.translate("&cParties are currently disabled."));
				return;
			}
			
			if(GameManager.getGameState().equals(GameState.PLAYING)) {
				player.sendMessage(Color.translate("&cThe game is already running."));
				return;
			}
			
			PartyRequest partyRequest = RequestManager.getByPlayer(player);

			if(partyRequest == null) {
				player.sendMessage(Color.translate("&cYou don't have pending invites."));
				return;
			}

			plugin.getPartyManager().handleRegisterParty(player, partyRequest.getParty());
			RequestManager.getRequests().remove(player);

		} else if(args[0].equalsIgnoreCase("deny")) {
			if(!PartyManager.isEnabled()) {
				player.sendMessage(Color.translate("&cParties are currently disabled."));
				return;
			}
			
			if(GameManager.getGameState().equals(GameState.PLAYING)) {
				player.sendMessage(Color.translate("&cThe game is already running."));
				return;
			}

			PartyRequest partyRequest = RequestManager.getByPlayer(player);

			if(partyRequest == null) {
				player.sendMessage(Color.translate("&cYou don't have pending invites."));
				return;
			}

			RequestManager.declined(player);
		} else if(args[0].equalsIgnoreCase("chat") || args[0].equalsIgnoreCase("c")) {
			if(!PartyManager.isEnabled()) {
				player.sendMessage(Color.translate("&cParties are currently disabled."));
				return;
			}
			
			Party party = PartyManager.getByPlayer(player);

			if(party == null) {
				player.sendMessage(Color.translate("&7You are not in a party."));
				return;
			}

			if(ChatListener.chat.contains(player.getUniqueId())) {
				ChatListener.chat.remove(player.getUniqueId());
				
				player.sendMessage(Color.translate("&bYou have &cDisabled &3Team Chat&7."));
			} else {
				ChatListener.chat.add(player.getUniqueId());
				
				player.sendMessage(Color.translate("&bYou have &a&lEnabled &3Team Chat&7."));
			}
		} else if(args[0].equalsIgnoreCase("coords")) {
			if(!PartyManager.isEnabled()) {
				player.sendMessage(Color.translate("&cParties are currently disabled."));
				return;
			}

			Party party = PartyManager.getByPlayer(player);

			if(party == null) {
				player.sendMessage(Color.translate("&7You are not in a party."));
				return;
			}

			int x = player.getLocation().getBlockX();
			int y = player.getLocation().getBlockY();
			int z = player.getLocation().getBlockZ();

			party.broadcast("&3" + player.getName() + " &bCoords -> x&3- &3" + x + "&7, y&3- &3" + y + "&7, z&3- &3" + z + "&7.");
		} else if(args[0].equalsIgnoreCase("damage")) {
			if(!PartyManager.isEnabled()) {
				player.sendMessage(Color.translate("&cParties are currently disabled."));
				return;
			}

			Party party = PartyManager.getByPlayer(player);

			if(party == null) {
				player.sendMessage(Color.translate("&7You are not in a party."));
				return;
			}
			
			if(BorderManager.border == 50) {
				player.sendMessage(Color.translate("&cYou can't edit party damage when border is 50."));
				return;
			}
			
			if(party.isDamageMembers()) {
				party.setDamageMembers(false);
				
				party.broadcast("&3" + party.getOwner().getName() + " &bhas &cdisabled&b party damage.");
			} else {
				party.setDamageMembers(true);
				
				party.broadcast("&3" + party.getOwner().getName() + " &bhas &aenabled&b party damage.");
			}
		}
	}

	public void sendUsage(CommandSender sender) {
		sender.sendMessage(Color.translate("&7&m---------------------------------------"));
		sender.sendMessage(Color.translate("&9&lParty Help"));
		sender.sendMessage(Color.translate("&7&m---------------------------------------"));
		sender.sendMessage(Color.translate("&9General Commands:"));
		sender.sendMessage(Color.translate("&b/party create &7- Create a new party"));
		sender.sendMessage(Color.translate("&b/party invite <playerName> &7- Invite players to your party"));
		sender.sendMessage(Color.translate("&b/party kick <playerName> &7- Kick player from your current party"));
		sender.sendMessage(Color.translate("&b/party show <playerName> &7- Check someones party info"));
		sender.sendMessage(Color.translate("&b/party leave &7- Leave your current party"));
		sender.sendMessage(Color.translate("&b/party accept &7- Accept party invitation"));
		sender.sendMessage(Color.translate("&b/party deny &7- Deny party invitation"));
		sender.sendMessage(Color.translate("&b/party chat &7- Toggle party chat"));
		sender.sendMessage(Color.translate("&b/party coords &7- Send your teammate's coords"));
		sender.sendMessage(Color.translate("&b/party damage &7- Enable/Disable party damage"));
		sender.sendMessage(Color.translate("&7&m---------------------------------------"));
	}

}
