package net.edwebb.mi.extract;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.edwebb.mi.data.DataStore;
import net.edwebb.mi.data.Foe;
import net.edwebb.mi.data.Item;


/**
 * @author aaw129
 * @version 1.0 : 11 Mar 2011
 */
public class MIReader {

	private static final int MODE_HEADER = 0;
	private static final int MODE_ORDER = 1;
	private static final int MODE_STATS = 2;
	private static final int MODE_SIGHT = 3;

	private static final int MODE_TORCH = 12;
	private static final int MODE_ROPE = 13;
	private static final int MODE_LOOT = 14;
	private static final int MODE_EQUIP = 15; 
	private static final int MODE_SCRYE = 16;
	private static final int MODE_OPPONENT = 17;
	private static final int MODE_MUSH = 18;

	private static final int MODE_BATTLE = 21;
	private static final int MODE_WRESTLE = 22;
	private static final int MODE_DEF_SPELL = 23;
	private static final int MODE_OPP_DEF_SPELL = 24;
	private static final int MODE_MISSILE = 25;
	private static final int MODE_OPP_MISSILE = 26;
	private static final int MODE_OFF_SPELL = 27;
	private static final int MODE_OPP_OFF_SPELL = 28;
	private static final int MODE_MELEE = 29;
	private static final int MODE_GRAPPLE = 30;
	private static final int MODE_SALVAGE = 31;
	private static final int MODE_LOOTING = 32;
	private static final int MODE_SKILL = 33;
	private static final int MODE_MAP = 34;

	private int shots = 0; // something to record the number of shots.
	
	private boolean statsOnly = false;
	
	private static final List<String> numbers = new ArrayList<String>();
	static {
		numbers.add("zero");
		numbers.add("one");
		numbers.add("two");
		numbers.add("three");
		numbers.add("four");
		numbers.add("five");
		numbers.add("six");
		numbers.add("seven");
		numbers.add("eight");
		numbers.add("nine");
		numbers.add("ten");
		numbers.add("eleven");
		numbers.add("twelve");
		numbers.add("thirteen");
		numbers.add("fourteen");
		numbers.add("fifteen");
		numbers.add("sixteen");
	}

	private List<Integer> mode;

	private String[] words;
	private int pos;
	private Turn turn;
	private Encounter enc;
	private Stats stats;
	private Stats opponent;

	private void setUp(int x, int y, Stats stats) {
		mode = new ArrayList<Integer>();
		words = null;
		pos = 0;
		turn = new Turn();
		turn.setX(x);
		turn.setY(y);
		if (stats != null) {
			this.stats = stats;
			turn.setArmourClass(stats.getArmourClass());
		} else {
			this.stats = new Stats(turn);
		}
		enc = turn;
	}
	
	public Turn read(File file, int x, int y, Stats stats) throws FileNotFoundException, IOException {
		return read(new FileReader(file), x, y, stats, false);
	}
	public Turn read(File file, int x, int y, Stats stats, boolean statsOnly) throws FileNotFoundException, IOException {
		return read(new FileReader(file), x, y, stats, statsOnly);
	}

	public Turn read(Reader reader, int x, int y, Stats stats) throws IOException {
		return read(reader, x, y ,stats, false);
	}

	public Turn read(Reader reader, int x, int y, Stats stats, boolean statsOnly) throws IOException {
		BufferedReader br = new BufferedReader(reader);
		StringBuffer sb = new StringBuffer();
		String line = br.readLine();
		while (line != null) {
			sb.append(line);
			sb.append("\n");
			line = br.readLine();
		}
		br.close();
		return read(sb.toString(), x, y, stats, statsOnly);
	}

	public Turn read(String text, int x, int y, Stats stats) {
		return read(text, x, y, stats, false);
	}

	public Turn read(String text, int x, int y, Stats stats, boolean statsOnly) {
		this.statsOnly = statsOnly;
		setUp(x, y, stats);
		mode.add(MODE_HEADER);

		words = text.split("\\s+");

		for (pos = 0; pos < words.length; pos++) {
			if (mode.size() == 0) {
				break;
			}
			switch (mode.get(0)) {
			case MODE_HEADER:
				checkHeader();
				break;
			case MODE_STATS:
				checkStats();
				break;
			case MODE_ORDER:
				checkLocation();
				checkBattle();
				checkMuscle();
				checkOrder();
				break;
			case MODE_EQUIP:
				checkEquip();
				checkOrder();
				break;
			case MODE_OPPONENT:
				checkMonsterEquip();
				checkOrder();
				break;
			case MODE_SKILL:
				checkSkill();
				checkOrder();
				break;
			case MODE_TORCH:
			case MODE_LOOT:
			case MODE_ROPE:
			case MODE_MUSH:
				checkLocation();
				checkTreasure();
				checkBattle();
				checkStat();
				checkOrder();
				break;
			case MODE_BATTLE:
			case MODE_WRESTLE:
				checkCreature();
				checkCombatResult();
				checkOrder();
				break;
			case MODE_DEF_SPELL:
				checkDefSpell();
			case MODE_OPP_DEF_SPELL:
				checkOppDefSpell();
			case MODE_MISSILE:
			case MODE_OPP_MISSILE:
				checkMissile();
			case MODE_OFF_SPELL:
				checkOffSpell();
			case MODE_OPP_OFF_SPELL:
				checkOppOffSpell();
			case MODE_MELEE:
				checkMelee();
			case MODE_SALVAGE:
				checkSalvage();
				checkCombatResult();
				checkOrder();
				break;
			case MODE_LOOTING:
				checkLooting();
				checkOrder();
			break;
			case MODE_SCRYE:
				checkScrye();
				checkOrder();
			break;
			case MODE_MAP:
				checkMap();
			break;
			case MODE_SIGHT:
				checkSightings();
				checkOrder();
			break;
			case MODE_GRAPPLE:
				checkGrapple();
				checkSalvage();
				checkCombatResult();
				checkOrder();
			break;
			}
		}

		int startMuscle = turn.getStats().getStats().get("Muscle") - turn.getMuscle();
		setMuscle(startMuscle, turn);

		return turn;
	}

	private void checkEquip() {
		if (match("Item#")) {
			pos++;
			if (match("62")) {
				// Shingle Poison
				mode.remove(0);
				return;
			}
			pos ++;
			StringBuffer sb = new StringBuffer();
			while (!match("now equipped.")) {
				sb.append(words[pos]);
				sb.append(" ");
				pos++;
			}
			updateEquipment(sb.toString().trim(), stats);
		}
		mode.remove(0);
	}

