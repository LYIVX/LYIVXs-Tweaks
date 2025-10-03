package net.lyivx.ls_tweaks.api.recipes;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** A renderable recipe instance containing positioned slots and textures. */
public interface RecipeDisplay {
    /** Optional background texture for this specific display. */
    default ResourceLocation backgroundTexture() { return null; }

    /** Title for this recipe instance; typically the output item name. */
    String titleText();

    /** All slot widgets that make up this recipe instance. */
    List<SlotWidget> slots();

    /** Arbitrary decorative textures (e.g., arrows, fire), positioned. */
    List<TextureWidget> textures();
    
    /** Text widgets. */
    default List<TextWidget> textWidgets() { return List.of(); }
    /** Hover tooltip widgets. */
    default List<HoverTooltipWidget> hoverWidgets() { return List.of(); }
    
    /** Content width for layout calculations. */
    default int contentWidth() { return 150; }
    
    /** Content height for layout calculations. */
    default int contentHeight() { return 66; }
}


