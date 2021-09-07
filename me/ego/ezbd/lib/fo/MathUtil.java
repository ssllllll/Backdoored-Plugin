package me.ego.ezbd.lib.fo;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.NavigableMap;
import java.util.TreeMap;

public final class MathUtil {
    private static final DecimalFormat oneDigitFormat = new DecimalFormat("#.#");
    private static final DecimalFormat twoDigitsFormat = new DecimalFormat("#.##");
    private static final DecimalFormat threeDigitsFormat = new DecimalFormat("#.###");
    private static final DecimalFormat fiveDigitsFormat = new DecimalFormat("#.#####");
    private static final NavigableMap<Integer, String> romanNumbers = new TreeMap();

    public static String toRoman(int number) {
        if (number == 0) {
            return "0";
        } else {
            int literal = (Integer)romanNumbers.floorKey(number);
            return number == literal ? (String)romanNumbers.get(number) : (String)romanNumbers.get(literal) + toRoman(number - literal);
        }
    }

    public static int max(int... numbers) {
        return Arrays.stream(numbers).max().getAsInt();
    }

    public static long floor(double d1) {
        long i = (long)d1;
        return d1 >= (double)i ? i : i - 1L;
    }

    public static long ceiling(double f1) {
        long i = (long)f1;
        return f1 >= (double)i ? i : i - 1L;
    }

    public static double range(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }

    public static int range(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    public static double atLeast(double value, double min) {
        return value > min ? value : min;
    }

    public static int atLeast(int value, int min) {
        return value > min ? value : min;
    }

    public static int increase(int number, double percent) {
        double myNumber = (double)number;
        double percentage = myNumber / 100.0D * percent;
        return (int)Math.round(myNumber + percentage);
    }

    public static double increase(double number, double percent) {
        double percentage = number / 100.0D * percent;
        return number + percentage;
    }

    public static int percent(double number, double maximum) {
        return (int)(number / maximum * 100.0D);
    }

    public static double average(Collection<Double> values) {
        return average((Double[])values.toArray(new Double[values.size()]));
    }

    public static double average(Double... values) {
        Valid.checkBoolean(values.length > 0, "No values given!", new Object[0]);
        double sum = 0.0D;
        Double[] var3 = values;
        int var4 = values.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            double val = var3[var5];
            sum += val;
        }

        return formatTwoDigitsD(sum / (double)values.length);
    }

    public static String formatOneDigit(double value) {
        return oneDigitFormat.format(value).replace(",", ".");
    }

    public static double formatOneDigitD(double value) {
        Valid.checkBoolean(!Double.isNaN(value), "Value must not be NaN", new Object[0]);
        return Double.parseDouble(oneDigitFormat.format(value).replace(",", "."));
    }

    public static String formatTwoDigits(double value) {
        return twoDigitsFormat.format(value).replace(",", ".");
    }

    public static double formatTwoDigitsD(double value) {
        Valid.checkBoolean(!Double.isNaN(value), "Value must not be NaN", new Object[0]);
        return Double.parseDouble(twoDigitsFormat.format(value).replace(",", "."));
    }

    public static String formatThreeDigits(double value) {
        return threeDigitsFormat.format(value).replace(",", ".");
    }

    public static double formatThreeDigitsD(double value) {
        Valid.checkBoolean(!Double.isNaN(value), "Value must not be NaN", new Object[0]);
        return Double.parseDouble(threeDigitsFormat.format(value).replace(",", "."));
    }

    public static String formatFiveDigits(double value) {
        return fiveDigitsFormat.format(value).replace(",", ".");
    }

    public static double formatFiveDigitsD(double value) {
        Valid.checkBoolean(!Double.isNaN(value), "Value must not be NaN", new Object[0]);
        return Double.parseDouble(fiveDigitsFormat.format(value).replace(",", "."));
    }

    public static double calculate(final String expression) {
        class 1Parser {
            int pos = -1;
            int c;

            _Parser/* $FF was: 1Parser*/() {
            }

            void eatChar() {
                this.c = ++this.pos < expression.length() ? expression.charAt(this.pos) : -1;
            }

            void eatSpace() {
                while(Character.isWhitespace(this.c)) {
                    this.eatChar();
                }

            }

            double parse() {
                this.eatChar();
                double v = this.parseExpression();
                if (this.c != -1) {
                    throw new MathUtil.CalculatorException("Unexpected: " + (char)this.c);
                } else {
                    return v;
                }
            }

            double parseExpression() {
                double v = this.parseTerm();

                while(true) {
                    while(true) {
                        this.eatSpace();
                        if (this.c == 43) {
                            this.eatChar();
                            v += this.parseTerm();
                        } else {
                            if (this.c != 45) {
                                return v;
                            }

                            this.eatChar();
                            v -= this.parseTerm();
                        }
                    }
                }
            }

            double parseTerm() {
                double v = this.parseFactor();

                while(true) {
                    while(true) {
                        this.eatSpace();
                        if (this.c == 47) {
                            this.eatChar();
                            v /= this.parseFactor();
                        } else {
                            if (this.c != 42 && this.c != 40) {
                                return v;
                            }

                            if (this.c == 42) {
                                this.eatChar();
                            }

                            v *= this.parseFactor();
                        }
                    }
                }
            }

            double parseFactor() {
                boolean negate = false;
                this.eatSpace();
                if (this.c == 43 || this.c == 45) {
                    negate = this.c == 45;
                    this.eatChar();
                    this.eatSpace();
                }

                double v;
                if (this.c == 40) {
                    this.eatChar();
                    v = this.parseExpression();
                    if (this.c == 41) {
                        this.eatChar();
                    }
                } else {
                    StringBuilder sb = new StringBuilder();

                    while(this.c >= 48 && this.c <= 57 || this.c == 46) {
                        sb.append((char)this.c);
                        this.eatChar();
                    }

                    if (sb.length() == 0) {
                        throw new MathUtil.CalculatorException("Unexpected: " + (char)this.c);
                    }

                    v = Double.parseDouble(sb.toString());
                }

                this.eatSpace();
                if (this.c == 94) {
                    this.eatChar();
                    v = Math.pow(v, this.parseFactor());
                }

                if (negate) {
                    v = -v;
                }

                return v;
            }
        }

        return (new 1Parser()).parse();
    }

    private MathUtil() {
    }

    static {
        romanNumbers.put(1000, "M");
        romanNumbers.put(900, "CM");
        romanNumbers.put(500, "D");
        romanNumbers.put(400, "CD");
        romanNumbers.put(100, "C");
        romanNumbers.put(90, "XC");
        romanNumbers.put(50, "L");
        romanNumbers.put(40, "XL");
        romanNumbers.put(10, "X");
        romanNumbers.put(9, "IX");
        romanNumbers.put(5, "V");
        romanNumbers.put(4, "IV");
        romanNumbers.put(1, "I");
    }

    public static final class CalculatorException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public CalculatorException(String message) {
            super(message);
        }
    }
}