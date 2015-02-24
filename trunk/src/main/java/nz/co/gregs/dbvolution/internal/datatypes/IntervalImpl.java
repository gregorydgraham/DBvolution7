/*
 * Copyright 2015 gregorygraham.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nz.co.gregs.dbvolution.internal.datatypes;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import org.joda.time.Period;

/**
 *
 * @author gregorygraham
 */
public class IntervalImpl {

	private static final String ZERO_INTERVAL_STRING = "P0Y0M0D0h0n0.0s";

	public IntervalImpl() {
	}

	public static String getZeroIntervalString() {
		return ZERO_INTERVAL_STRING;
	}

	@SuppressWarnings("deprecation")
	public static String subtract2Dates(Date original, Date compareTo) {
		if (original == null || compareTo == null) {
			return null;
		}
		int years = original.getYear() - compareTo.getYear();
		int months = original.getMonth() - compareTo.getMonth();
		int days = original.getDate() - compareTo.getDate();
		int hours = original.getHours() - compareTo.getHours();
		int minutes = original.getMinutes() - compareTo.getMinutes();
		int millis = (int) ((original.getTime() - ((original.getTime() / 1000) * 1000)) - (compareTo.getTime() - ((compareTo.getTime() / 1000) * 1000)));
		double seconds = original.getSeconds() - compareTo.getSeconds() + (millis / 1000.0);
		String intervalString = "P" + years + "Y" + months + "M" + days + "D" + hours + "h" + minutes + "n" + seconds + "s";
		return intervalString;
	}

	public static String getIntervalString(Period interval) {
		if (interval == null) {
			return null;
		}
		int years = interval.getYears();
		int months = interval.getMonths();
		int days = interval.getDays() + interval.getWeeks() * 7;
		int hours = interval.getHours();
		int minutes = interval.getMinutes();

		int millis = interval.getMillis();
		double seconds = interval.getSeconds() + (millis / 1000.0);
		String intervalString = "P" + years + "Y" + months + "M" + days + "D" + hours + "h" + minutes + "n" + seconds + "s";
		return intervalString;
	}

	public static boolean isEqualTo(String original, String compareTo) {
		return compareIntervalStrings(original, compareTo) == 0;
	}

	public static boolean isGreaterThan(String original, String compareTo) {
		return compareIntervalStrings(original, compareTo) == 1;
	}

	public static boolean isLessThan(String original, String compareTo) {
		return compareIntervalStrings(original, compareTo) == -1;
	}

	public static Integer compareIntervalStrings(String original, String compareTo) {
		if (original == null || compareTo == null) {
			return null;
		}
		String[] splitOriginal = original.split("[A-Za-z]");
		String[] splitCompareTo = compareTo.split("[A-Za-z]");
		for (int i = 1; i < splitCompareTo.length; i++) { // Start at 1 because the first split is empty
			System.out.println("SPLITORIGINAL " + i + ": " + splitOriginal[i]);
			double intOriginal = Double.parseDouble(splitOriginal[i]);
			double intCompareTo = Double.parseDouble(splitCompareTo[i]);
			if (intOriginal > intCompareTo) {
				return 1;
			}
			if (intOriginal < intCompareTo) {
				return -1;
			}
		}
		return 0;
	}

	public static Date addDateAndIntervalString(Date original, String intervalStr) {
		if (original == null || intervalStr == null || intervalStr.length() == 0 || original.toString().length() == 0) {
			return null;
		}
		Calendar cal = new GregorianCalendar();
		cal.setTime(original);
		int years = getYearPart(intervalStr);
		int months = getMonthPart(intervalStr);
		int days = getDayPart(intervalStr);
		int hours = getHourPart(intervalStr);
		int minutes = getMinutePart(intervalStr);
		int seconds = getSecondPart(intervalStr);

		int millis = getMillisecondPart(intervalStr);

		cal.add(Calendar.YEAR, years);
		cal.add(Calendar.MONTH, months);
		cal.add(Calendar.DAY_OF_MONTH, days);
		cal.add(Calendar.HOUR, hours);
		cal.add(Calendar.MINUTE, minutes);
		cal.add(Calendar.SECOND, seconds);
		cal.add(Calendar.MILLISECOND, millis);
		return cal.getTime();
	}