	private void updateEquipment(String equip, Stats theStats) {
		Item item = DataStore.getInstance().getItem(equip);
		if (item == null) {
			System.out.println("Cannot work out what this: " + equip + " is.");
		} else if (item.getEquipType() == null) {
			System.out.println("This: " + equip + " doesn't have an equipment type.");
		} else {
			String equipClass = item.getEquipType().name();
			if ("BaEdPtPlWp".contains(equipClass)) {
				theStats.getEquip().put("Primary Weapon:", equip);
			} else if ("Mi".equals(equipClass)) {
				theStats.getEquip().put("Missile Weapon:", equip);
			} else if ("Gr".equals(equipClass)) {
				theStats.getEquip().put("Greaves:", equip);
			} else if ("Ga".equals(equipClass)) {
				theStats.getEquip().put("Gauntlets:", equip);
			} else if ("He".equals(equipClass)) {
				theStats.getEquip().put("Helm:", equip);
			} else if ("Bo".equals(equipClass)) {
				theStats.getEquip().put("Body Armor:", equip);
			} else if ("De".equals(equipClass)) {
				theStats.getEquip().put("Defensive Weapon:", equip);
			} else if ("Am".equals(equipClass)) {
				theStats.getEquip().put("Amulet:", equip);
			} else if ("Ch".equals(equipClass)) {
				theStats.getEquip().put("Charm:", equip);
			} else if ("Ri".equals(equipClass)) {
				theStats.getEquip().put("Ring:", equip);
			} else {
				System.out.println("Cannot work out what this: " + equip + " is.");
			}
		}
	}
	
	private void checkSkill() {
		if (match("Level *")) {
			int level = getNumber(words[pos+1]);
			String type = words[pos+3];
			stats.getStats().put(type + ":", level);
			mode.remove(0);
		}
	}
	
	private void setMuscle(int start, Encounter enc) {
		Iterator<Encounter> encs = enc.getEncounters().iterator();
		while (encs.hasNext()) {
			Encounter child = encs.next();
			child.setMuscle(start + child.getMuscle());
			setMuscle(start, child);
		}
	}

	private void checkHeader() {
		if (match("TURN#") || match("TURNtt")) {
			turn.setTurnNumber(Integer.parseInt(words[pos + 1]));
			pos++;
		} else if (match("MONSTER#") || match ("MONSTERtt")) {
			turn.setMonsterNumber(Integer.valueOf(words[pos + 1]));
			pos++;
		} else if (match("* Skin Toughness.]")) {
			if (stats != null && stats.getStats().containsKey("Skin Toughness")) {
				int st = stats.getStats().get("Skin Toughness");
				st += getNumber(words[pos]);
				stats.getStats().put("Skin Toughness", st);
			}
		} else if (match("You break camp.") || match("You exit the fort.") || match("You retrieve your Bird Trap.") || match("didn't recover any health between turns.") || match("to your gain.")) {
			pos += 2;
			if (statsOnly) {
				mode.set(0, MODE_STATS);
			} else {
				mode.set(0, MODE_ORDER);
			}
		} else if (match("You check the list of services and then leave the Inn.")
				|| match("You check the list of services and then leave the Tower.")) {
			pos += 10;
			if (statsOnly) {
				mode.set(0, MODE_STATS);
			} else {
				mode.set(0, MODE_ORDER);
			}
		}
	}

	private void checkOrder() {
		if (match("left.)") 
		 || match("[*Not Processed*]")
		 || match("You check the hive entrance and find it has been sealed tightly.") // Wanna Bee Hive
		 || match("you climb out of the Roach Hollow.") // Roach Hollow
		 || match("You slip back down through the lower layers to the ground.") // Bodden
		) {
			mode.clear();
			mode.add(MODE_ORDER);
			opponent = null;
			while (enc.getParent() != null) {
				enc = enc.getParent();
			}
		} else if (match("C - 757") || match("TT -") || match("PURCHASE FROM RAILWAY STATION")) {
			// Don't know where we are so set the X and Y to crazy co-ordinates
			turn.setX(-1000); turn.setY(-1000);
		} else if (match("U - 33")) {
			mode.add(0, MODE_TORCH);
		} else if (match("U - 385")) {
			mode.add(0, MODE_TORCH);
		} else if (match("U - 162")) {
			mode.add(0, MODE_MUSH);
		} else if (match("U - 28")) {
			mode.add(0, MODE_ROPE);
		} else if (match("LV")) {
			mode.add(0, MODE_LOOT);
		} else if (match("U - 262")) {
			mode.add(0, MODE_SCRYE);
		} else if (match("E - *")) {
			mode.add(0, MODE_EQUIP);
		} else if (match("CROSSING INTO NEXT SQUARE(*")) {
			mode.clear();
			mode.add(MODE_ORDER);
			opponent = null;
			while (enc.getParent() != null) {
				enc = enc.getParent();
			}
			checkSquare();
		} else if (match("Sightings from squares visited this turn:")) {
			mode.clear();
			mode.add(MODE_SIGHT);
		} else if (match("=== START OF MAP DATA ===")) {
			mode.clear();
			mode.add(MODE_MAP);
		} else if (match("Overall Healthiness")) {
			mode.clear();
			mode.add(MODE_STATS);
		} else if (match("Skill Level *")) {
			mode.add(0, MODE_SKILL);
		} else if (match ("equipped with")) {
			mode.add(0, MODE_OPPONENT);
		}
	}

	private void checkSquare() {
		String square = words[pos + 3];
		square = square.substring(7, square.length() - 2);
		String[] coords = square.split(",");
		turn.setX(Integer.valueOf(coords[1]));
		turn.setY(Integer.valueOf(coords[0]));
	}
	
	private void checkBattle() {
		if (match("** BATTLE:")) {
			mode.add(0, MODE_BATTLE);
			BattleEncounter be = new BattleEncounter();
			be.setParent(enc);
			be.setArmourClass(stats.getArmourClass());
			if (opponent != null) {
				be.setOpponentAC(opponent.getArmourClass());
			}
			enc = be;
		} else if (match("** WRESTLING:")) {
			mode.add(0, MODE_WRESTLE);
			BattleEncounter be = new BattleEncounter();
			be.setParent(enc);
			be.setArmourClass(stats.getArmourClass());
			if (opponent != null) {
				be.setOpponentAC(opponent.getArmourClass());
			}
			enc = be;
		} else if (match("You come upon a sleeping Knolltir")) {
			mode.add(0, MODE_BATTLE);
			BattleEncounter be = new BattleEncounter();
			be.setParent(enc);
			be.setArmourClass(stats.getArmourClass());
			enc = be;
		}
	}

	private void checkMuscle() {
		if (match("*+* Muscle*") || match("*-* Muscle*")) {
			turn.setMuscle(turn.getMuscle()
					+ Integer.parseInt(words[pos].replaceAll("[^-0-9]", "")));
		}
	}

