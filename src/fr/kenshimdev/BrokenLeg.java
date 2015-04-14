package fr.kenshimdev;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BrokenLeg extends JavaPlugin implements Listener{	
	private Configuration config; 
	public Logger log = Logger.getLogger("Minecraft");
	public static Economy econ = null;
	public boolean DrinkMilk = false;

	@Override
	public void onEnable(){
		this.getServer().getPluginManager().registerEvents(this, this);

		final Logger logger = getLogger();
		try {
			config = new Configuration(new File(this.getDataFolder(), "config.yml"));
			config.load(); // Chargement de la config depuis le fichier spécifié ci-dessus.
		}
		catch(final InvalidConfigurationException ex) {
			ex.printStackTrace();
			logger.log(Level.SEVERE, "Oooops ! Something went wrong while loading the configuration !");
			Bukkit.getPluginManager().disablePlugin(this);
		}

		////SETUP API ECONOMY
		if (!setupEconomy() ) {
			log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		log.info("[BrokenLeg] has been enabled !");
	}

	@Override
	public void onDisable() {
		log.info("[BrokenLeg] has been disabled !");	
		try {
			config.save(); // Sauvegarde de la config dans le fichier.
		}
		catch(final InvalidConfigurationException ex) {
			ex.printStackTrace();
			getLogger().log(Level.SEVERE, "Oooops ! Something went wrong while saving the configuration !");
		}
	}

	//SETUP API ECONOMY
	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	/*
	 * Liste des commandes disponible en jeu
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)){
			sender.sendMessage("Cette commande ne peut être effectué uniquement par un joueur !");
			return true;
		}

		Player player = (Player) sender;
		if (label.equalsIgnoreCase("bl")) {
			if (args.length == 0){
				sendMessageHelp(player);
			}else if (args.length == 1 && args[0].equalsIgnoreCase("heal")){
				player.sendMessage(ChatColor.GOLD + "[BL] " + ChatColor.RED + "Veuillez saisir le bon argument...");
				return false;
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase("npc")){
				if(sender.hasPermission("brokenleg.npc")|| sender.isOp()){
					Player p = (Player) sender;
					spawnHEALER(p);
					p.sendMessage(ChatColor.GOLD + "[BL] " + ChatColor.GREEN + "Bravo ! Votre soigneur a bien été placé :) !");
				}else{
					player.sendMessage(ChatColor.GOLD + "[BL] " + ChatColor.RED + "Vous ne possédez pas la permission d'effectuer cette action !");
					return false;
				}
			}
			else if(args.length >= 2 && args[0].equalsIgnoreCase("npc")){
				if(args[1].equalsIgnoreCase("remove")){
					if(sender.hasPermission("brokenleg.npc.remove")|| sender.isOp()){
						Player p = (Player) sender;
						for(Entity entity : p.getNearbyEntities(10, 10, 10)){
							if(entity instanceof Villager){
								Villager v = (Villager) entity;
								if(v.getCustomName().equalsIgnoreCase(ChatColor.GOLD + "[BL]" + ChatColor.GREEN + "Soigneur")){
									v.remove();
									p.sendMessage(ChatColor.GOLD + "[BL] " + ChatColor.GREEN + "Le soigneur se trouvant dans une zone de 10 blocs autour de vous a été supprimé !");
								}
							}
						}
					}else{
						player.sendMessage(ChatColor.GOLD + "[BL] " + ChatColor.RED + "Vous ne possédez pas la permission d'effectuer cette action !");
						return false;					
					}
				}else{
					player.sendMessage(ChatColor.GOLD + "[BL] " + ChatColor.RED + "Vous ne possédez pas la permission d'effectuer cette action !");
					return false;
				}
			}
			else if (args.length >= 2){
				String cmd = args[0]; //1er argument
				if(cmd.equalsIgnoreCase("heal")){
					if(sender.hasPermission("brokenleg.healing")|| sender.isOp()){
						String name = args[1]; //2ème argument
						if (player.getServer().getPlayer(name) != null) {
							Player targetPlayer = player.getServer().getPlayer(name);
							targetPlayer.removePotionEffect(PotionEffectType.SLOW);
							String SenderName = sender.getName();
							targetPlayer.sendMessage(ChatColor.GOLD + "Votre jambe a été soigné par " + ChatColor.GREEN +SenderName + ChatColor.GOLD +" !");
						} else {
							player.sendMessage(ChatColor.GOLD + "[BL] " +ChatColor.RED + "Le joueur ciblé n'est pas en ligne !");
						}
					}else{
						player.sendMessage(ChatColor.GOLD + "[BL] " + ChatColor.RED + "Vous ne possédez pas la permission d'effectuer cette action !");
					}
				}
			}
		}
		return false;
	}

	//bl help...
	public void sendMessageHelp(Player p){
		p.sendMessage(ChatColor.MAGIC + "======== " + ChatColor.GOLD + "BROKENLEG" + ChatColor.WHITE + ChatColor.MAGIC + " ========");
		p.sendMessage(ChatColor.GOLD + "Liste des commandes : ");
		p.sendMessage(ChatColor.AQUA + "/bl heal <player>     " + ChatColor.GREEN +" << " + ChatColor.AQUA + "Soigne la jambe d'un joueur");
		p.sendMessage(ChatColor.AQUA + "/bl npc               " + ChatColor.GREEN +" << " + ChatColor.AQUA + "Spawn un NPC soigneur");
		p.sendMessage(ChatColor.AQUA + "/bl npc remove        " + ChatColor.GREEN +" << " + ChatColor.AQUA + "Supprime les NPC dans une zone des 10 blocs autour de vous");
	}


	/*
	 * Création du villageois permettant de se soigner...
	 */
	public void spawnHEALER(Player player){
		Villager v = (Villager) player.getLocation().getWorld().spawn(player.getLocation(), Villager.class);	

		v.setCustomName(ChatColor.GOLD + "[BL]" + ChatColor.GREEN + "Soigneur");
		v.setCustomNameVisible(true);
		v.setAgeLock(true);
	}

	/*
	 * Permet de rendre invincible notre PNJ...
	 */
	@EventHandler
	public void VillagerAttack(EntityDamageEvent event){
		Entity en = (Entity) event.getEntity();
		if(en instanceof Villager){
			Villager v = (Villager) en;
			if(v.getCustomName().equalsIgnoreCase(ChatColor.GOLD + "[BL]" + ChatColor.GREEN + "Soigneur")){
				event.setCancelled(true);
			}
		}
	}
	
	/*
	 * Event soin quand on clique droit sur un villageois
	 */
	@EventHandler
	public void VillagerInteract(PlayerInteractEntityEvent e){
		if(e.getRightClicked() != null){
			Player p = (Player) e.getPlayer();
			Entity en = (Entity) e.getRightClicked();
			if(en instanceof Villager){
				Villager v = (Villager) en;
				if(v.getCustomName().equalsIgnoreCase(ChatColor.GOLD + "[BL]" + ChatColor.GREEN + "Soigneur")){
					//On set un prix pour se réparer la jambe
					EconomyResponse r = econ.withdrawPlayer(p.getName(), config.OptionsHealingAmount);
					if(r.transactionSuccess()){
						e.setCancelled(true);
						p.removePotionEffect(PotionEffectType.SLOW);
						Location lp = p.getLocation();
						p.playSound(lp, Sound.FIREWORK_LAUNCH, 5, 0);
						p.playSound(lp, Sound.CLICK, 5, 0);
						p.sendMessage(ChatColor.GOLD + "[BL] " + ChatColor.GREEN + "Vous avez été soigné par " + v.getCustomName());
					}
				}
			}
		}
	}

	/*
	 * Permet d'activer l'effet "jambe cassé"
	 */
	@EventHandler
	public void onFall(EntityDamageEvent event){
		if ((event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) && ((event.getEntity() instanceof Player))){
			Player player = (Player)event.getEntity();
			int hauteur = (int)player.getFallDistance();
			int FallDistance = (int)config.OptionsFallDistance;

			if (hauteur >= FallDistance && !player.hasPermission("brokenleg.none") && !player.isOp()){
				int implifier = this.config.OptionsSlowImplifier;
				if (implifier <= 5)
				{
					player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2147483647, this.config.OptionsSlowImplifier));
					player.sendMessage(ChatColor.RED + this.config.OptionsMsgBroken);
				}
				else
				{
					player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2147483647, 2));
					player.sendMessage(ChatColor.RED + this.config.OptionsMsgBroken);
				}
			}
		}
	}

	/*
	 * Autorise/Interdit l'utilisation du lait pour se soigner
	 */
	@EventHandler
	public void onDrinkMilk(PlayerInteractEvent event) {
		if(DrinkMilk == true){
			Player player = (Player) event.getPlayer();
			player.sendMessage(ChatColor.GOLD + "[BL] " + ChatColor.GREEN +"Vous êtes autorisé à vous soigner avec le lait !");
		}else if(DrinkMilk == false){
			Player player = (Player) event.getPlayer();
			if(player.getItemInHand().getType() == Material.MILK_BUCKET){
				event.setCancelled(true);
				player.sendMessage(ChatColor.GOLD + "[BL] " +  ChatColor.RED +"Vous êtes pas autorisé à vous soigner avec le lait !");
			}
		}
	}
}