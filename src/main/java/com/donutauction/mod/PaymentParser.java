package com.donutauction.mod;

import java.util.regex.*;

public class PaymentParser {
    public static class Result {
        public final String name; public final double amount;
        public Result(String name, double amount) { this.name = name; this.amount = amount; }
    }
    public static Result tryParse(String plainMessage, String regex) {
        try {
            Matcher m = Pattern.compile(regex).matcher(plainMessage);
            if (!m.find()) return null;
            String name = m.group("name"), raw = m.group("amount");
            Double amount = parseAmount(raw);
            return amount == null ? null : new Result(name, amount);
        } catch (PatternSyntaxException | IllegalArgumentException e) { return null; }
    }
    private static Double parseAmount(String raw) {
        String s = raw.trim().replace(",", "");
        if (s.isEmpty()) return null;
        double multiplier = 1.0;
        char last = Character.toLowerCase(s.charAt(s.length()-1));
        if (last=='k') { multiplier=1_000; s=s.substring(0,s.length()-1); }
        else if (last=='m') { multiplier=1_000_000; s=s.substring(0,s.length()-1); }
        else if (last=='b') { multiplier=1_000_000_000; s=s.substring(0,s.length()-1); }
        try { return Double.parseDouble(s)*multiplier; } catch(NumberFormatException e){ return null; }
    }
}