	private void checkStat() {
		if (match("* Muscle.]")) {
			turn.setMuscle(turn.getMuscle() + getNumber(words[pos]));
		}
		if (match("* Toughness.]") || match("* Muscle.]") || match("* Stealth.]") || match("* Stealth]") || match("* Max Spell Pts.]")) {
			NumericEncounter stat = new NumericEncounter();
			stat.setQuantity(getNumber(words[pos]));
			stat.setType(words[pos+1]);
			stat.setParent(enc);
		}
		if ((match("* Health.]") || match ("* Health]")) 
			&& !words[pos-1].equals("unwell.") 
			&& !words[pos-1].equals("sickly.") 
			&& !words[pos-1].equals("dizzy.")
			&& !(words[pos-2].equals("alleviate") && words[pos-1].equals("them.")) 
			&& !(words[pos-2].equals("needed") && words[pos-1].equals("now.")) 
			&& !words[pos-1].equals("sustenance.")
			&& !words[pos-1].equals("sick.") 
			&& !words[pos-1].equals("ill.") 
			&& !words[pos-1].equals("famished!") 
			&& !words[pos-1].equals("hungry.") 
			&& !words[pos-1].equals("need.") 
			&& !words[pos-1].equals("Food.)") 
			&& !words[pos-1].equals("food.)") 
			&& !words[pos-1].equals("food.")
			&& (!words[pos-1].equals("floor.") && !words[pos-2].equals("behind."))
			&& !words[pos-1].equals("needed.") 
			&& !words[pos-1].equals("tasty.")
			&& !words[pos-1].equals("Grub.")
			&& (!words[pos-1].equals("unhappy.") && !words[pos-3].equals("empty"))
			&& !words[pos-1].equals("ache.") && !words[pos-2].equals("stomach")) {
			NumericEncounter stat = new NumericEncounter();
			stat.setQuantity(getNumber(words[pos]));
			stat.setType(words[pos+1]);
			stat.setParent(enc);
		}
	}

	private void checkCombatResult() {
		if (match("You've killed it!")) {
			((BattleEncounter) enc).setOutcome(BattleEncounter.CREATURE_KILLED);
			mode.set(0, MODE_SALVAGE);
			opponent = null;
		} else if (match("The Demon Condor did not")) {
			((BattleEncounter) enc).setOutcome(BattleEncounter.MONSTER_FLEE);
			enc = enc.getParent();
			mode.remove(0);
			mode.remove(0);
			opponent = null;
		} else if (match("It's escaped!")
				|| match("It has escaped!")) {
			((BattleEncounter) enc).setOutcome(BattleEncounter.CREATURE_FLEE);
			enc = enc.getParent();
			mode.remove(0);
			mode.remove(0);
			opponent = null;
		} else if (match("has died!")) {
			((BattleEncounter) enc).setOutcome(BattleEncounter.CREATURE_KILLED);
			enc = enc.getParent();
			mode.set(0, MODE_LOOTING);
			opponent = null;
		} else if (match("He's a deader!")) {
			((BattleEncounter) enc).setOutcome(BattleEncounter.CREATURE_KILLED);
			enc = enc.getParent();
			mode.set(0, MODE_LOOTING);
			opponent = null;
		} else if (match("You suddenly come to your senses and scram before your life is terminated.") || match("and successfully flee.") || match("You escape!")) {
			((BattleEncounter) enc).setOutcome(BattleEncounter.MONSTER_FLEE);
			enc = enc.getParent();
			mode.remove(0);
			mode.remove(0);
			opponent = null;
		}
	}

	private void checkDefSpell() {
		if (match("[You decide that your foe isn't too tough")) {
			mode.set(0, MODE_OPP_DEF_SPELL);
		} else if (match("Your opponent's movements become slower.")) {
			mode.set(0, MODE_DEF_SPELL);
			Round rnd = new Round();
			rnd.setType(Round.SPELL);
			rnd.setParent(enc);
			rnd.setWeapon(DataStore.getInstance().getItem("Battle Slowness"));
			mode.set(0, MODE_OPP_DEF_SPELL);
		} else if (match("spell is successful!")) {
			mode.set(0, MODE_DEF_SPELL);
			Round rnd = new Round();
			rnd.setType(Round.SPELL);
			rnd.setParent(enc);
			int i = pos;
			while (i > pos - 10 && i > 0) {
				if (words[i].equals("Your")) {
					rnd.setMonster(true);
					break;
				} else if (words[i].equals("His") || words.equals("Her") || words.equals("Its")) {
					rnd.setMonster(false);
					break;
				}
				i--;
			}
			if (i > pos - 10) {
				StringBuffer sb = new StringBuffer();
				for (int j = i + 1; j < pos; j++) {
					sb.append(words[j]);
					sb.append(" ");
				}
				String spell = sb.toString().trim(); 
				if (spell.equals("Armor")) {
					spell = "Armour";
				}

				rnd.setWeapon(DataStore.getInstance().getItem(spell));
			}
			mode.set(0, MODE_OPP_DEF_SPELL);
		}
	}

	private void checkOppDefSpell() {
		if (match("You note your foe curse as a spell fails.")) {
			mode.set(0, MODE_MISSILE);
		} else if (match("You note that your foe's entire body is glowing.")) {
			mode.set(0, MODE_OPP_DEF_SPELL);
			Round rnd = new Round();
			rnd.setType(Round.SPELL);
			rnd.setMonster(false);
			rnd.setParent(enc);
			rnd.setWeapon(DataStore.getInstance().getItem("Armour"));
			mode.set(0, MODE_MISSILE);
		} else if (match("You note that your foe's entire body is glowing slightly.")) {
			mode.set(0, MODE_OPP_DEF_SPELL);
			Round rnd = new Round();
			rnd.setType(Round.SPELL);
			rnd.setMonster(false);
			rnd.setParent(enc);
			rnd.setWeapon(DataStore.getInstance().getItem("Light Armour"));
			mode.set(0, MODE_MISSILE);
		}
	}

