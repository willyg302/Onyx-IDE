package unrealeditor;

import java.lang.reflect.Method;

public class GUIProperties {
    public static final String PLAF_ACRYL = "com.jtattoo.plaf.acryl.AcrylLookAndFeel";
    public static final String PLAF_AERO = "com.jtattoo.plaf.aero.AeroLookAndFeel";
    public static final String PLAF_BERNSTEIN = "com.jtattoo.plaf.bernstein.BernsteinLookAndFeel";
    public static final String PLAF_GRAPHITE = "com.jtattoo.plaf.graphite.GraphiteLookAndFeel";
    public static final String PLAF_LUNA = "com.jtattoo.plaf.luna.LunaLookAndFeel";
    public static final String PLAF_MCWIN = "com.jtattoo.plaf.mcwin.McWinLookAndFeel";
    public static final String PLAF_MINT = "com.jtattoo.plaf.mint.MintLookAndFeel";
    public static final String PLAF_SMART = "com.jtattoo.plaf.smart.SmartLookAndFeel";
    
    public static final String defaults[][] = {
        {"Onyx (Default)", PLAF_SMART,     "Gray"},
        {"Amazon",         PLAF_GRAPHITE,  "Green"},
        {"Apple Classic",  PLAF_MCWIN,     "Default"},
        {"Apple Modern",   PLAF_MCWIN,     "Modern"},
        {"Apple Pink",     PLAF_MCWIN,     "Pink"},
        {"Blue Lake",      PLAF_AERO,      "Default"},
        {"Desert",         PLAF_AERO,      "Gold"},
        {"Emerald",        PLAF_AERO,      "Green"},
        {"Mint",           PLAF_MINT,      "Default"},
        {"Onyx Gold",      PLAF_SMART,     "Gold"},
        {"Onyx Lemon",     PLAF_SMART,     "Lemmon"},
        {"Onyx Sand",      PLAF_SMART,     "Brown"},
        {"Onyx Sky",       PLAF_SMART,     "Default"},
        {"Sunshine",       PLAF_BERNSTEIN, "Default"},
        {"Tropical",       PLAF_ACRYL,     "Lemmon"},
        {"Windows",        PLAF_LUNA,      "Default"}
    };
    
    // The row in defaults[][] that the current theme belongs to
    private int onyxTheme = 0;

    public GUIProperties() {}
    
    public String getFriendlyName() {return defaults[onyxTheme][0];}
    public String getLookAndFeel() {return defaults[onyxTheme][1];}
    public String getTheme() {return defaults[onyxTheme][2];}
    
    public int getOnyxTheme() {return onyxTheme;}
    public void setOnyxTheme(int ot) {onyxTheme = ot;}
    public boolean isOnyxTheme(int ot) {return onyxTheme == ot;}
    
    public void initializeTheme() {
        try {
            Class<?> c = Class.forName(getLookAndFeel());
            Method method = c.getDeclaredMethod ("setTheme", String.class);
            method.invoke(c, getTheme());
        } catch(Exception e) {
            System.err.println("[GUIProperties] Error: failed to initialize theme");
        }
    }
}