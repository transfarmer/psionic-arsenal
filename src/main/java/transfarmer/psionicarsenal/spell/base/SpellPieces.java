package transfarmer.psionicarsenal.spell.base;

import net.minecraft.util.ResourceLocation;
import transfarmer.psionicarsenal.Main;
import transfarmer.psionicarsenal.spell.trick.entity.PieceTrickRedirectMotion;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.common.spell.base.ModSpellPieces.PieceContainer;

public class SpellPieces {
    public static PieceContainer trickRedirectMotion;

    public static void init() {
        trickRedirectMotion = register(PieceTrickRedirectMotion.class, "trick_redirect_motion", "movement");
    }

    public static PieceContainer register(final Class<? extends SpellPiece> pieceClass, final String name, final String group) {
        return register(pieceClass, name, group, false);
    }

    public static PieceContainer register(final Class<? extends SpellPiece> pieceClass, final String name, final String group, final boolean main) {
        PsiAPI.registerSpellPiece(String.format("%s.%s", Main.MOD_ID, name), pieceClass);
        PsiAPI.addPieceToGroup(pieceClass, group, main);
        registerTexture(name, Main.MOD_ID, name);

        return (final Spell spell) -> SpellPiece.create(pieceClass, spell);
    }

    public static void registerTexture(final String name, final String modId, final String texture) {
        PsiAPI.simpleSpellTextures.put(String.format("%s.%s", Main.MOD_ID, name), new ResourceLocation(modId, "textures/spell/" + texture + ".png"));
    }
}
