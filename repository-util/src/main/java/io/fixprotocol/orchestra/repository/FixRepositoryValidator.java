package io.fixprotocol.orchestra.repository;

import io.fixprotocol.orchestra.event.EventListener;

/**
 * Validates that an Orchestra repository file conforms to the schema and applies FIX style rules
 * 
 * @author Don Mendelson
 *
 */
public class FixRepositoryValidator extends BasicRepositoryValidator {

  public FixRepositoryValidator(EventListener eventLogger) {
    super(eventLogger);

    isValidBoolean = t -> t.equals("Y") || t.equals("N");
    isValidChar = t -> t.length() == 1 && !Character.isWhitespace(t.charAt(0));
    isValidName = t -> t.length() > 0 && t.chars().noneMatch(Character::isWhitespace)
        && Character.isUpperCase(t.charAt(0));
    isValidString = t -> t.length() > 0 && t.chars().noneMatch(Character::isWhitespace);
  }

}
