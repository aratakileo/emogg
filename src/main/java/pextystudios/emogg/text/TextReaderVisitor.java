package pextystudios.emogg.text;

import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;

import java.util.ArrayList;
import java.util.List;

public class TextReaderVisitor implements CharacterVisitor {
  private final List<TextSegment> textSegments = new ArrayList<>();
  
  public boolean accept(int val, Style style, int currCharInt) {
    this.textSegments.add(new TextSegment(style, (char)currCharInt));
    return true;
  }
  
  public void replaceBetween(int beginIndex, int endIndex, String text, Style style) {
    deleteBetween(beginIndex, endIndex);
    insertAt(beginIndex, text, style);
  }
  
  public void deleteBetween(int beginIndex, int endIndex) {
    for (int i = endIndex - 1; i >= beginIndex; i--)
      this.textSegments.remove(i);
  }
  
  public void insertAt(int index, String text, Style style) {
    for (int i = 0; i < text.length(); i++)
      this.textSegments.add(index + i, new TextSegment(style, text.charAt(i)));
  }
  
  public OrderedText getOrderedText() {
    LiteralText literalText = new LiteralText("");
    for (TextSegment textSegment : this.textSegments)
      literalText.append((new LiteralText(Character.toString(textSegment.getChr()))).setStyle(textSegment.getStyle()));
    return literalText.asOrderedText();
  }
  
  public String getString() {
    StringBuilder sb = new StringBuilder();
    for (TextSegment textSegment : this.textSegments)
      sb.append(textSegment.getChr());
    return sb.toString();
  }
}