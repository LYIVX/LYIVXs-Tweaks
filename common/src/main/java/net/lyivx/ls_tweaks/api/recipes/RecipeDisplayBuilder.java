package net.lyivx.ls_tweaks.api.recipes;

import java.util.ArrayList;
import java.util.List;

/**
 * Simplified builder for recipe displays
 */
public class RecipeDisplayBuilder {
    private String title = "";
    private final List<SlotWidget> slots = new ArrayList<>();
    private final List<TextureWidget> textures = new ArrayList<>();
    private final List<TextWidget> texts = new ArrayList<>();
    private final List<HoverTooltipWidget> hovers = new ArrayList<>();
    private int contentWidth = 150;
    private int contentHeight = 66;
    
    public RecipeDisplayBuilder title(String title) {
        this.title = title;
        return this;
    }
    
    public RecipeDisplayBuilder addSlot(SlotWidget slot) {
        this.slots.add(slot);
        return this;
    }
    
    public RecipeDisplayBuilder addTexture(TextureWidget texture) {
        this.textures.add(texture);
        return this;
    }
    
    public RecipeDisplayBuilder addText(TextWidget text) {
        this.texts.add(text);
        return this;
    }
    public RecipeDisplayBuilder addHover(HoverTooltipWidget hover) {
        this.hovers.add(hover);
        return this;
    }
    
    public RecipeDisplayBuilder contentSize(int width, int height) {
        this.contentWidth = width;
        this.contentHeight = height;
        return this;
    }
    
    public RecipeDisplay build() {
        return new SimpleRecipeDisplay(title, slots, textures, texts, hovers, contentWidth, contentHeight);
    }
    
    private static class SimpleRecipeDisplay implements RecipeDisplay {
        private final String title;
        private final List<SlotWidget> slots;
        private final List<TextureWidget> textures;
        private final List<TextWidget> texts;
        private final List<HoverTooltipWidget> hovers;
        
        private final int contentWidth;
        private final int contentHeight;
        
        public SimpleRecipeDisplay(String title, List<SlotWidget> slots, List<TextureWidget> textures, List<TextWidget> texts, List<HoverTooltipWidget> hovers, int contentWidth, int contentHeight) {
            this.title = title;
            this.slots = new ArrayList<>(slots);
            this.textures = new ArrayList<>(textures);
            this.texts = new ArrayList<>(texts);
            this.hovers = new ArrayList<>(hovers);
            this.contentWidth = contentWidth;
            this.contentHeight = contentHeight;
        }
        
        @Override
        public String titleText() {
            return title;
        }
        
        @Override
        public List<SlotWidget> slots() {
            return new ArrayList<>(slots);
        }
        
        @Override
        public List<TextureWidget> textures() {
            return new ArrayList<>(textures);
        }
        
        @Override
        public List<TextWidget> textWidgets() {
            return new ArrayList<>(texts);
        }
        @Override
        public List<HoverTooltipWidget> hoverWidgets() { return new ArrayList<>(hovers); }
        
        @Override
        public int contentWidth() {
            return contentWidth;
        }
        
        @Override
        public int contentHeight() {
            return contentHeight;
        }
    }
}