	public static Date subtractDateAndIntervalString(Date original, String intervalInput) {
		if (original == null || intervalInput == null || intervalInput.length() == 0) {
			return null;
		}
		String intervalStr = intervalInput.replaceAll("[^-.PYMDhns0-9]+", "");
		Calendar cal = new GregorianCalendar();
		cal.setTime(original);
		int years = getYearPart(intervalStr);
		int months = getMonthPart(intervalStr);
		int days = getDayPart(intervalStr);
		int hours = getHourPart(intervalStr);
		int minutes = getMinutePart(intervalStr);
		int seconds = getSecondPart(intervalStr);
		int millis = getMillisecondPart(intervalStr);

		cal.add(Calendar.YEAR, -1 * years);
		cal.add(Calendar.MONTH, -1 * months);
		cal.add(Calendar.DAY_OF_MONTH, -1 * days);
		cal.add(Calendar.HOUR, -1 * hours);
		cal.add(Calendar.MINUTE, -1 * minutes);
		cal.add(Calendar.SECOND, -1 * seconds);
		cal.add(Calendar.MILLISECOND, -1 * millis);
		return cal.getTime();
	}

	public static Period parseIntervalFromGetString(String intervalStr) {
		if (intervalStr == null || intervalStr.length() == 0) {
			return null;
		}
		System.out.println("DBV INTERVAL: " + intervalStr);
		Period interval = new Period();
		interval = interval.withYears(getYearPart(intervalStr));
		interval = interval.withMonths(getMonthPart(intervalStr));
		interval = interval.withDays(getDayPart(intervalStr));
		interval = interval.withHours(getHourPart(intervalStr));
		interval = interval.withMinutes(getMinutePart(intervalStr));
		interval = interval.withSeconds(getSecondPart(intervalStr));
		interval = interval.withMillis(getMillisecondPart(intervalStr));
		return interval;
	}

	public static Integer getMillisecondPart(String intervalStr) throws NumberFormatException {
		if (intervalStr == null || intervalStr.length() == 0) {
			return null;
		}
		final Double secondsDouble = Double.parseDouble(intervalStr.replaceAll(".*n([-0-9.]+)s.*", "$1"));
		final int secondsInt = secondsDouble.intValue();
		final int millis = new Double(secondsDouble * 1000.0 - secondsInt * 1000).intValue();
		return millis;
	}

	public static Integer getSecondPart(String intervalStr) throws NumberFormatException {
		if (intervalStr == null || intervalStr.length() == 0 || !intervalStr.matches(".*n([-0-9.]+)s.*")) {
			return null;
		}
		final Double valueOf = Double.valueOf(intervalStr.replaceAll(".*n([-0-9.]+)s.*", "$1"));
		if (valueOf == null) {
			return null;
		}
		return valueOf.intValue();
	}

	public static int getMinutePart(String intervalStr) throws NumberFormatException {
		if (intervalStr == null || intervalStr.length() == 0) {
			return 0;
		}
		return Integer.parseInt(intervalStr.replaceAll(".*h([-0-9.]+)n.*", "$1"));
	}

	public static int getHourPart(String intervalStr) throws NumberFormatException {
		if (intervalStr == null || intervalStr.length() == 0) {
			return 0;
		}
		return Integer.parseInt(intervalStr.replaceAll(".*D([-0-9.]+)h.*", "$1"));
	}

	public static int getDayPart(String intervalStr) throws NumberFormatException {
		if (intervalStr == null || intervalStr.length() == 0) {
			return 0;
		}
		return Integer.parseInt(intervalStr.replaceAll(".*M([-0-9.]+)D.*", "$1"));
	}

	public static int getMonthPart(String intervalStr) throws NumberFormatException {
		if (intervalStr == null || intervalStr.length() == 0) {
			return 0;
		}
		return Integer.parseInt(intervalStr.replaceAll(".*Y([-0-9.]+)M.*", "$1"));
	}

	public static int getYearPart(String intervalStr) throws NumberFormatException {
		if (intervalStr == null || intervalStr.length() == 0) {
			return 0;
		}
		return Integer.parseInt(intervalStr.replaceAll(".*P([-0-9.]+)Y.*", "$1"));
	}
}
