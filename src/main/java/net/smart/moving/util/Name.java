package net.smart.moving.util;

/** Possible development, SRG and obfuscated names for optional integration points. */
public final class Name {
    public final String deobfuscated;
    public final String forgefuscated;
    public final String obfuscated;

    public Name(String deobfuscated) { this(deobfuscated, null, null); }
    public Name(String deobfuscated, String obfuscated) { this(deobfuscated, null, obfuscated); }
    public Name(String deobfuscated, String forgefuscated, String obfuscated) {
        this.deobfuscated = deobfuscated;
        this.forgefuscated = forgefuscated;
        this.obfuscated = obfuscated;
    }
}
