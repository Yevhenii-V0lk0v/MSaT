package multiagent.lab2;

public enum Percept {
	BREEZE("breeze"),
	BUMP("bump"),
	GLITTER("glitter"),
	NOTHING(""),
	SCREAM("scream"),
	STENCH("stench");

	Percept(String stringInterpretation) {
		this.stringInterpretation = stringInterpretation;
	}

	private final String stringInterpretation;

	public String getStringInterpretation() {
		return stringInterpretation;
	}

	public String getPunctuatedInterpretation() {
		return stringInterpretation + ",";
	}

	public static Percept getByInterpretation(String interpretation) {
		for (Percept value : values()) {
			if (value.stringInterpretation.equals(interpretation)) {
				return value;
			}
		}
		return NOTHING;
	}
}
