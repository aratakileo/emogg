package pextystudios.emogg.text;

import net.minecraft.text.Style;

public class TextSegment {
  private Style style;
  
  private char chr;
  
  public TextSegment(Style style, char chr) {
    this.style = style;
    this.chr = chr;
  }
  
  public Style getStyle() {
    return this.style;
  }
  
  public char getChr() {
    return this.chr;
  }
}
