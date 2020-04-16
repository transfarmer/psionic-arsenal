/*Copyright 2019 Dudblockman

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.*/
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
