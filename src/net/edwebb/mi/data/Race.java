package net.edwebb.mi.data;

public class Race implements Comparable<Race>, Foe {

	private String code;
	private String name;
	
	public Race(String code, String name) {
		this.name = name;
		this.code = code;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public short getId() {
		return 0;
	}

	@Override
	public int compareTo(Race o) {
		if (o == null) {
			return 1;
		} else {
			return getName().compareTo(o.getName());
		}
	}
}