	private void checkMissile() {
		if (match("(You decide this Creature isn't worth expending Missile ammo on.)")) {
			if (mode.get(0).equals(MODE_MISSILE)) {
				mode.set(0, MODE_OPP_MISSILE);
			}
		} else {
			if (match("you fling") || match("you shoot") || match ("you blow") 
			 || match ("you propel") || match("you send") || match("you deliver") 
			 || match("you fire") || match("you discharge") || match("you launch")
			 || match("you project")) {
				shots = getNumber(words[pos + 2]);
			}
			if (match("you spit out") || match("you let loose")) {
				shots = getNumber(words[pos + 3]);
			}
			if (match("flings") || match("shoots") || match ("blows") 
			 || match ("propels") || match("sends") || match("delivers") 
			 || match("fires") || match("discharges") || match("launches")
			 || match("projects")) {
				shots = getNumber(words[pos + 1]);
				pos += 2;
				if (opponent != null) {
					if (opponent.getEquip().get("Missile Weapon:").equals("Blow Pipe")) {
						updateEquipment(getBlowPipeAmmo(), opponent);
					}
				}
			}

			// TODO this needs to be more robust what if the monster has no
			// missile weapon and no off spell, and
			// no battle cry?
			// Need to pick up some "You fire", "You shoot" "You fling" text.
			// TODO need to get the number of projectiles fired
			if (match("Health by")) {
				mode.set(0, MODE_MISSILE);
				Round rnd = new Round();
				rnd.setType(Round.MISSILE);
				rnd.setParent(enc);
				Stats theStats = null;
				if (words[pos - 1].equals("its") || words[pos - 1].equals("his") || words[pos - 1].equals("her")) {
					rnd.setMonster(true);
					theStats = stats;
				} else if (words[pos - 1].equals("your")) {
					rnd.setMonster(false);
					theStats = opponent;
				}
				
				if (theStats != null && theStats.getEquip().get("Missile Weapon:") != null) {
					String weapon = theStats.getEquip().get("Missile Weapon:"); 
					if (weapon != null && weapon.endsWith(",")) {
						weapon = weapon.substring(0, weapon.length()-1);
					}
					//rnd.setDamageClass(DataStore.getInstance().getItemClass(weapon));
					rnd.setWeapon(DataStore.getInstance().getItem(weapon));
					//rnd.setWeapon(weapon);
					if (rnd.isMonster()) {
						rnd.setWeaponSkill(getWeaponSkill(weapon));
					}
				}
				rnd.setShots(shots);
				rnd.setHealth(Integer.parseInt(words[pos + 2].replaceAll("[^-0-9]", "")));
				rnd.setHits(Integer.parseInt(words[pos - 4].replaceAll("[^-0-9]", "")));
				shots = 0;
				pos += 2;

				if (mode.get(0).equals(MODE_MISSILE)) {
					mode.set(0, MODE_OPP_MISSILE);
				} else {
					mode.set(0, MODE_OFF_SPELL);
				}
			}
		}
	}

	private String getBlowPipeAmmo() {
		int i = pos;
		StringBuffer sb = new StringBuffer();
		while (words[i].substring(0,1).equals(words[i].substring(0,1).toUpperCase()) && i - pos < 10) {
			sb.append(words[i]);
			sb.append(" ");
			i++;
		}
		return sb.toString().trim();
	}
	
	private void checkOffSpell() {
		if (match("[You decide not to cast your")) {
			mode.set(0, MODE_OPP_OFF_SPELL);
		} else if (match("You let out a Battle Cry,")) {
			mode.set(0, MODE_MELEE);
		} else if (match("Your weapon glows with power!")) {
			Round rnd = new Round();
			rnd.setParent(enc);
			rnd.setType(Round.SPELL);
			rnd.setWeapon(DataStore.getInstance().getItem("Enchant Weapon"));
			rnd.setMonster(true);
		} else if (match("A cold breeze blows past.")) {
			Round rnd = new Round();
			rnd.setParent(enc);
			rnd.setType(Round.SPELL);
			rnd.setWeapon(DataStore.getInstance().getItem("Ice Storm"));
			rnd.setMonster(true);
			pos += 24;
			rnd.setHealth(getNumber(words[pos]));
		} else if (match("balls of lightning")) {
			Round rnd = new Round();
			rnd.setParent(enc);
			rnd.setType(Round.SPELL);
			rnd.setWeapon(DataStore.getInstance().getItem("Ball Lightning"));
			rnd.setMonster(true);
			rnd.setShots(getNumber(words[pos-1]));
			rnd.setHits(getNumber(words[pos-1]));
			rnd.setHealth(getNumber(words[pos+15]));
		} else {
			// TODO get offensive spell
		}
	}

	private void checkOppOffSpell() {
		if (match("You let out a Battle Cry,")) {
			mode.set(0, MODE_MELEE);
		} else if (match("You're struck by a Lightning Bolt!")) {
			mode.set(0, MODE_OPP_OFF_SPELL);
			Round rnd = new Round();
			rnd.setType(Round.SPELL);
			rnd.setMonster(false);
			rnd.setParent(enc);
			rnd.setWeapon(DataStore.getInstance().getItem("Lightning"));
			rnd.setHealth(getNumber(words[pos+6]));
			mode.set(0, MODE_MISSILE);			
		} else if (match("Rocks and stones begin to fly through the air")) {
			mode.set(0, MODE_OPP_OFF_SPELL);
			Round rnd = new Round();
			rnd.setType(Round.SPELL);
			rnd.setMonster(false);
			rnd.setParent(enc);
			rnd.setWeapon(DataStore.getInstance().getItem("No Item"));
			rnd.setHealth(getNumber(words[pos+19]));
			mode.set(0, MODE_MISSILE);			
		}
	}

	private void checkMelee() {
		if (match("Health by")) {
			mode.set(0, MODE_MELEE);
			Round rnd = new Round();
			rnd.setType(Round.MELEE);
			rnd.setParent(enc);
			Stats theStats = null;
			if (words[pos - 1].equals("its") || words[pos - 1].equals("his") || words[pos - 1].equals("her")) {
				rnd.setMonster(true);
				theStats = stats;
			} else if (words[pos - 1].equals("your")) {
				rnd.setMonster(false);
				theStats = opponent;
			}
			if (theStats != null && theStats.getEquip().get("Primary Weapon:") != null) {
				String weapon = theStats.getEquip().get("Primary Weapon:"); 
				if (weapon != null && weapon.endsWith(",")) {
					weapon = weapon.substring(0, weapon.length()-1);
				}
				//rnd.setDamageClass(DataStore.getInstance().getItemClass(weapon));
				//rnd.setWeapon(weapon);
				rnd.setWeapon(DataStore.getInstance().getItem(weapon));
				if (rnd.isMonster()) {
					rnd.setWeaponSkill(getWeaponSkill(weapon));
				}
			}
			rnd.setHealth(Integer.parseInt(words[pos + 2].replaceAll("[^0-9]", "")));
			rnd.setHits(Integer.parseInt(words[pos - 4].replaceAll("[^0-9]", "")));
		}
	}

	public int getWeaponSkill(String itemName) {
		Item item = DataStore.getInstance().getItem(itemName);
		if (item == null || item.getEquipType() == null) {
			return 0;
		} else {
			String code = item.getEquipType().name();
			if (code.equals("Ba")) {
				return stats.getStats().get("Bashing:");
			} else if (code.equals("Mi")) {
				return stats.getStats().get("Missile:");
			} else if (code.equals("Pl")) {
				return stats.getStats().get("Pole:");
			} else if (code.equals("Pt")) {
				return stats.getStats().get("Pointed:");
			} else if (code.equals("Wp")) {
				return stats.getStats().get("Whip:");
			} else if (code.equals("Ed")) {
				return stats.getStats().get("Edged:");
			} else {
				return 0;
			}
		}
	}

	private void checkGrapple() {
		if (match("damage inflicted.]")) {
			Round rnd = new Round();
			rnd.setType(Round.MELEE);
			rnd.setHealth(getNumber(words[pos - 1], 0));
			rnd.setWeapon(DataStore.getInstance().getItem("Wrestling"));
			rnd.setMonster(enc.getEncounters().size() % 2 == 0 ? true : false);
			rnd.setParent(enc);
		}
	}

