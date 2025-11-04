package cy.jdkdigital.trophymanager.common.datamap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record PropertiesMap(float scale, double yOffset, float rotX) {
    public static final Codec<PropertiesMap> CODEC =
            RecordCodecBuilder.create(in -> in.group(
                    Codec.FLOAT.fieldOf("scale").forGetter(PropertiesMap::scale),
                    Codec.DOUBLE.fieldOf("offset").forGetter(PropertiesMap::yOffset),
                    Codec.FLOAT.fieldOf("rot").forGetter(PropertiesMap::rotX)
            ).apply(in, PropertiesMap::new));
}
