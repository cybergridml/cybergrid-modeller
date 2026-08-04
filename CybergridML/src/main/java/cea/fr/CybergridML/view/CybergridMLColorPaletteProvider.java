package cea.fr.CybergridML.view;

import org.eclipse.sirius.components.view.ColorPalette;
import org.eclipse.sirius.components.view.FixedColor;
import org.eclipse.sirius.components.view.ViewFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * {@code org.eclipse.syson.services.ColorProvider} resolves named colors (e.g. {@code "grey 100"}) by first
 * loading {@code studioColorPalettes.json} from the classpath into a fresh {@link org.eclipse.emf.ecore.resource.ResourceSet}
 * for every view, then falling back to whatever {@link ColorPalette} is attached to the view itself. That classpath
 * load has been observed to silently fail for one of CybergridML's three views at application startup (a
 * Sirius Web timing issue, not something reproducible from CybergridML's own code), leaving every named color on
 * that view unresolved and rendered with the "black" default. Registering CybergridML's own colors directly on
 * each view via {@code IViewDescriptionProvider#buildColorPalette()} removes the dependency on that classpath
 * load succeeding.
 */
public final class CybergridMLColorPaletteProvider {

    private CybergridMLColorPaletteProvider() {
    }

    public static ColorPalette build() {
        ColorPalette colorPalette = ViewFactory.eINSTANCE.createColorPalette();
        colorPalette.setName("CyberGridML Colors Palette");
        collectColorNames().forEach(colorName -> colorPalette.getColors().add(toFixedColor(colorName)));
        return colorPalette;
    }

    private static Set<String> collectColorNames() {
        Set<String> colorNames = new LinkedHashSet<>();
        for (Field field : CybergridMLViewConstants.class.getFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == String.class) {
                try {
                    colorNames.add((String) field.get(null));
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        return colorNames;
    }

    private static FixedColor toFixedColor(String colorName) {
        FixedColor color = ViewFactory.eINSTANCE.createFixedColor();
        color.setName(colorName);
        int lastSpace = colorName.lastIndexOf(' ');
        String hue = colorName.substring(0, lastSpace);
        String shade = colorName.substring(lastSpace + 1);
        color.setValue(hue + "[" + shade + "]");
        return color;
    }
}
