package com.duxman.labelviewer.converter;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts a supported subset of ZPL II and Godex EZPL label formats.
 * Label dimensions are converted between ZPL dots and EZPL millimeters;
 * field coordinates remain dots in both formats.
 */
@Component
public class ZplGodexConverter {

    private static final String NL = "\r\n";
    private static final int DEFAULT_DARKNESS = 10;
    private static final Pattern ZPL_COMMAND = Pattern.compile("\\^([A-Z0-9]{2})([^\\^]*)");
    private static final Pattern GODEx_RECT = Pattern.compile("(?i)^R(-?\\d+),(-?\\d+),(-?\\d+),(-?\\d+),(\\d+),(\\d+)$");
    private static final Pattern GODEx_LINE = Pattern.compile("(?i)^L([OE]),(-?\\d+),(-?\\d+),(-?\\d+),(-?\\d+)$");
    private static final Pattern GODEx_DIAGONAL = Pattern.compile("(?i)^Ls,(-?\\d+),(-?\\d+),(\\d+),(-?\\d+),(-?\\d+)$");

    private final int dpi;

    public ZplGodexConverter() {
        this(203);
    }

    private static String zplFieldData(String data) {
        if (data.indexOf('^') < 0 && data.indexOf('~') < 0 && data.indexOf('_') < 0) {
            return "^FD" + data;
        }
        return "^FH_^FD" + data.replace("_", "_5F").replace("^", "_5E").replace("~", "_7E");
    }

    /**
     * @param dpi printer resolution, typically 203 or 300 dots per inch
     */
    public ZplGodexConverter(int dpi) {
        if (dpi <= 0) {
            throw new IllegalArgumentException("dpi must be positive");
        }
        this.dpi = dpi;
    }

    private static class Label {
        int widthDots;
        int heightDots;
        int gapMm;
        int darkness = DEFAULT_DARKNESS;
        int copies = 1;
        final List<Element> elements = new ArrayList<>();
    }

    private interface Element { }

    private record Text(int x, int y, int rotation, int height, int width, String data) implements Element { }

    private record Barcode(int x, int y, int rotation, String type, int narrow, int wide, int height,
                          boolean human, String data) implements Element { }

    private record Box(int x, int y, int width, int height, int thickness) implements Element { }

    private record Line(int x, int y, int x2, int y2, int thickness) implements Element { }

    public String zplToGodex(String zpl) {
        return toGodex(parseZpl(zpl));
    }

    public String godexToZpl(String godex) {
        return toZpl(parseGodex(godex));
    }

    private Label parseZpl(String zpl) {
        if (zpl == null) {
            throw new IllegalArgumentException("ZPL is null");
        }

        Label label = new Label();
        int x = 0, y = 0, narrow = 2, wide = 6, barHeight = 10;
        int fontHeight = 30, fontWidth = 30, fontRotation = 0;
        String barType = null;
        int barRotation = 0;
        boolean human = true, barcodePending = false;

        Matcher matcher = ZPL_COMMAND.matcher(zpl);
        while (matcher.find()) {
            String command = matcher.group(1).toUpperCase();
            String params = matcher.group(2).strip();
            String[] args = params.split(",", -1);
            switch (command) {
                case "PW" -> label.widthDots = number(args, 0, label.widthDots);
                case "LL" -> label.heightDots = number(args, 0, label.heightDots);
                case "PQ" -> label.copies = Math.max(1, number(args, 0, 1));
                case "MD" -> label.darkness = clamp(DEFAULT_DARKNESS + number(args, 0, 0), 0, 19);
                case "FO", "FT" -> {
                    x = number(args, 0, 0);
                    y = number(args, 1, 0);
                }
                case "BY" -> {
                    narrow = number(args, 0, narrow);
                    int ratio = number(args, 1, 3);
                    wide = narrow * ratio;
                    barHeight = number(args, 2, barHeight);
                }
                case "CF" -> {
                    fontHeight = number(args, 1, fontHeight);
                    fontWidth = number(args, 2, fontHeight);
                }
                case "BC", "B3", "BE", "B8" -> {
                    barType = command;
                    barRotation = zplRotation(args.length > 0 ? args[0] : "N");
                    int heightIndex = command.equals("B3") ? 2 : 1;
                    barHeight = number(args, heightIndex, barHeight);
                    int humanIndex = command.equals("BC") ? 2 : command.equals("B3") ? 3 : 2;
                    human = args.length <= humanIndex || !args[humanIndex].equalsIgnoreCase("N");
                    barcodePending = true;
                }
                case "GB" -> label.elements.add(new Box(x, y, number(args, 0, 1), number(args, 1, 1),
                        number(args, 2, 1)));
                case "FD" -> {
                    if (barcodePending) {
                        label.elements.add(new Barcode(x, y, barRotation, barType, narrow, wide, barHeight, human,
                                params));
                        barcodePending = false;
                    } else {
                        label.elements.add(new Text(x, y, fontRotation, fontHeight, fontWidth, params));
                    }
                }
                default -> {
                    if (command.charAt(0) == 'A') {
                        String[] fontArgs = params.split(",", -1);
                        if (fontArgs.length > 0) {
                            String fontAndRotation = fontArgs[0];
                            fontRotation = zplRotation(fontAndRotation.length() > 1
                                    ? fontAndRotation.substring(1) : "N");
                        }
                        fontHeight = number(fontArgs, 1, fontHeight);
                        fontWidth = number(fontArgs, 2, fontHeight);
                    }
                }
            }
        }
        return label;
    }

