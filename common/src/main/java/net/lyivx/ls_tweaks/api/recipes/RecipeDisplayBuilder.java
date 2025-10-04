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
    private final List<IconWidget> icons = new ArrayList<>();
    private final List<TextWidget> texts = new ArrayList<>();
    private final List<HoverTooltipWidget> hovers = new ArrayList<>();
    private int contentWidth = 150;
    private int contentHeight = 66;
    
    public RecipeDisplayBuilder title(String title) {
        this.title = title;
        return this;
    }
    
    public RecipeDisplayBuilder addSlot(SlotWidget slot) {
        // Defer position finalization until build(), after contentSize is known
        this.slots.add(slot);
        return this;
    }

    // Convenience overload to allow composite grid/scroll widgets that extend SlotWidget
    public RecipeDisplayBuilder addSlot(GridSlotWidget grid) {
        this.slots.add(grid);
        return this;
    }

    public RecipeDisplayBuilder addSlot(ScrollWidget scroll) {
        this.slots.add(scroll);
        return this;
    }
    
    public RecipeDisplayBuilder addTexture(TextureWidget texture) {
        this.textures.add(texture);
        return this;
    }
    public RecipeDisplayBuilder addIcon(IconWidget icon) {
        this.icons.add(icon);
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
        // Resolve any positioned SlotBuilders and other widget builders now that content size is finalized
        java.util.List<SlotWidget> resolved = new java.util.ArrayList<>(this.slots.size());
        for (SlotWidget s : this.slots) {
            if (s instanceof Widgets.AddSlot.SlotBuilder sb && sb.hasPositioning()) {
                resolved.add(sb.finalizePosition(this.contentWidth, this.contentHeight));
                continue;
            }
            // Texture widgets are not SlotWidget; we pass them through by capturing into textures list already.
            // For positioned texture builders, finalize inside textures list, not slots.
            resolved.add(s);
        }
        // Finalize textures positioning
        java.util.List<TextureWidget> resolvedTextures = new java.util.ArrayList<>(this.textures.size());
        for (TextureWidget t : this.textures) {
            if (t instanceof Widgets.AddTexture.TextureBuilder tb && tb.hasPositioning()) {
                resolvedTextures.add(tb.finalizePosition(this.contentWidth, this.contentHeight));
            } else {
                resolvedTextures.add(t);
            }
        }
        // Finalize text positioning
        java.util.List<TextWidget> resolvedTexts = new java.util.ArrayList<>(this.texts.size());
        for (TextWidget t : this.texts) {
            if (t instanceof Widgets.AddText.TextBuilder tb && tb.hasPositioning()) {
                resolvedTexts.add(tb.finalizePosition(this.contentWidth, this.contentHeight));
            } else {
                resolvedTexts.add(t);
            }
        }
        // Finalize icon positioning
        java.util.List<IconWidget> resolvedIcons = new java.util.ArrayList<>(this.icons.size());
        for (IconWidget i : this.icons) {
            if (i instanceof Widgets.AddIcon.IconBuilder ib && ib.hasPositioning()) {
                resolvedIcons.add(ib.finalizePosition(this.contentWidth, this.contentHeight));
            } else {
                resolvedIcons.add(i);
            }
        }
        // Finalize hover positioning
        java.util.List<HoverTooltipWidget> resolvedHovers = new java.util.ArrayList<>(this.hovers.size());
        for (HoverTooltipWidget h : this.hovers) {
            if (h instanceof Widgets.AddHover.HoverBuilder hb && hb.hasPositioning()) {
                resolvedHovers.add(hb.finalizePosition(this.contentWidth, this.contentHeight));
            } else {
                resolvedHovers.add(h);
            }
        }
        // Finalize grid/scroll positioning
        java.util.List<TextureWidget> finalTextures = resolvedTextures;
        java.util.List<TextWidget> finalTexts = resolvedTexts;
        java.util.List<IconWidget> finalIcons = resolvedIcons;
        java.util.List<HoverTooltipWidget> finalHovers = resolvedHovers;
        java.util.List<SlotWidget> finalSlots = new java.util.ArrayList<>(resolved.size());
        for (SlotWidget s : resolved) {
            if (s instanceof Widgets.AddGrid.GridBuilder gb && gb.hasPositioning()) {
                finalSlots.add(gb.finalizePosition(this.contentWidth, this.contentHeight));
            } else if (s instanceof Widgets.AddScroll.ScrollBuilder sb && sb.hasPositioning()) {
                finalSlots.add(sb.finalizePosition(this.contentWidth, this.contentHeight));
            } else {
                finalSlots.add(s);
            }
        }
        return new SimpleRecipeDisplay(title, finalSlots, finalTextures, finalTexts, finalIcons, finalHovers, contentWidth, contentHeight);
    }
    
    private static class SimpleRecipeDisplay implements RecipeDisplay {
        private final String title;
        private final List<SlotWidget> slots;
        private final List<TextureWidget> textures;
        private final List<TextWidget> texts;
        private final List<HoverTooltipWidget> hovers;
        private final List<IconWidget> icons;
        
        private final int contentWidth;
        private final int contentHeight;
        
        public SimpleRecipeDisplay(String title, List<SlotWidget> slots, List<TextureWidget> textures, List<TextWidget> texts, List<IconWidget> icons, List<HoverTooltipWidget> hovers, int contentWidth, int contentHeight) {
            this.title = title;
            this.slots = new ArrayList<>(slots);
            this.textures = new ArrayList<>(textures);
            this.texts = new ArrayList<>(texts);
            this.icons = new ArrayList<>(icons);
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
        public List<IconWidget> icons() { return new ArrayList<>(icons); }
        
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
