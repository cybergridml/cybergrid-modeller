package cea.fr.CybergridML.view.colors;

import org.eclipse.sirius.components.view.ColorPalette;
import org.eclipse.sirius.components.view.FixedColor;
import org.eclipse.sirius.components.view.ViewFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Set;


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
