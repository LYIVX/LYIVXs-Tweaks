package net.lyivx.ls_tweaks.api.recipes.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotate a class implementing RecipeBrowserPlugin to auto-register categories. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LYIVXsTweakPlugin {
}