    private String toGodex(Label label) {
        StringBuilder out = new StringBuilder();
        if (label.heightDots > 0) {
            out.append("^Q").append(dotsToMm(label.heightDots)).append(',').append(label.gapMm).append(NL);
        }
        if (label.widthDots > 0) {
            out.append("^W").append(dotsToMm(label.widthDots)).append(NL);
        }
        out.append("^H").append(label.darkness).append(NL);
        out.append("^L").append(NL);
        for (Element element : label.elements) {
            if (element instanceof Text text) {
                out.append("AT,").append(text.x).append(',').append(text.y).append(',')
                        .append(text.width).append(',').append(text.height).append(",0,")
                        .append(text.rotation).append(",0,0,").append(text.data);
            } else if (element instanceof Barcode barcode) {
                out.append('B').append(godexBarcodeType(barcode.type)).append(',')
                        .append(barcode.x).append(',').append(barcode.y).append(',')
                        .append(barcode.narrow).append(',').append(barcode.wide).append(',')
                        .append(barcode.height).append(',').append(barcode.rotation).append(',')
                        .append(barcode.human ? 1 : 0).append(',').append(barcode.data);
            } else if (element instanceof Box box) {
                out.append("R").append(box.x).append(',').append(box.y).append(',')
                        .append(box.x + box.width).append(',').append(box.y + box.height).append(',')
                        .append(box.thickness).append(',').append(box.thickness);
            } else if (element instanceof Line line) {
                if (line.x == line.x2 || line.y == line.y2) {
                    out.append("Lo,").append(line.x).append(',').append(line.y).append(',')
                            .append(line.x2).append(',').append(line.y2);
                } else {
                    out.append("Ls,").append(line.x).append(',').append(line.y).append(',')
                            .append(line.thickness).append(',').append(line.x2).append(',').append(line.y2);
                }
            }
            out.append(NL);
        }
        if (label.copies > 1) {
            out.append("^P").append(label.copies).append(NL);
        }
        return out.append('E').append(NL).toString();
    }

    private Label parseGodex(String godex) {
        if (godex == null) {
            throw new IllegalArgumentException("Godex EZPL is null");
        }
        Label label = new Label();
        for (String raw : godex.split("\\r?\\n")) {
            String line = raw.strip();
            if (line.isEmpty()) {
                continue;
            }
            String[] args;
            Matcher matcher;
            if (line.regionMatches(true, 0, "^Q", 0, 2)) {
                args = line.substring(2).split(",", -1);
                label.heightDots = mmToDots(number(args, 0, 0));
                label.gapMm = number(args, 1, 0);
            } else if (line.regionMatches(true, 0, "^W", 0, 2)) {
                label.widthDots = mmToDots(number(new String[]{line.substring(2)}, 0, 0));
            } else if (line.regionMatches(true, 0, "^H", 0, 2)) {
                label.darkness = clamp(number(new String[]{line.substring(2)}, 0, DEFAULT_DARKNESS), 0, 19);
            } else if (line.regionMatches(true, 0, "^P", 0, 2)) {
                label.copies = Math.max(1, number(new String[]{line.substring(2)}, 0, 1));
            } else if (line.regionMatches(true, 0, "AT,", 0, 3)) {
                args = line.substring(3).split(",", 9);
                if (args.length == 9) {
                    label.elements.add(new Text(number(args, 0, 0), number(args, 1, 0),
                            number(args, 5, 0), number(args, 3, 30), number(args, 2, 30), args[8]));
                }
            } else if (line.length() > 1 && line.charAt(0) == 'B' && Character.isLetterOrDigit(line.charAt(1))) {
                args = line.substring(1).split(",", 9);
                if (args.length == 9) {
                    label.elements.add(new Barcode(number(args, 1, 0), number(args, 2, 0),
                            number(args, 6, 0), args[0], number(args, 3, 1), number(args, 4, 2),
                            number(args, 5, 10), number(args, 7, 0) != 0, args[8]));
                }
            } else if ((matcher = GODEx_RECT.matcher(line)).matches()) {
                int x = number(matcher.group(1), 0), y = number(matcher.group(2), 0);
                label.elements.add(new Box(x, y, number(matcher.group(3), 0) - x,
                        number(matcher.group(4), 0) - y,
                        Math.max(number(matcher.group(5), 1), number(matcher.group(6), 1))));
            } else if ((matcher = GODEx_LINE.matcher(line)).matches()) {
                label.elements.add(new Line(number(matcher.group(2), 0), number(matcher.group(3), 0),
                        number(matcher.group(4), 0), number(matcher.group(5), 0), 1));
            } else if ((matcher = GODEx_DIAGONAL.matcher(line)).matches()) {
                int x = number(matcher.group(1), 0), y = number(matcher.group(2), 0);
                label.elements.add(new Line(x, y, number(matcher.group(4), 0),
                        number(matcher.group(5), 0), number(matcher.group(3), 1)));
            }
        }
        return label;
    }