	private void checkSalvage() {
		boolean done = false;
		if (match("It's of no value.")) {
			mode.set(0, MODE_SALVAGE);
			done = true;
		} else if (match("You collect * Food.")) {
			mode.set(0, MODE_SALVAGE);
			BattleEncounter be = (BattleEncounter) enc;
			be.setFood(getNumber(words[pos + 2]));
			done = true;
		} else if (match("You collect")) {
			mode.set(0, MODE_SALVAGE);
			BattleEncounter be = (BattleEncounter) enc;
			int i = 2;
			for (; i < 6; i++) {
				int item = getNumber(words[pos + i]);
				if (item > -1) {
					be.setItem(item);
					break;
				}
			}
			while (!words[pos].endsWith(".")) {
				pos++;
			}
			pos++;
			if (!match("You collect")) {
				done = true;
			} else {
				pos--;
			}
		} else if (match("Too bad you can't carry") || match("you've already got") || match("You couldn't salvage")) {
			while (!words[pos].endsWith(".")) {
				pos++;
			}
			pos++;
			if (!match("You collect")) {
				done = true;
			} else {
				pos--;
			}
		}
		
		//TODO see if checking that !match("You collect * Food.") works better
		/*if (done || match("You pause") || match("You retrieve")
				|| match("You move on.") || match("You stumble") 
				|| match("Another Wanna Bee") || match ("You have attained")
				|| match("You wipe") || match("You resolve") || match("You cringe")
				|| match("You trip,") || match ("You walk") || match("You reach")
				|| match("You rest") || match("You find") || match ("Before you")
				|| match("You look") || match("You leap") || match("A Blood Mite")
				|| match("The clatter") || match("A coconut") || match("You make")
				|| match("You eat") || match("You stop") || match("You grin")
				|| match("You kick") || match("The heat") || match("You stagger")
				|| match("In what")) {*/
		if (done) {
			enc = enc.getParent();
			mode.remove(0);
			mode.remove(0);
		}
	}

	private void checkLooting() {
		boolean left = false;
		boolean itemend = false;
		StringBuffer sb = new StringBuffer();
		TreasureEncounter te = new TreasureEncounter();
		te.setQuantity(1);
		
		if (match("You take") || match ("You find") || match("You collect")) {
			while (true) {
				if (match("You find nothing worth taking.")) {
					left = true;
					pos += 4;
				}
				
				if (match("You drag the carcass") || match("You leave the carcass to rot.")) {
					mode.remove(0);
					mode.remove(0);
					enc.setSubEncounterNumber(enc.getSubEncounterNumber() + 1);
					return;
				}
				
				pos++;
				
				if (words[pos].endsWith(".") && !words[pos].equals("Dam.")) {
					itemend = true;
				} else if (words[pos].endsWith(",")) {
					itemend = true;
				}
				
				if (words[pos].substring(0, 1).matches("[A-Z]") && !words[pos].equals("The")) {
					sb.append(words[pos]);
					sb.append(" ");
				} else if (getNumber(words[pos]) > -1) {
					te.setQuantity(getNumber(words[pos]));
				}
				
				if (itemend) {
					te.setParent(enc);

					String item = sb.toString().trim();
					if (item.endsWith(",") || item.endsWith(".")) {
						item = item.substring(0, item.length()-1);
					}
					te.setItem(DataStore.getInstance().getItem(item));
					
					if (left) {
						te.setQuantity(-te.getQuantity());
					}
					
					if (words[pos].endsWith(".")) {
						left = true;
					}
					
					te = new TreasureEncounter();
					te.setQuantity(1);
					sb = new StringBuffer();
					itemend = false;
				}
			}
		}
	}

