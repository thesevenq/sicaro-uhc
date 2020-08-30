package com.thesevenq.uhc.commands.arguments;

import com.thesevenq.uhc.utilties.Color;
import org.bukkit.command.CommandSender;
import com.thesevenq.uhc.UHC;
import com.thesevenq.uhc.commands.BaseCommand;
import com.thesevenq.uhc.managers.GameManager;
import com.thesevenq.uhc.player.UHCData;
import com.thesevenq.uhc.player.state.GameState;

import java.util.*;
import java.util.stream.Collectors;

public class TopKillsCommand extends BaseCommand {

	public TopKillsCommand(UHC plugin) {
		super(plugin);
		
		this.command = "topkills";
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!GameManager.getGameState().equals(GameState.PLAYING)) {
			sender.sendMessage(Color.translate("&cUHC is currently not running."));
			return;
		}

		HashMap<String, Integer> players = new HashMap<String, Integer>();

		for(UHCData uhcData : UHCData.getUhcDatas().values()) {
			players.put(uhcData.getName(), uhcData.getKills());
		}
        
		sender.sendMessage(Color.translate("&7&m---------------------------"));
		sender.sendMessage(Color.translate("&bTop 10 Killers:"));
		sender.sendMessage("");

		List<UHCData> kills = new ArrayList<>(UHCData.getUhcDatas().values().stream().filter(x -> x instanceof UHCData).map(x -> (UHCData) x).filter(x -> x.getKills() > 0).collect(Collectors.toSet()));
		Collections.sort(kills, KILLS_COMPARATOR);
		Collections.reverse(kills);
		
		for(int i = 0; i < 10; i++) {
			if(i >= kills.size()) {
				break;
			}

			UHCData next = kills.get(i);

			if(next.isAlive()) {
				sender.sendMessage(Color.translate("&7" + (i + 1) + ") &a" + next.getName() + "&7: &3" + next.getKills()));
			} else {
				sender.sendMessage(Color.translate("&7" + (i + 1) + ") &c" + next.getName() + "&7: &3" + next.getKills()));
			}
		}
		
		sender.sendMessage(Color.translate("&7&m---------------------------"));
		
	}
	
    public static final Comparator<UHCData> KILLS_COMPARATOR = (player1, player2) -> {
        return Integer.compare(player1.getKills(), player2.getKills());
    };
}
