package aero.sita.messaging.mercury.e2e.utilities.format.typeb.section.utils;

public enum HeadingField {

  PRE_SOA_TYPE("preSOAType"),
  ADDRESS_END_INDICATOR("AddressEndIndicator"),
  PILOT_SIGNAL("PilotSignal"),
  PLAIN_HEADING_TEXT("PlainHeadingText");

  private final String key;

  HeadingField(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  public static HeadingField fromKey(String key) {
    for (HeadingField field : values()) {
      if (field.key.equalsIgnoreCase(key)) {
        return field;
      }
    }
    throw new IllegalArgumentException("Unknown HeadingField key: " + key);
  }
}