	private void checkLocation() {
		LocationEncounter le = new LocationEncounter();
		if (match("entered the scary place.")) {
			le.setLocation("Tomb of Mumi");
		} else if (match("you slowly roll the stone to the left.")) {
			le.setLocation("Tomb of Mumi");
		} else if (match("you grip the stone and push it sideways.")) {
			le.setLocation("Tomb of Mumi");
		} else if (match("you move the stone and enter.")) {
			le.setLocation("Tomb of Mumi");
		} else if (match("you gain entry by pushing the stone a bit.")) {
			le.setLocation("Tomb of Mumi");
		} else if (match("you move aside the stone enough to gain entry.")) {
			le.setLocation("Tomb of Mumi");
		} else if (match("You search the mushroom grove")) {
			le.setLocation("Fungus Forest");
		} else if (match("you approach the Loggerhead Camp.")) {
			le.setLocation("Loggerhead Camp");
		} else if (match("You immediately find the rusted gate.")) {
			le.setLocation("Small Quarry");
		} else if (match("You pull and pull and pull and pull but the gate is stuck.")) {
			le.setLocation("Large Quarry");
		} else if (match("you carefully climb over the Gate.")) {
			le.setLocation("Large Quarry");
		} else if (match("The mouth of the shaft is black and gloomy.")) {
			le.setLocation("Mine Shaft");
		} else if (match("A cold breeze issues from the mouth of the shaft.")) {
			le.setLocation("Mine Shaft");
		} else if (match("A small puff of steam rises out of the opening.")) {
			le.setLocation("Mine Shaft");
		} else if (match("A huge timber supports the top of the Mine Shaft opening.")) {
			le.setLocation("Mine Shaft");
		} else if (match("but finally find the Mine Shaft.")) {
			le.setLocation("Mine Shaft");
		} else if (match("You prepare yourself for danger and enter the shaft.")) {
			le.setLocation("Mine Shaft");
		} else if (match("you locate the mouth of the Mine Shaft.")) {
			le.setLocation("Mine Shaft");
		} else if (match("you locate the Mine Shaft easily.")) {
			le.setLocation("Mine Shaft");
		} else if (match("and find the Mine Shaft.")) {
			le.setLocation("Mine Shaft");
		} else if (match("you easily find the Mine Shaft.")) {
			le.setLocation("Mine Shaft");
		} else if (match("You find the mouth of the Mine Shaft on the side of a small hill.")) {
			le.setLocation("Mine Shaft");
		} else if (match("you find the mouth of the Mine Shaft.")) {
			le.setLocation("Mine Shaft");
		} else if (match("You approach the ruins of the temple dedicated to demigod Vlados Xi.")) {
			le.setLocation("Ruined Temple");
			// We will come out in a different square
			turn.setX(-1000);
			turn.setY(-1000);
		} else if (match("You locate the Ancient Graveyard.")) {
			le.setLocation("Ancient Graveyard");
		} else if(match("It's a Wanna Bee Hive!")) {
			le.setLocation("Wanna Bee Hive");
			mode.add(0, MODE_TORCH);
		} else if(match("They are soft and squishy.")) {
			le.setLocation("Giant Tree Squid Nest");
			mode.add(0, MODE_TORCH);
		} else if (match("you are caught in a massive spider web!")) {
			le.setLocation("Giant Silk Spider Web");
			mode.add(0, MODE_TORCH);
		} else if (match("You fall through a rotten spot in the limb into a debris-filled hole!")) {
			le.setLocation("Roach Hollow");
			mode.add(0, MODE_TORCH);
		} else if (match("You locate the wooden doors below a large pile of rubble.")) {
			le.setLocation("Voodoo Workshop");
		} else if (match("you push through the brush and pause for a second.")) {
			le.setLocation("Hillock");
		} else if (match("You then pry away the brush hiding the entrance")) {			
			le.setLocation("Hillock");
		} else if (match("You then clear the brush away from the entrance.")) {
			le.setLocation("Hillock");
		} else if (match("You then pry away the brush hiding the entrance.")) {
			le.setLocation("Hillock");
		} else if (match("At the brush-covered entrance you ready yourself.")) {
			le.setLocation("Hillock");
		} else if (match("There are at least * snakes here.")) {
			le.setLocation("Koma Den");
		} else if (match("There are around * snakes here.")) {
			le.setLocation("Koma Den");
		} else if (match("you fail in your attempt to force the lock.")) {
			le.setLocation("Small Quarry");
		} else if (match("You test the chain and lock but they are too strong to break.")) {
			le.setLocation("Small Quarry");
		} else if (match("find a small, water-filled quarry.")) {
			le.setLocation("Small Quarry");
		} else if (match("you locate the rusted gate.")) {
			le.setLocation("Small Quarry");
		} else if (match("and find the rusted gate.")) {
			le.setLocation("Small Quarry");
		} else if (match("you approach the Great Cave.")) {
			le.setLocation("Great Cave");
		} else if (match("You locate the mouth of the Large Cave easily.")) {
			le.setLocation("Large Cave");
		} else if (match("You arrive at the entrance to the Small Cave.")) {
			le.setLocation("Small Cave");
		} else if (match("You climb over a fallen tree that has blocked the Gate.")) {
			le.setLocation("Large Quarry");
		} else if (match("You see a large bird fly through the metalwork as you find the Gate.")) {
			le.setLocation("Large Quarry");
		} else if (match("You find the gate.")) {
			le.setLocation("Large Quarry");
		} else if (match("You stumble through a large patch of Brickbrake trees to the Gate.")) {
			le.setLocation("Large Quarry");
		} else if (match("You kick over a couple of rocks in front of the Gate.")) {
			le.setLocation("Large Quarry");
		} else if (match("You find a small dirt path that leads the way to the Gate.")) {
			le.setLocation("Large Quarry");
		} else if (match("You stumble through a patch of itchy urtipods and find the Gate.")) {
			le.setLocation("Large Quarry");
		} else if (match("You see a large bird fly through the metalwork as you find the Gate.")) {
			le.setLocation("Large Quarry");
		} else if (match("You get a running start and hit the gate at full force.")) {
			le.setLocation("Large Quarry");
		} else if (match("find yourself face to face wih the Collapsed Vault.")) {
			le.setLocation("Collapsed Vault");
		} else if (match("The ground before you slopes gently down into a hollow.")) {
			le.setLocation("Sunken Graveyard");
		} else if (match("You cautiously approach the large stone door.")) {
			le.setLocation("Stone Crypt");
		} else if (match("opening to the Underwater Grotto.")) {
			le.setLocation("Underwater Grotto");
		} else if (match("entrance to the Underwater Grotto.")) {
			le.setLocation("Underwater Grotto");
		} else if (match("You approach the hollow,")) {
			le.setLocation("Tainted Hollow");
		} else if (match("Geyser before you erupts.")) {
			le.setLocation("Dragon Geyser");
		} else if (match("Bat Cave through some branches.")) {
			le.setLocation("Bat Cave");
		} else if (match("to the Bat Cave entrance.")) {
			le.setLocation("Bat Cave");
		} else if (match("the Bat Cave.")) {
			le.setLocation("Bat Cave");
		} else if (match("the Bat Cave opening.")) {
			le.setLocation("Bat Cave");
		} else if (match("Three lovely glitterbirds flit near the entrance.")) {
			le.setLocation("Bat Cave");
		} else if (match("A grey bat flying through the jungle")) {
			le.setLocation("Bat Cave");
		} else if (match("Fungus Forest")) {
			le.setLocation("Fungus Forest");
		} else if (match("from a long vine.")) {
			le.setLocation("Bodden Camp");
			mode.add(0, MODE_TORCH);
		} else {
			return;
		}
		le.setParent(enc);
		enc = le;
	}

	private void checkMonsterEquip() {
		opponent = new Stats(new Turn());
		boolean more = true;
		StringBuffer sb = new StringBuffer();
		while (more) {
			if (match("a") || match ("an") || match("and") || match("with")) {
				// Ignore
			} else if (match ("suit of")) {
				pos++;
			} else {
				sb.append(words[pos]);
				sb.append(" ");
				if (words[pos].endsWith(",") || (words[pos].endsWith(".") && !match("Dam."))) {
					String equip = sb.toString().substring(0, sb.toString().length() - 2);
					updateEquipment(equip, opponent);
					sb = new StringBuffer();
				}
				if ((words[pos].endsWith(".") || words[pos].endsWith("!")) && !match("Dam.")) {
					more = false;
				}
			}
			pos++;
		}
		mode.remove(0);
	}
	
	private void checkCreature() {
		if (match("(named")) {
			pos++;
			((BattleEncounter) enc).setFoe(getFoe(words[pos]), words[pos]);
		} else if (match("vs.")) {
			pos++;
			if (match("a") || match("an")) {
				pos++;
			}
			StringBuffer sb = new StringBuffer();
			while (!match("**")) {
				sb.append(words[pos++]);
				sb.append(" ");
			}
			String creature = sb.toString().trim();

			if (creature.toUpperCase().equals(creature) && creature.contains("-")) {
				if (!(enc.getParent() instanceof LocationEncounter)) {
					LocationEncounter le = new LocationEncounter();
					Encounter parent = enc.getParent(); 
					parent.getEncounters().remove(enc);
					le.setLocation("Loggerhead Camp");
					le.setParent(parent);
					enc.setParent(le);
				}
			}
			((BattleEncounter) enc).setFoe(getFoe(creature), creature);
			
			if (mode.get(0) == MODE_WRESTLE) {
				mode.add(0, MODE_GRAPPLE);
			} else {
				mode.add(0, MODE_DEF_SPELL);
			}
		}
	}

	private Foe getFoe(String foeName) {
		if (enc.getParent().getEncType().equals("Location")) {
			LocationEncounter le = (LocationEncounter)enc.getParent();
			if (le == null) {
				System.out.println("A Location Encounter without a Location");
			} else if (le.getLocation().getName().equals("Loggerhead Camp")) {
				return DataStore.getInstance().getRace("Loggerhead");
			} else if (le.getLocation().getName().equals("Hillock")) {
				return DataStore.getInstance().getRace("Knolltir");
			} else if (le.getLocation().getName().equals("Mine Shaft")) {
				return DataStore.getInstance().getRace("Rock Troll");
			} else if (le.getLocation().getName().equals("Bodden Camp") && foeName.toUpperCase().equals(foeName)) {
				return DataStore.getInstance().getRace("Bodden");
			} else if (le.getLocation().getName().equals("Bodden Camp") && foeName.contains("'")) {
				return DataStore.getInstance().getRace("High Bodden");
			}
		}
		return DataStore.getInstance().getCreature(foeName);
	}
		