    private String toZpl(Label label) {
        StringBuilder out = new StringBuilder("^XA").append(NL);
        if (label.widthDots > 0) {
            out.append("^PW").append(label.widthDots).append(NL);
        }
        if (label.heightDots > 0) {
            out.append("^LL").append(label.heightDots).append(NL);
        }
        out.append("^MD").append(label.darkness - DEFAULT_DARKNESS).append(NL);
        for (Element element : label.elements) {
            if (element instanceof Text text) {
                out.append("^FO").append(text.x).append(',').append(text.y)
                        .append("^A0").append(zplRotation(text.rotation)).append(',')
                        .append(text.height).append(',').append(text.width)
                        .append(zplFieldData(text.data)).append("^FS");
            } else if (element instanceof Barcode barcode) {
                out.append("^BY").append(barcode.narrow).append(',')
                        .append(Math.max(2, barcode.wide / Math.max(1, barcode.narrow))).append(',')
                        .append(barcode.height).append("^FO").append(barcode.x).append(',').append(barcode.y)
                        .append(zplBarcodeCommand(barcode)).append(zplFieldData(barcode.data)).append("^FS");
            } else if (element instanceof Box box) {
                out.append("^FO").append(box.x).append(',').append(box.y)
                        .append("^GB").append(box.width).append(',').append(box.height).append(',')
                        .append(box.thickness).append("^FS");
            } else if (element instanceof Line line) {
                int width = Math.max(1, Math.abs(line.x2 - line.x));
                int height = Math.max(1, Math.abs(line.y2 - line.y));
                out.append("^FO").append(line.x).append(',').append(line.y);
                if (line.x == line.x2 || line.y == line.y2) {
                    out.append("^GB").append(width).append(',').append(height).append(',')
                            .append(line.thickness).append("^FS");
                } else {
                    out.append("^GD").append(width).append(',').append(height).append(',')
                            .append(line.thickness).append(',')
                            .append((line.x2 - line.x) * (line.y2 - line.y) >= 0 ? "R" : "L")
                            .append(",B^FS");
                }
            }
            out.append(NL);
        }
        if (label.copies > 1) {
            out.append("^PQ").append(label.copies).append(NL);
        }
        return out.append("^XZ").append(NL).toString();
    }

    private String zplBarcodeCommand(Barcode barcode) {
        String rotation = zplRotation(barcode.rotation);
        String readable = barcode.human ? "Y" : "N";
        return switch (barcode.type.toUpperCase()) {
            case "A", "A2", "A3", "A4", "A5", "A6" -> "^B3" + rotation + ",N," + barcode.height + ","
                    + readable + ",N";
            case "E" -> "^BE" + rotation + "," + barcode.height + "," + readable + ",N";
            case "B" -> "^B8" + rotation + "," + barcode.height + "," + readable + ",N";
            case "Q", "Q2" -> "^BC" + rotation + "," + barcode.height + "," + readable + ",N,N";
            default -> throw new IllegalArgumentException("Unsupported Godex barcode type: " + barcode.type);
        };
    }

    private static String godexBarcodeType(String zplType) {
        return switch (zplType) {
            case "B3" -> "A";
            case "BE" -> "E";
            case "B8" -> "B";
            default -> "Q";
        };
    }

    private static int zplRotation(String orientation) {
        return switch (orientation.strip().toUpperCase()) {
            case "R" -> 1;
            case "I" -> 2;
            case "B" -> 3;
            default -> 0;
        };
    }

    private static String zplRotation(int rotation) {
        return switch (rotation) {
            case 1 -> "R";
            case 2 -> "I";
            case 3 -> "B";
            default -> "N";
        };
    }

    private int dotsToMm(int dots) {
        return (int) Math.round(dots * 25.4 / dpi);
    }

    private int mmToDots(int mm) {
        return (int) Math.round(mm * dpi / 25.4);
    }

    private static int number(String[] values, int index, int fallback) {
        return index < values.length ? number(values[index], fallback) : fallback;
    }

    private static int number(String value, int fallback) {
        try {
            return (int) Math.round(Double.parseDouble(value.strip()));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
