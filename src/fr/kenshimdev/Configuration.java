package fr.kenshimdev;

import java.io.File;
import java.util.Arrays;

public class Configuration extends Skyoconfig{
	
	//Message si jambe cassé...
	@ConfigOptions(name = "Options.Msg.Broken")
	public String OptionsMsgBroken = "Hoo... Tu t'es cassé la jambe :( !";
	
	//Amplifier
	@ConfigOptions(name = "Options.Slow.Implifier")
	public int OptionsSlowImplifier = 2;
	
	//FallDistance
	@ConfigOptions(name = "Options.FallDistance.height")
	public int OptionsFallDistance = 6;
	
	//AmountHealing
	@ConfigOptions(name = "Options.Healing.Amount")
	public int OptionsHealingAmount = 10;
	
	public Configuration(final File file) {
		super(file, Arrays.asList("BrokenLeg - Configuration \nExample Implifier : 1,2,3,4 or 5 " ));
	}
	
}