	private void checkTreasure() {
		if (match("They explain how to make *") || match("Pulling it out you find...*")) {
			boolean skip = false;
			pos += 5;
			for (int i = pos; i < pos + 20; i++) {
				if (words[i].equals("Treasure") && words[i+1].equals("-")) {
					skip = true;
					break;
				}
			}
			if (!skip) {
				boolean more = true;
				StringBuffer sb = new StringBuffer();
				while (more) {
					sb.append(words[pos]);
					sb.append(" ");
					if (words[pos].endsWith(".")) {
						more = false;
					}
					pos++;
				}
				String item = sb.toString();
				item = item.substring(0, sb.toString().length() - 2);
				TreasureEncounter te = new TreasureEncounter();
				te.setParent(enc);
				te.setQuantity(-1);
				te.setItem(DataStore.getInstance().getItem(item));
			}
		} else if (match("reach in and find *") && !match("reach in and find a Treasure")) {
			pos += 4;
			int quantity = -getNumber(words[pos]);
			pos++;
			boolean more = true;
			StringBuffer sb = new StringBuffer();
			while (more) {
				sb.append(words[pos]);
				if (!words[pos].equals(".")) {
					sb.append(" ");
				}
				if (words[pos].endsWith(".")) {
					more = false;
				}
				pos++;
			}
			String item = sb.toString();
			item = item.substring(0, sb.toString().length() - 2);
			TreasureEncounter te = new TreasureEncounter();
			te.setParent(enc);
			te.setQuantity(quantity);
			te.setItem(DataStore.getInstance().getItem(item));
		} else if (match("*Treasure - ")) {
			pos += 2;
			boolean fullStop = false;
			while (!fullStop) {
				TreasureEncounter te = new TreasureEncounter();
				te.setParent(enc);
				te.setQuantity(getNumber(words[pos], 1));
				StringBuffer sb = new StringBuffer();
				if (getNumber(words[pos]) != Integer.MIN_VALUE) {
					pos++;
				}
				while (pos < words.length) {
					if (match("(see")) {
						if (words[pos+1].endsWith(".")) {
							fullStop = true;
						}
						pos += 2;
						if (words[pos].equals("and")) {
							pos++;
						}
						break;
					}
					if (words[pos].endsWith(".")) {
						fullStop = true;
					}
					if ((words[pos].endsWith(".") || words[pos].endsWith(",")) && !words[pos].equals("Dam.")) {
						sb.append(words[pos].substring(0, words[pos].length() - 1));
						pos++;
						break;
					} else if (words[pos].equals("and")) {
						pos++;
						break;
					} else {
						sb.append(words[pos]);
						sb.append(" ");
					}
					pos++;
				}
				te.setItem(DataStore.getInstance().getItem(sb.toString().trim()));
			}
		}
	}

	private void checkScrye() {
		String type = "None";
		StringBuffer thing = new StringBuffer();
		int x = 0;
		int y = 0;
		if (match("You look deeply into your Crystal Ball.")) {
			//System.out.println("Crystal Ball: (" + turn.getY() + "," + turn.getX() + ")");
			pos += 7;
			while(pos < words.length) {
				if (match("* Action Pts used,")) {
					break;
				}
				
				if (!type.equals("None")) {
					if (match("You see the following") || match("You detect the following")) {
						type = "None";
					} else if (match("* East, * South*")) {
						x = turn.getX() + getNumber(words[pos]);
						y = turn.getY() - getNumber(words[pos + 2]);
						pos += 3;
					} else if (match("* East, * North*")) {
						x = turn.getX() + getNumber(words[pos]);
						y = turn.getY() + getNumber(words[pos + 2]);
						pos += 3;
					} else if (match("* West, * South*")) {
						x = turn.getX() - getNumber(words[pos]);
						y = turn.getY() - getNumber(words[pos + 2]);
						pos += 3;
					} else if (match("* West, * North*")) {
						x = turn.getX() - getNumber(words[pos]);
						y = turn.getY() + getNumber(words[pos + 2]);
						pos += 3;
					} else if (match ("*Your square*")) {
						x = turn.getX();
						y = turn.getY();
						pos += 1;
					} else if (match ("* South*")) {
						x = turn.getX();
						y = turn.getY() - getNumber(words[pos]);
						pos += 1;
					} else if (match ("* North*")) {
						x = turn.getX();
						y = turn.getY() + getNumber(words[pos]);
						pos += 1;
					} else if (match ("* East*")) {
						x = turn.getX() + getNumber(words[pos]);
						y = turn.getY();
						pos += 1;
					} else if (match ("* West*")) {
						x = turn.getX() - getNumber(words[pos]);
						y = turn.getY();
						pos += 1;
					} else if (match("and")) {
						thing.append(" ");
					} else if (match("a") || match("an")) {
						// Ignore this
					} else {
						thing.append(words[pos]);
						thing.append(" ");
					}
					if (words[pos].endsWith(",") || words[pos].endsWith(".") || match ("and")) {
						String name = thing.toString();
						if (name.startsWith("Temple of Fuvah")) {
							Sighting s = new Sighting(x, y, type, "Rebuilt Temple. ");
							turn.getSightings().add(s);
							s = new Sighting(x, y, type, "Fuvah. ");
							turn.getSightings().add(s);
						} else if (name.startsWith("Kabuki Temple")) {
							Sighting s = new Sighting(x, y, type, "Rebuilt Temple. ");
							turn.getSightings().add(s);
							s = new Sighting(x, y, type, "Kabuki. ");
							turn.getSightings().add(s);
						} else if (name.startsWith("Shroud Temple")) {
							Sighting s = new Sighting(x, y, type, "Rebuilt Temple. ");
							turn.getSightings().add(s);
							s = new Sighting(x, y, type, "Shroud. ");
							turn.getSightings().add(s);
						} else if (name.trim().length() > 0 && !name.startsWith("None")) {
							Sighting s;
							if (name.startsWith("Kongo-Mongo tree") || name.startsWith("Hairy Coco Palm Grove")) {
								s = new Sighting(x, y, "Location", thing.toString());
							} else {
								s = new Sighting(x, y, type, thing.toString());
							}
							turn.getSightings().add(s);
						}
						thing = new StringBuffer();
					}
				}
				if (match("Creatures:")) {
					type = "Creature";
				} else if (match("Plants:")) {
					type = "Plant";
				} else if (match("Hazards:") || match("Structures:") || match("Places:")) {
					type = "Location";
				}
				
				pos++;
			}
		}
	}
	
