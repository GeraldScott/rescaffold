package io.archton.scaffold.util;

import java.time.LocalDate;

import static java.lang.Integer.parseInt;

/**
 * South Africa ID Number Format : {YYMMDD}{G}{SSS}{C}{A}{Z}
 * YYMMDD: Date of birth.
 * G: Gender, where 0-4 is Female and 5-9 is Male.
 * SSS: Sequence No. for DOB/G combination.
 * C: Citizenship, where 0 is RSA, and 1 Other.
 * A: Usually 8 or 9, but can be other values
 * Z: Control digit
 *
 * Note that the date of birth calculation assumes that members or students whose year of birth in the ID number is less than 16 was born in this century.
 *
 **/
public class RSA_IDNumber {
    public static Boolean isValidIdNumber(String idNumber) {
        if (idNumber == null) {
            return false;
        } else {
            // Validate check digit
            if (idNumber.length() == 13) {
                char[] idchars = idNumber.toCharArray();
                int sum = 0;
                for (int i = 1; i <= idchars.length; i++) {
                    int digit = Character.getNumericValue(idchars[idchars.length - i]);
                    if ((i % 2) != 0) {
                        sum += digit;
                    } else {
                        sum += digit < 5 ? digit * 2 : digit * 2 - 9;
                    }
                }
                if ((sum % 10) == 0) {
                    // Validate date of birth
                    try {
                        LocalDate dateOfBirth = LocalDate.of(
                                parseInt(idNumber.substring(0, 2)) < 16 ?
                                        parseInt("20" + idNumber.substring(0, 2)) :
                                        parseInt("19" + idNumber.substring(0, 2)),
                                parseInt(idNumber.substring(2, 4)),
                                parseInt(idNumber.substring(4, 6)));
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }
    public static LocalDate getDateOfBirth(String idNumber) {
        if (RSA_IDNumber.isValidIdNumber(idNumber)) {
            return LocalDate.of(
                    parseInt(idNumber.substring(0, 2)) < 16 ?
                            parseInt("20" + idNumber.substring(0, 2)) :
                            parseInt("19" + idNumber.substring(0, 2)),
                    parseInt(idNumber.substring(2, 4)),
                    parseInt(idNumber.substring(4, 6)));
        } else {
            return null;
        }
    }

    public static boolean isFemale(String idNumber) {
        if (RSA_IDNumber.isValidIdNumber(idNumber)) {
            return parseInt(idNumber.substring(6, 7)) < 5;
        } else {
            return false;
        }
    }

    public static boolean isMale(String idNumber) {
        if (RSA_IDNumber.isValidIdNumber(idNumber)) {
            return parseInt(idNumber.substring(6, 7)) >= 5;
        } else {
            return false;
        }
    }

    public static boolean isCitizen(String idNumber) {
        if (RSA_IDNumber.isValidIdNumber(idNumber)) {
            return idNumber.charAt(10) == '0';
        } else {
            return false;
        }
    }
}