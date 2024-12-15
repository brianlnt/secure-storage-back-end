package project.brianle.securestorage.enumeration.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import project.brianle.securestorage.enumeration.Authority;

import java.util.stream.Stream;

/*
The RoleConverter is a custom JPA AttributeConverter that converts an enum value (Authority) to a String when storing it in the database,
and converts the String back to an enum (Authority) when reading from the database.
This is useful for persisting enums in a more human-readable format in the database rather than their ordinal position (like 0, 1, 2).
 */
@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<Authority, String> {

    /*
    Input: Authority (like Authority.USER).
    Output: String (like USER_AUTHORITIES).
    This method tells JPA to store the string value in the database instead of the enum name.
     */
    @Override
    public String convertToDatabaseColumn(Authority authority) {
        if(authority == null) return null;
        return authority.getValue();
    }

    /*
    Input: String (like USER_AUTHORITIES).
    Output: Authority (like Authority.USER).
    It reads the value from the database, matches it with the correct Authority enum, and converts it back to an enum.
     */
    @Override
    public Authority convertToEntityAttribute(String role) {
        if(role == null) return null;
        return Stream.of(Authority.values())
                .filter(authority -> authority.getValue().equals(role))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