	private void checkMap() {
		if (match("=== END OF MAP DATA ===")) {
			mode.clear();
			mode.add(MODE_ORDER);
			return;
		}
		if (match("(*")) {
			String coords = words[pos++];
			String feature = words[pos++];
			String[] xy = coords.substring(1, coords.length()-1).split(",");
			Sighting s = new Sighting(Integer.valueOf(xy[1]), Integer.valueOf(xy[0]), feature);
			turn.getSightings().add(s);
		}
	}
	
	private void checkSightings() {
		int x;
		int y;
		StringBuffer thing = new StringBuffer();
		if (match("Spyglass sightings:")) {
			pos++;
			return;
		}
		if (match("Last visited:")) {
			mode.set(0, MODE_STATS);
			return;
		}
		if (match("Square(*")) {
			String[] coords = words[pos].split(",");
			y = getNumber(coords[0]);
			x = getNumber(coords[1]);
			pos++;
			while (!match("Square*") && !match("Last visited:")) {
				if (!words[pos].equals("and") && !words[pos].equals("a") && !words[pos].equals("an")) {
					thing.append(words[pos]);
					thing.append(" ");
				}
				if (words[pos].endsWith(",") || words[pos].endsWith(".")) {
					if (!thing.toString().startsWith("None")) {
						String name = thing.toString();
						String type = "Unknown";
						String what = thing.toString();
						if (what.contains("#")) {
							type = "Creature";
							name = name.substring(0, name.indexOf("(") + 2);
						}
						if (what.contains("Totem")) {
							while (!words[pos].endsWith(".") && !words[pos].endsWith(",")) {
								pos++;
							}
						}
						if (!what.contains("Bodden") && !what.contains("Totem") && !what.contains("Also") && !what.contains("tracks") && !what.contains(" female ") && !what.contains(" male ") && !what.contains("unusual")) {
							Sighting s = new Sighting(x, y, thing.toString().startsWith("Kongo-Mongo tree") ? "Location" : type, name);
							turn.getSightings().add(s);
						}
					}
					thing = new StringBuffer();
				}
				pos++;
			}
			pos--;
		}
	}
	
	private void checkStats() {
		if (match("Mount:")) {
			checkMount();
		}
		if (match("Riding Skill:")) {
			turn.getStats().getStats().put("Riding Skill", getNumber(words[pos+2]));
		}
		Iterator<String> it = null;
		it = Stats.EQUIP.iterator();
		while (it.hasNext()) {
			String equip = it.next();
			if (match(equip)) {
				StringBuffer sb = new StringBuffer();
				boolean jewelry = false;
				boolean spell = false;
				pos += equip.split("\\s").length;
				while (!words[pos].endsWith(",") && !jewelry && ! spell) {
					if (sb.length() > 0 && "Ring|Charm|Dead|Amulet|Straps|Collar|Brooch|Strength|Protection|Necklace|Poison".contains(words[pos])) { 
						jewelry = true;
					} else if (words[pos].startsWith("(") && words[pos].endsWith(")")) {
						spell = true;
					} else {
						sb.append(words[pos]);
						sb.append(" ");
						pos++;
					}
				}
				if (!spell) {
					sb.append(words[pos]);
				} else {
					sb = new StringBuffer();
					sb.append(words[pos].substring(1, words[pos].length() -1));
				}
				turn.getStats().getEquip().put(equip, sb.toString().trim());
				return;
			}
		}

		it = Stats.STATS.iterator();
		while (it.hasNext()) {
			String stat = it.next();
			if (match(stat)) {
				int value = getNumber(words[pos - 1]);
				if (value > 0) {
					turn.getStats().getStats().put(stat, value);
				}
				pos += stat.split("\\s").length - 1;
				return;
			}
		}

		it = Stats.SKILL.iterator();
		while (it.hasNext()) {
			String skill = it.next();
			if (match(skill)) {
				int value = getNumber(words[pos + 2]);
				turn.getStats().getStats().put(skill, value);
				pos += skill.split("\\s").length - 1;
				return;
			}
		}
		
		if (match("You've bestowed * Talisman*")) {
			turn.getStats().getStats().put("Talismans", getNumber(words[pos + 2]));
		}

		if (match("You've bestowed * Treasure*")) {
			turn.getStats().getStats().put("Treasures", getNumber(words[pos + 2]));
		}
		
		if (match ("Account Transactions:")) {
			mode.remove(0);
		}
		
	}

	private void checkMount() {
		turn.getStats().getEquip().put("Mount", words[pos-1]);
		turn.getStats().getStats().put("MountHealth", getNumber(words[pos+1]));
		turn.getStats().getStats().put("MountTough", getNumber(words[pos+3]));
		pos += 5;
	}

	private boolean match(String phrase) {
		if (phrase.contains(" ")) {
			String[] bits = phrase.split("\\s");
			// Not enough words to match the phrase against
			if (bits.length + pos > words.length) {
				return false;
			}
			for (int i = 0; i < bits.length; i++) {
				if (bits[i].equals("*")) {
					// continue
				} else if (bits[i].startsWith("*") && bits[i].endsWith("*")) {
					if (!words[pos + i].contains(bits[i].replaceAll("\\*", ""))) {
						return false;
					}
				} else if (bits[i].startsWith("*")) {
					if (!words[pos + i].endsWith(bits[i].replaceAll("\\*", ""))) {
						return false;
					}
				} else if (bits[i].endsWith("*")) {
					if (!words[pos + i].startsWith(bits[i]
							.replaceAll("\\*", ""))) {
						return false;
					}
				} else if (!bits[i].equals(words[pos + i])) {
					return false;
				}
			}
			return true;
		} else {
			if (phrase.equals("**")) {
				return words[pos].equals(phrase);
			} else if (phrase.startsWith("*") && phrase.endsWith("*")) {
				return words[pos].contains(phrase.replaceAll("\\*", ""));
			} else if (phrase.startsWith("*")) {
				return words[pos].endsWith(phrase.replaceAll("\\*", ""));
			} else if (phrase.endsWith("*")) {
				return words[pos].startsWith(phrase.replaceAll("\\*", ""));
			} else {
				return words[pos].equals(phrase);
			}
		}
	}

	public int getNumber(String word, int defaultNumber) {
		int number = getNumber(word); 
		if (number == Integer.MIN_VALUE) {
			return defaultNumber;
		} else {
			return number;
		}
	}
	
	public int getNumber(String word) {
		String digits = word.replaceAll("[^0-9\\-]", "");
		if (digits.length() > 0) {
			return Integer.parseInt(digits);
		} else {
			String num = word.replaceAll("[^A-Za-z]", "");
			if (num.equals("a") || num.equals("an") || num.equals("the")) {
				return 1;
			} else if (num.equals("some")) {
				return 3;
			} else {
				if (numbers.contains(num)) {
					return numbers.indexOf(num);
				} else {
					return Integer.MIN_VALUE;
				}
			}
		}
	}

}
